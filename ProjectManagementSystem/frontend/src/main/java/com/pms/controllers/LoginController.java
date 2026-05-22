package com.pms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import animatefx.animation.Shake;
import com.pms.services.AuthService;
import javafx.application.Platform;

import javafx.scene.control.ComboBox;

public class LoginController {
    @FXML private javafx.scene.layout.StackPane rootPane;
    @FXML private javafx.scene.layout.VBox leftPanel;
    @FXML private javafx.scene.control.ToggleButton toggleFaculty;
    @FXML private javafx.scene.control.ToggleButton toggleStudent;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Window Drag Logic
        if (rootPane != null) {
            rootPane.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            rootPane.setOnMouseDragged(event -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            
            // Fade-in animation for the root pane
            rootPane.setOpacity(0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(600), rootPane);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }

        // Slide-in animation for left panel elements
        if (leftPanel != null) {
            double delay = 0;
            for (javafx.scene.Node node : leftPanel.getChildren()) {
                node.setTranslateX(-50);
                node.setOpacity(0);
                
                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(400), node);
                tt.setToX(0);
                tt.setDelay(javafx.util.Duration.millis(delay));
                
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), node);
                ft.setToValue(1.0);
                ft.setDelay(javafx.util.Duration.millis(delay));
                
                tt.play();
                ft.play();
                delay += 50;
            }
        }

        // Button hover animation
        if (loginButton != null) {
            loginButton.setOnMouseEntered(e -> {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), loginButton);
                st.setToX(1.03);
                st.setToY(1.03);
                st.play();
            });
            loginButton.setOnMouseExited(e -> {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), loginButton);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        
        // Determine role based on which toggle is selected
        final String role = (toggleStudent != null && toggleStudent.isSelected()) ? "Student" : "Faculty";

        if (email.isEmpty() || password.isEmpty() || role == null) {
            new Shake(loginButton).play();
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.TOMATO);
                statusLabel.setText("Please enter email, password, and role.");
            }
            return;
        }

        loginButton.setText("LOGGING IN...");
        loginButton.setDisable(true);
        if (statusLabel != null) statusLabel.setText("");

        // Run authentication in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                boolean success = AuthService.login(email, password, role);
                Platform.runLater(() -> {
                    loginButton.setText("LOGIN");
                    loginButton.setDisable(false);
                    
                    if (success) {
                        if (statusLabel != null) {
                            statusLabel.setTextFill(Color.LIGHTGREEN);
                            statusLabel.setText("Login Successful!");
                        }
                        
                        try {
                            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/MainDashboard.fxml"));
                            javafx.scene.Parent dashboardRoot = loader.load();
                            javafx.scene.Scene scene = loginButton.getScene();
                            
                            // Set the new Dashboard root
                            scene.setRoot(dashboardRoot);
                            
                            // Resize window for the larger dashboard layout
                            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
                            stage.setWidth(1024);
                            stage.setHeight(768);
                            stage.centerOnScreen();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (statusLabel != null) {
                                statusLabel.setTextFill(Color.TOMATO);
                                statusLabel.setText("Failed to load dashboard.");
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setText("LOGIN");
                    loginButton.setDisable(false);
                    new Shake(loginButton).play();
                    if (statusLabel != null) {
                        statusLabel.setTextFill(Color.TOMATO);
                        statusLabel.setText(e.getMessage());
                    }
                });
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        System.exit(0);
    }
}
