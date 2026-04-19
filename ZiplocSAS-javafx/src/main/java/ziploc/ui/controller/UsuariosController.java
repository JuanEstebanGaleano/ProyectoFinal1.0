package ziploc.ui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import ziploc.ui.client.ApiClient;

import java.util.*;

public class UsuariosController {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

    public Node buildView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("👥 Gestión de Usuarios");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        // ── Formulario ──────────────────────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);

        TextField fNombre = campo("Nombre");
        TextField fEmail  = campo("Email");
        TextField fCedula = campo("Cédula");

        form.add(label("Nombre:"), 0, 0); form.add(fNombre, 1, 0);
        form.add(label("Email:"),  0, 1); form.add(fEmail,  1, 1);
        form.add(label("Cédula:"), 0, 2); form.add(fCedula, 1, 2);

        Button btnRegistrar = boton("➕ Registrar", "#4f98a3");
        Button btnRefresh   = boton("🔄 Actualizar lista", "#6daa45");
        Button btnEliminar  = boton("🗑️ Eliminar seleccionado", "#dd6974");

        Label lblMsg = new Label();
        lblMsg.setTextFill(Color.web("#4f98a3"));

        HBox botones = new HBox(10, btnRegistrar, btnRefresh, btnEliminar);

        // ── Tabla ────────────────────────────────────────────────────────────
        tabla.setItems(datos);
        tabla.setStyle("-fx-background-color: #16213e; -fx-text-fill: white;");
        tabla.setMinHeight(300);

        agregarColumna("ID",       "id",       90);
        agregarColumna("Nombre",   "nombre",   150);
        agregarColumna("Email",    "email",    200);
        agregarColumna("Cédula",   "cedula",   120);
        agregarColumna("Puntos",   "puntos",   80);
        agregarColumna("Nivel",    "nivel",    90);

        // ── Acciones ─────────────────────────────────────────────────────────
        btnRegistrar.setOnAction(e -> {
            if (fNombre.getText().isBlank() || fEmail.getText().isBlank() || fCedula.getText().isBlank()) {
                mostrarMsg(lblMsg, "❌ Completa todos los campos", "#dd6974"); return;
            }
            Map<String, Object> body = Map.of(
                    "nombre", fNombre.getText().trim(),
                    "email",  fEmail.getText().trim(),
                    "cedula", fCedula.getText().trim()
            );
            new Thread(() -> {
                String resp = ApiClient.post("/usuarios", body);
                JsonNode r = ApiClient.parse(resp);
                Platform.runLater(() -> {
                    if (r.path("exito").asBoolean()) {
                        mostrarMsg(lblMsg, "✅ " + r.path("mensaje").asText(), "#4f98a3");
                        fNombre.clear(); fEmail.clear(); fCedula.clear();
                        cargarUsuarios();
                    } else {
                        mostrarMsg(lblMsg, "❌ " + r.path("mensaje").asText(), "#dd6974");
                    }
                });
            }).start();
        });

        btnRefresh.setOnAction(e -> cargarUsuarios());

        btnEliminar.setOnAction(e -> {
            Map<String, String> sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarMsg(lblMsg, "❌ Selecciona un usuario", "#dd6974"); return; }
            new Thread(() -> {
                String resp = ApiClient.delete("/usuarios/" + sel.get("id"));
                JsonNode r = ApiClient.parse(resp);
                Platform.runLater(() -> {
                    mostrarMsg(lblMsg, r.path("exito").asBoolean()
                                    ? "✅ Usuario eliminado" : "❌ " + r.path("mensaje").asText(),
                            r.path("exito").asBoolean() ? "#4f98a3" : "#dd6974");
                    cargarUsuarios();
                });
            }).start();
        });

        cargarUsuarios();
        root.getChildren().addAll(title, form, botones, lblMsg, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return root;
    }

    private void cargarUsuarios() {
        new Thread(() -> {
            String json = ApiClient.get("/usuarios");
            JsonNode arr = ApiClient.parse(json).path("datos");
            List<Map<String, String>> lista = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode u : arr) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id",      u.path("id").asText());
                    row.put("nombre",  u.path("nombre").asText());
                    row.put("email",   u.path("email").asText());
                    row.put("cedula",  u.path("cedula").asText());
                    row.put("puntos",  String.valueOf(u.path("puntosTotales").asInt()));
                    row.put("nivel",   u.path("nivel").asText());
                    lista.add(row);
                }
            }
            Platform.runLater(() -> { datos.setAll(lista); });
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

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        tf.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #0f3638; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;");
        return tf;
    }

    private Label label(String txt) {
        Label l = new Label(txt);
        l.setTextFill(Color.web("#aaa"));
        l.setFont(Font.font("Segoe UI", 13));
        return l;
    }

    private Button boton(String txt, String color) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-size: 13px;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private void mostrarMsg(Label l, String msg, String color) {
        l.setText(msg); l.setTextFill(Color.web(color));
    }
}