package ziploc.ui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import ziploc.ui.client.ApiClient;

public class AnaliticaController {

    public Node buildView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("📈 Analítica del Sistema");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        Button btnRefresh = new Button("🔄 Actualizar");
        btnRefresh.setStyle("-fx-background-color:#4f98a3;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:8 16;");

        // Contenedor dinámico
        VBox contenido = new VBox(16);

        btnRefresh.setOnAction(e -> cargar(contenido));
        cargar(contenido);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(title, btnRefresh, scroll);
        return root;
    }

    private void cargar(VBox contenido) {
        new Thread(() -> {
            String json = ApiClient.get("/analitica/reporte");
            JsonNode data = ApiClient.parse(json).path("datos");

            Platform.runLater(() -> {
                contenido.getChildren().clear();

                // ── KPIs principales ─────────────────────────────────────────
                HBox kpis = new HBox(16);
                kpis.getChildren().addAll(
                        kpi("👥 Usuarios",         String.valueOf(data.path("totalUsuarios").asLong()),     "#4f98a3"),
                        kpi("💼 Billeteras",        String.valueOf(data.path("totalBilleteras").asLong()),  "#6daa45"),
                        kpi("💸 Transacciones",     String.valueOf(data.path("totalTransacciones").asLong()),"#e8af34"),
                        kpi("💰 Monto 24h",         String.format("$%.2f", data.path("montoUltimas24h").asDouble()), "#5591c7"),
                        kpi("⚠️ Sospechosas",       String.valueOf(data.path("transaccionesSospechosas").asInt()), "#dd6974")
                );

                // ── Destacados ───────────────────────────────────────────────
                VBox destacados = seccion("🏆 Destacados");
                destacados.getChildren().addAll(
                        fila("Usuario más activo:",  data.path("usuarioMasActivoNombre").asText("N/A")),
                        fila("Billetera top:",        data.path("billeteraTopNombre").asText("N/A")
                                + " (" + data.path("billeteraTopTxs").asInt() + " txs)")
                );

                // ── Frecuencia por tipo ──────────────────────────────────────
                VBox tiposBox = seccion("📊 Frecuencia por tipo de transacción");
                JsonNode tipos = data.path("frecuenciaPorTipo");
                tipos.fields().forEachRemaining(entry ->
                        tiposBox.getChildren().add(
                                barraProgreso(entry.getKey(), entry.getValue().asInt(),
                                        tiposMax(data))));

                // ── Actividad por categoría ──────────────────────────────────
                VBox catBox = seccion("📂 Actividad por categoría de billetera");
                JsonNode cats = data.path("actividadPorCategoria");
                cats.fields().forEachRemaining(entry ->
                        catBox.getChildren().add(
                                barraProgreso(entry.getKey(), entry.getValue().asInt(),
                                        catsMax(data))));

                // ── Ranking usuarios ─────────────────────────────────────────
                VBox rankBox = seccion("🏅 Ranking usuarios por puntos");
                JsonNode usuarios = data.path("usuariosPorPuntos");
                if (usuarios.isArray()) {
                    int pos = 1;
                    for (JsonNode u : usuarios) {
                        String medal = pos == 1 ? "🥇" : pos == 2 ? "🥈" : pos == 3 ? "🥉" : "  " + pos + ".";
                        rankBox.getChildren().add(fila(
                                medal + " " + u.path("nombre").asText(),
                                u.path("puntosTotales").asInt() + " pts  |  " + u.path("nivel").asText()));
                        pos++;
                    }
                }

                contenido.getChildren().addAll(kpis, destacados, tiposBox, catBox, rankBox);
            });
        }).start();
    }

    private int tiposMax(JsonNode data) {
        int max = 1;
        JsonNode tipos = data.path("frecuenciaPorTipo");
        for (JsonNode v : tipos) max = Math.max(max, v.asInt());
        return max;
    }

    private int catsMax(JsonNode data) {
        int max = 1;
        JsonNode cats = data.path("actividadPorCategoria");
        for (JsonNode v : cats) max = Math.max(max, v.asInt());
        return max;
    }

    private VBox kpi(String titulo, String valor, String color) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(14, 20, 14, 20));
        c.setMinWidth(130);
        c.setAlignment(Pos.CENTER_LEFT);
        c.setStyle("-fx-background-color:#16213e;-fx-background-radius:10;" +
                "-fx-border-color:" + color + "44;-fx-border-radius:10;-fx-border-width:1;");
        Label l = new Label(titulo); l.setTextFill(Color.web("#888")); l.setFont(Font.font("Segoe UI", 11));
        Label v = new Label(valor);  v.setTextFill(Color.web(color));  v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        c.getChildren().addAll(l, v);
        return c;
    }

    private VBox seccion(String titulo) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#16213e;-fx-background-radius:10;");
        Label lbl = new Label(titulo);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        lbl.setTextFill(Color.web("#4f98a3"));
        box.getChildren().add(lbl);
        return box;
    }

    private HBox fila(String clave, String valor) {
        Label k = new Label(clave); k.setTextFill(Color.web("#888")); k.setMinWidth(200);
        Label v = new Label(valor); v.setTextFill(Color.web("#ddd")); v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        HBox row = new HBox(12, k, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox barraProgreso(String nombre, int valor, int maximo) {
        double pct = maximo > 0 ? (double) valor / maximo : 0;
        VBox box = new VBox(3);

        HBox header = new HBox();
        Label lNombre = new Label(nombre); lNombre.setTextFill(Color.web("#ccc")); lNombre.setMinWidth(220);
        Label lValor  = new Label(String.valueOf(valor)); lValor.setTextFill(Color.web("#4f98a3"));
        header.getChildren().addAll(lNombre, lValor);

        ProgressBar pb = new ProgressBar(pct);
        pb.setPrefWidth(400); pb.setPrefHeight(10);
        pb.setStyle("-fx-accent: #4f98a3; -fx-background-color: #0f3638; -fx-background-radius: 5;");

        box.getChildren().addAll(header, pb);
        return box;
    }
}