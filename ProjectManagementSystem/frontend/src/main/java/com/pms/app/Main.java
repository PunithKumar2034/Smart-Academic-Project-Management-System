package com.pms.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginScreen.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        
        // Apply JMetro Dark Theme
        JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);
        
        // Apply custom CSS
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        // Try loading login.css, if it exists
        try {
            java.net.URL loginCss = getClass().getResource("/css/login.css");
            if (loginCss != null) {
                scene.getStylesheets().add(loginCss.toExternalForm());
            }
        } catch (Exception e) {}

        primaryStage.setTitle("SYNORA - Project Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
