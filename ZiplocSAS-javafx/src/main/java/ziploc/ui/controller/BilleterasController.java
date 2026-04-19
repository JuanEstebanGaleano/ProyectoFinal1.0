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

public class BilleterasController {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

    public Node buildView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("💼 Gestión de Billeteras");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        // ── Formulario crear billetera ───────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);

        TextField fUid    = campo("ID de usuario");
        TextField fNombre = campo("Nombre billetera");
        TextField fSaldo  = campo("Saldo inicial");
        ComboBox<String> cboTipo = new ComboBox<>(FXCollections.observableArrayList(
                "AHORRO", "GASTOS_DIARIOS", "COMPRAS", "TRANSPORTE", "INVERSION"));
        cboTipo.setValue("AHORRO");
        cboTipo.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3638;");

        form.add(label("ID Usuario:"), 0, 0); form.add(fUid,    1, 0);
        form.add(label("Nombre:"),     0, 1); form.add(fNombre, 1, 1);
        form.add(label("Tipo:"),       0, 2); form.add(cboTipo, 1, 2);
        form.add(label("Saldo ini.:"), 0, 3); form.add(fSaldo,  1, 3);

        // ── Formulario recarga/retiro ─────────────────────────────────────────
        GridPane formOp = new GridPane();
        formOp.setHgap(12); formOp.setVgap(10);

        TextField fBid   = campo("ID billetera");
        TextField fMonto = campo("Monto");

        formOp.add(label("ID Billetera:"), 0, 0); formOp.add(fBid,   1, 0);
        formOp.add(label("Monto:"),        0, 1); formOp.add(fMonto, 1, 1);

        // ── Botones ──────────────────────────────────────────────────────────
        Button btnCrear    = boton("➕ Crear", "#4f98a3");
        Button btnRecargar = boton("⬆️ Recargar", "#6daa45");
        Button btnRetirar  = boton("⬇️ Retirar", "#e8af34");
        Button btnRefresh  = boton("🔄 Listar todas", "#5591c7");

        Label lblMsg = new Label();
        lblMsg.setTextFill(Color.web("#4f98a3"));

        HBox botonesA = new HBox(10, btnCrear, btnRefresh);
        HBox botonesB = new HBox(10, btnRecargar, btnRetirar);

        // ── Tabla ────────────────────────────────────────────────────────────
        tabla.setItems(datos);
        tabla.setStyle("-fx-background-color: #16213e;");
        tabla.setMinHeight(260);

        agregarColumna("ID",       "id",       90);
        agregarColumna("Nombre",   "nombre",   150);
        agregarColumna("Tipo",     "tipo",     120);
        agregarColumna("Saldo",    "saldo",    100);
        agregarColumna("Activa",   "activa",   70);
        agregarColumna("Txs",      "txs",      60);
        agregarColumna("Usuario",  "uid",      90);

        // ── Acciones ─────────────────────────────────────────────────────────
        btnCrear.setOnAction(e -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("usuarioId",    fUid.getText().trim());
            body.put("nombre",       fNombre.getText().trim());
            body.put("tipo",         cboTipo.getValue());
            body.put("saldoInicial", parsearDouble(fSaldo.getText()));
            new Thread(() -> {
                String resp = ApiClient.post("/billeteras", body);
                JsonNode r = ApiClient.parse(resp);
                Platform.runLater(() -> {
                    mostrarMsg(lblMsg,
                            r.path("exito").asBoolean() ? "✅ " + r.path("mensaje").asText() : "❌ " + r.path("mensaje").asText(),
                            r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974");
                    if (r.path("exito").asBoolean()) cargarTodas();
                });
            }).start();
        });

        btnRecargar.setOnAction(e -> operacion(fBid.getText(), fMonto.getText(), "recargar", lblMsg));
        btnRetirar.setOnAction(e  -> operacion(fBid.getText(), fMonto.getText(), "retirar",  lblMsg));
        btnRefresh.setOnAction(e  -> cargarTodas());

        // Al seleccionar fila, copiar ID a campo operación
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) fBid.setText(n.get("id"));
        });

        cargarTodas();
        root.getChildren().addAll(title,
                new Label("── Crear billetera ──") {{ setTextFill(Color.web("#666")); }},
                form, botonesA,
                new Label("── Recargar / Retirar ──") {{ setTextFill(Color.web("#666")); }},
                formOp, botonesB, lblMsg, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return root;
    }

    private void operacion(String bid, String monto, String tipo, Label lblMsg) {
        if (bid.isBlank()) { mostrarMsg(lblMsg, "❌ ID de billetera requerido", "#dd6974"); return; }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("billeteraId", bid.trim());
        body.put("monto", parsearDouble(monto));
        new Thread(() -> {
            String resp = ApiClient.post("/billeteras/" + bid.trim() + "/" + tipo, body);
            JsonNode r = ApiClient.parse(resp);
            Platform.runLater(() -> {
                mostrarMsg(lblMsg,
                        r.path("exito").asBoolean() ? "✅ " + r.path("mensaje").asText() : "❌ " + r.path("mensaje").asText(),
                        r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974");
                cargarTodas();
            });
        }).start();
    }

    private void cargarTodas() {
        new Thread(() -> {
            String json = ApiClient.get("/billeteras/usuario/ALL");
            // Intenta el endpoint general de listado ordenado
            json = ApiClient.get("/analitica/reporte");
            JsonNode rep = ApiClient.parse(json);

            // Fallback: listar desde reporte no es lo ideal;
            // usa endpoint real si tienes /billeteras (GET all)
            // Por ahora llamamos al endpoint de billeteras ordenadas
            String jsonB = ApiClient.get("/billeteras/ranking");
            JsonNode arr = ApiClient.parse(jsonB).path("datos");

            List<Map<String, String>> lista = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode b : arr) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id",     b.path("id").asText());
                    row.put("nombre", b.path("nombre").asText());
                    row.put("tipo",   b.path("tipo").asText());
                    row.put("saldo",  String.format("$%.2f", b.path("saldo").asDouble()));
                    row.put("activa", b.path("activa").asBoolean() ? "✅" : "❌");
                    row.put("txs",    String.valueOf(b.path("totalTransacciones").asInt()));
                    row.put("uid",    b.path("usuarioId").asText());
                    lista.add(row);
                }
            }
            Platform.runLater(() -> datos.setAll(lista));
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void agregarColumna(String titulo, String campo, int ancho) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(ancho);
        col.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        cd.getValue().getOrDefault(campo, "")));
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
        b.setStyle("-fx-background-color: " + c + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16;");
        return b;
    }
    private void mostrarMsg(Label l, String m, String c) { l.setText(m); l.setTextFill(Color.web(c)); }
    private double parsearDouble(String s) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; } }
}
