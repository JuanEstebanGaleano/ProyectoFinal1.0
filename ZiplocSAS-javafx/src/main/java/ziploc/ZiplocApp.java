package ziploc;

import ziploc.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ZiplocApp extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        MainWindow window = new MainWindow();
        Scene scene = new Scene(window.build(), 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        stage.setTitle("💳 Ziploc Billeteras – Estructuras de Datos 2026");
        stage.setScene(scene);
        stage.show();
    }
}