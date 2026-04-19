package ziploc.ui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import ziploc.ui.client.ApiClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProgramadasController {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

    public Node buildView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("📅 Operaciones Programadas");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        // ── Formulario ──────────────────────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);

        TextField fUid    = campo("ID usuario");
        TextField fBOrig  = campo("ID billetera origen (opcional)");
        TextField fBDest  = campo("ID billetera destino");
        TextField fMonto  = campo("Monto");
        TextField fDesc   = campo("Descripción");
        TextField fMin    = campo("Ejecutar en X minutos desde ahora");

        ComboBox<String> cboTipo = new ComboBox<>(FXCollections.observableArrayList(
                "RECARGA", "RETIRO", "TRANSFERENCIA_ENVIADA", "PAGO_PROGRAMADO"));
        cboTipo.setValue("RECARGA");
        cboTipo.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3638;");

        form.add(label("ID Usuario:"),    0, 0); form.add(fUid,   1, 0);
        form.add(label("B. Origen:"),     0, 1); form.add(fBOrig, 1, 1);
        form.add(label("B. Destino:"),    0, 2); form.add(fBDest, 1, 2);
        form.add(label("Tipo:"),          0, 3); form.add(cboTipo,1, 3);
        form.add(label("Monto:"),         0, 4); form.add(fMonto, 1, 4);
        form.add(label("Descripción:"),   0, 5); form.add(fDesc,  1, 5);
        form.add(label("En X minutos:"),  0, 6); form.add(fMin,   1, 6);

        Button btnProgramar = boton("📅 Programar",          "#4f98a3");
        Button btnEjecutar  = boton("▶️ Ejecutar pendientes", "#6daa45");
        Button btnPendientes= boton("📋 Ver pendientes",      "#5591c7");
        Button btnTodas     = boton("🔄 Ver todas",           "#a86fdf");

        Label lblMsg = new Label();
        lblMsg.setTextFill(Color.web("#4f98a3"));

        HBox botones = new HBox(10, btnProgramar, btnEjecutar, btnPendientes, btnTodas);

        // ── Tabla ────────────────────────────────────────────────────────────
        tabla.setItems(datos);
        tabla.setStyle("-fx-background-color: #16213e;");
        tabla.setMinHeight(250);

        agregarColumna("ID",          "id",       90);
        agregarColumna("Descripción", "desc",     160);
        agregarColumna("Tipo",        "tipo",     150);
        agregarColumna("Monto",       "monto",    90);
        agregarColumna("Fecha Ejec.", "fecha",    140);
        agregarColumna("Estado",      "estado",   90);
        agregarColumna("Usuario",     "uid",      90);

        // ── Acciones ─────────────────────────────────────────────────────────
        btnProgramar.setOnAction(e -> {
            int minutos = 1;
            try { minutos = Integer.parseInt(fMin.getText().trim()); } catch (Exception ex) { /* default 1 */ }
            LocalDateTime fecha = LocalDateTime.now().plusMinutes(minutos);
            String fechaStr = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("usuarioId",         fUid.getText().trim());
            body.put("billeteraOrigenId", fBOrig.getText().isBlank() ? null : fBOrig.getText().trim());
            body.put("billeteraDestinoId",fBDest.getText().trim());
            body.put("tipo",              cboTipo.getValue());
            body.put("monto",             parsearDouble(fMonto.getText()));
            body.put("fechaEjecucion",    fechaStr);
            body.put("descripcion",       fDesc.getText().trim());

            new Thread(() -> {
                String resp = ApiClient.post("/operaciones-programadas", body);
                JsonNode r = ApiClient.parse(resp);
                Platform.runLater(() -> {
                    mostrarMsg(lblMsg,
                            r.path("exito").asBoolean() ? "✅ " + r.path("mensaje").asText() : "❌ " + r.path("mensaje").asText(),
                            r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974");
                    cargar("/operaciones-programadas/pendientes");
                });
            }).start();
        });

        btnEjecutar.setOnAction(e -> new Thread(() -> {
            String resp = ApiClient.post("/operaciones-programadas/ejecutar-pendientes");
            JsonNode r = ApiClient.parse(resp);
            Platform.runLater(() -> {
                mostrarMsg(lblMsg, "✅ Ejecutadas: " + r.path("datos").asInt(0), "#4f98a3");
                cargar("/operaciones-programadas/pendientes");
            });
        }).start());

        btnPendientes.setOnAction(e -> new Thread(() ->
                cargar("/operaciones-programadas/pendientes")).start());

        btnTodas.setOnAction(e -> {
            String uid = fUid.getText().trim();
            if (!uid.isBlank())
                new Thread(() -> cargar("/operaciones-programadas/usuario/" + uid)).start();
            else
                mostrarMsg(lblMsg, "⚠️ Ingresa el ID de usuario para ver sus operaciones", "#e8af34");
        });

        cargar("/operaciones-programadas/pendientes");
        root.getChildren().addAll(title, form, botones, lblMsg, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return root;
    }

    private void cargar(String path) {
        String json = ApiClient.get(path);
        JsonNode arr = ApiClient.parse(json).path("datos");
        List<Map<String, String>> lista = new ArrayList<>();
        if (arr.isArray()) {
            for (JsonNode op : arr) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("id",     op.path("id").asText());
                row.put("desc",   op.path("descripcion").asText());
                row.put("tipo",   op.path("tipo").asText());
                row.put("monto",  String.format("$%.2f", op.path("monto").asDouble()));
                String f = op.path("fechaEjecucion").asText("");
                row.put("fecha",  f.length() >= 16 ? f.substring(0, 16) : f);
                row.put("estado", op.path("ejecutada").asBoolean() ? "✅ EJECUTADA" : "⏳ PENDIENTE");
                row.put("uid",    op.path("usuarioId").asText());
                lista.add(row);
            }
        }
        Platform.runLater(() -> datos.setAll(lista));
    }

    @SuppressWarnings("unchecked")
    private void agregarColumna(String titulo, String campo, int ancho) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(ancho);
        col.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getOrDefault(campo, "")));
        tabla.getColumns().add(col);
    }

    private TextField campo(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); tf.setPrefWidth(230);
        tf.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3638; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;");
        return tf;
    }
    private Label label(String t) { Label l = new Label(t); l.setTextFill(Color.web("#aaa")); return l; }
    private Button boton(String t, String c) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + c + ";-fx-text-fill:white;-fx-background-radius:6;-fx-padding:8 14;");
        return b;
    }
    private void mostrarMsg(Label l, String m, String c) { l.setText(m); l.setTextFill(Color.web(c)); }
    private double parsearDouble(String s) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; } }
}