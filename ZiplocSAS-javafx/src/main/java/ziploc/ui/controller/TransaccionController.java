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

import java.util.*;

public class TransaccionController {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

    public Node buildView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("💸 Transacciones");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        // ── Formulario operaciones ───────────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);

        TextField fUid    = campo("ID usuario");
        TextField fBOrig  = campo("ID billetera origen");
        TextField fBDest  = campo("ID billetera destino");
        TextField fMonto  = campo("Monto");

        form.add(label("ID Usuario:"),    0, 0); form.add(fUid,   1, 0);
        form.add(label("Billetera orig:"),0, 1); form.add(fBOrig, 1, 1);
        form.add(label("Billetera dest:"),0, 2); form.add(fBDest, 1, 2);
        form.add(label("Monto:"),         0, 3); form.add(fMonto, 1, 3);

        Button btnRecargar    = boton("⬆️ Recargar",     "#4f98a3");
        Button btnRetirar     = boton("⬇️ Retirar",      "#e8af34");
        Button btnTransferir  = boton("↔️ Transferir",   "#6daa45");
        Button btnRevertir    = boton("↩️ Revertir",     "#dd6974");
        Button btnHistorial   = boton("📋 Historial",    "#5591c7");
        Button btnSospechosas = boton("⚠️ Sospechosas",  "#a86fdf");

        Label lblMsg = new Label();
        lblMsg.setTextFill(Color.web("#4f98a3"));

        HBox bots1 = new HBox(10, btnRecargar, btnRetirar, btnTransferir);
        HBox bots2 = new HBox(10, btnRevertir, btnHistorial, btnSospechosas);

        // ── Tabla ────────────────────────────────────────────────────────────
        tabla.setItems(datos);
        tabla.setStyle("-fx-background-color: #16213e;");
        tabla.setMinHeight(260);

        agregarColumna("ID",      "id",       90);
        agregarColumna("Fecha",   "fecha",    140);
        agregarColumna("Tipo",    "tipo",     160);
        agregarColumna("Valor",   "valor",    100);
        agregarColumna("Estado",  "estado",   100);
        agregarColumna("Puntos",  "puntos",   70);
        agregarColumna("Riesgo",  "riesgo",   80);

        // ── Acciones ─────────────────────────────────────────────────────────
        btnRecargar.setOnAction(e -> {
            Map<String, Object> body = Map.of("billeteraId", fBDest.getText().trim(), "monto", parsearDouble(fMonto.getText()));
            new Thread(() -> ejecutar("/transacciones/recargar?usuarioId=" + fUid.getText().trim(), body, lblMsg)).start();
        });

        btnRetirar.setOnAction(e -> {
            Map<String, Object> body = Map.of("billeteraId", fBOrig.getText().trim(), "monto", parsearDouble(fMonto.getText()));
            new Thread(() -> ejecutar("/transacciones/retirar?usuarioId=" + fUid.getText().trim(), body, lblMsg)).start();
        });

        btnTransferir.setOnAction(e -> {
            Map<String, Object> body = Map.of(
                    "billeteraOrigenId",  fBOrig.getText().trim(),
                    "billeteraDestinoId", fBDest.getText().trim(),
                    "monto",              parsearDouble(fMonto.getText()));
            new Thread(() -> ejecutar("/transacciones/transferir?usuarioOrigenId=" + fUid.getText().trim(), body, lblMsg)).start();
        });

        btnRevertir.setOnAction(e -> new Thread(() -> {
            String resp = ApiClient.post("/transacciones/revertir?usuarioId=" + fUid.getText().trim());
            JsonNode r = ApiClient.parse(resp);
            Platform.runLater(() -> mostrarMsg(lblMsg,
                    r.path("exito").asBoolean() ? "✅ Revertida" : "❌ " + r.path("mensaje").asText(),
                    r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974"));
        }).start());

        btnHistorial.setOnAction(e -> new Thread(() -> {
            String uid = fUid.getText().trim();
            if (uid.isBlank()) { Platform.runLater(() -> mostrarMsg(lblMsg, "❌ ID de usuario requerido", "#dd6974")); return; }
            cargarTransacciones("/transacciones/usuario/" + uid);
        }).start());

        btnSospechosas.setOnAction(e -> new Thread(() ->
                cargarTransacciones("/transacciones/sospechosas")).start());

        root.getChildren().addAll(title, form, bots1, bots2, lblMsg, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return root;
    }

    private void ejecutar(String path, Map<String, Object> body, Label lblMsg) {
        String resp = ApiClient.post(path, body);
        JsonNode r = ApiClient.parse(resp);
        Platform.runLater(() -> {
            mostrarMsg(lblMsg,
                    r.path("exito").asBoolean() ? "✅ " + r.path("mensaje").asText() : "❌ " + r.path("mensaje").asText(),
                    r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974");
        });
    }

    private void cargarTransacciones(String path) {
        String json = ApiClient.get(path);
        JsonNode arr = ApiClient.parse(json).path("datos");
        List<Map<String, String>> lista = new ArrayList<>();
        if (arr.isArray()) {
            for (JsonNode t : arr) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("id",     t.path("id").asText());
                row.put("fecha",  t.path("fecha").asText("").substring(0, Math.min(16, t.path("fecha").asText("").length())));
                row.put("tipo",   t.path("tipo").asText());
                row.put("valor",  String.format("$%.2f", t.path("valor").asDouble()));
                row.put("estado", t.path("estado").asText());
                row.put("puntos", String.valueOf(t.path("puntosGenerados").asInt()));
                row.put("riesgo", t.path("nivelRiesgo").asText());
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
        TextField tf = new TextField(); tf.setPromptText(p); tf.setPrefWidth(210);
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