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

public class DashboardController {

    public Node buildView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("📊 Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#4f98a3"));

        // KPI Grid
        GridPane kpis = new GridPane();
        kpis.setHgap(16); kpis.setVgap(16);

        // Cargar datos del reporte
        new Thread(() -> {
            String json = ApiClient.get("/analitica/reporte");
            JsonNode data = ApiClient.parse(json).path("datos");

            long usuarios     = data.path("totalUsuarios").asLong(0);
            long billeteras   = data.path("totalBilleteras").asLong(0);
            long transacs     = data.path("totalTransacciones").asLong(0);
            double monto24h   = data.path("montoUltimas24h").asDouble(0);
            int sospechosas   = data.path("transaccionesSospechosas").asInt(0);
            String masActivo  = data.path("usuarioMasActivoNombre").asText("N/A");
            String billTop    = data.path("billeteraTopNombre").asText("N/A");

            Platform.runLater(() -> {
                kpis.getChildren().clear();
                kpis.add(kpiCard("👥 Usuarios",        String.valueOf(usuarios),     "#4f98a3"), 0, 0);
                kpis.add(kpiCard("💼 Billeteras",       String.valueOf(billeteras),   "#6daa45"), 1, 0);
                kpis.add(kpiCard("💸 Transacciones",    String.valueOf(transacs),     "#e8af34"), 2, 0);
                kpis.add(kpiCard("💰 Monto (24h)",      String.format("$%.0f", monto24h), "#5591c7"), 0, 1);
                kpis.add(kpiCard("⚠️  Sospechosas",     String.valueOf(sospechosas),  "#dd6974"), 1, 1);
                kpis.add(kpiCard("🏆 Más activo",       masActivo,                    "#a86fdf"), 2, 1);

                // Info extra
                Label extra = new Label("💼 Billetera top: " + billTop);
                extra.setTextFill(Color.web("#999"));
                extra.setFont(Font.font("Segoe UI", 13));
                root.getChildren().removeIf(n -> n instanceof Label && ((Label)n).getText().startsWith("💼 Billetera"));
                root.getChildren().add(extra);
            });
        }).start();

        Label loading = new Label("⏳ Cargando datos...");
        loading.setTextFill(Color.web("#666"));

        root.getChildren().addAll(title, kpis, loading);
        return root;
    }

    private VBox kpiCard(String titulo, String valor, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setMinWidth(160); card.setMinHeight(90);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "44; -fx-border-radius: 10; -fx-border-width: 1;");

        Label lbl = new Label(titulo);
        lbl.setTextFill(Color.web("#888")); lbl.setFont(Font.font("Segoe UI", 12));

        Label val = new Label(valor);
        val.setTextFill(Color.web(color));
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        card.getChildren().addAll(lbl, val);
        return card;
    }
}