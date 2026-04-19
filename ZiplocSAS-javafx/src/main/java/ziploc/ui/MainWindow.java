package ziploc.ui;

import javafx.scene.Node;
import ziploc.ui.controller.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainWindow {

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // ── Barra lateral ─────────────────────────────────────────────────
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // ── Área de contenido ─────────────────────────────────────────────
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #1a1a2e;");
        root.setCenter(content);

        // ── Barra de estado ───────────────────────────────────────────────
        Label status = new Label("  ✅ Conectado · http://localhost:8080  |  Swagger: /swagger-ui.html  |  H2: /h2-console");
        status.setTextFill(Color.web("#666"));
        status.setStyle("-fx-background-color: #111; -fx-padding: 4 12;");
        status.setMaxWidth(Double.MAX_VALUE);
        root.setBottom(status);

        // Controladores
        DashboardController    dashboard  = new DashboardController();
        UsuariosController     usuarios   = new UsuariosController();
        BilleterasController   billeteras = new BilleterasController();
        TransaccionController  transacs   = new TransaccionController();
        ProgramadasController  programadas = new ProgramadasController();
        AnaliticaController    analitica  = new AnaliticaController();

        // Cargar dashboard por defecto
        content.getChildren().setAll(dashboard.buildView());

        // Navegación
        for (Node btn : sidebar.getChildren()) {
            if (btn instanceof Button b) {
                b.setOnAction(e -> {
                    content.getChildren().clear();
                    content.getChildren().add(switch (b.getText()) {
                        case "📊 Dashboard"            -> dashboard.buildView();
                        case "👥 Usuarios"              -> usuarios.buildView();
                        case "💼 Billeteras"            -> billeteras.buildView();
                        case "💸 Transacciones"         -> transacs.buildView();
                        case "📅 Operaciones Prog."     -> programadas.buildView();
                        case "📈 Analítica"             -> analitica.buildView();
                        default -> dashboard.buildView();
                    });
                });
            }
        }
        return root;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(6);
        sb.setPadding(new Insets(20, 12, 20, 12));
        sb.setPrefWidth(190);
        sb.setStyle("-fx-background-color: #16213e; -fx-border-color: #0f3638; -fx-border-width: 0 1 0 0;");

        Label logo = new Label("💳 Fintech");
        logo.setFont(Font.font("Segoe UI", 18));
        logo.setTextFill(Color.web("#4f98a3"));
        logo.setPadding(new Insets(0, 0, 16, 4));

        String[] items = {"📊 Dashboard","👥 Usuarios","💼 Billeteras","💸 Transacciones","📅 Operaciones Prog.","📈 Analítica"};
        sb.getChildren().add(logo);
        for (String item : items) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14; -fx-background-radius: 6; -fx-font-size: 13px;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #0f3638; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14; -fx-background-radius: 6; -fx-font-size: 13px;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14; -fx-background-radius: 6; -fx-font-size: 13px;"));
            sb.getChildren().add(btn);
        }
        return sb;
    }
}