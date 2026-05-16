package com.pms.controllers;

import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsersController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleSelector;
    @FXML private Button addButton;
    @FXML private Label statusLabel;
    @FXML private Label bulkStatusLabel;

    @FXML
    public void initialize() {
        roleSelector.getItems().addAll("faculty", "student");
    }

    @FXML
    private void handleBulkUpload() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Open Data File");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Spreadsheets", "*.csv", "*.xlsx")
        );
        java.io.File file = fileChooser.showOpenDialog(addButton.getScene().getWindow());

        if (file != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/BulkUploadDialog.fxml"));
                javafx.scene.Parent root = loader.load();
                BulkUploadDialogController controller = loader.getController();
                
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Bulk User Import - AI Assistant");
                stage.setScene(new javafx.scene.Scene(root));
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                
                controller.setFile(file, () -> {
                    bulkStatusLabel.setTextFill(javafx.scene.paint.Color.LIGHTGREEN);
                    bulkStatusLabel.setText("Bulk import completed successfully!");
                });
                
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                bulkStatusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                bulkStatusLabel.setText("Error opening file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleSelector.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
            statusLabel.setText("Password must be at least 6 characters.");
            return;
        }

        addButton.setDisable(true);
        addButton.setText("CREATING...");

        new Thread(() -> {
            try {
                boolean success = DataService.adminCreateUser(email, password, name, role);
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setTextFill(javafx.scene.paint.Color.LIGHTGREEN);
                        statusLabel.setText("User account created successfully!");
                        nameField.clear();
                        emailField.clear();
                        passwordField.clear();
                        roleSelector.getSelectionModel().clearSelection();
                    } else {
                        statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                        statusLabel.setText("Failed to create user. Email may already exist.");
                    }
                    addButton.setDisable(false);
                    addButton.setText("CREATE USER ACCOUNT");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                    statusLabel.setText("Error: " + e.getMessage());
                    addButton.setDisable(false);
                    addButton.setText("CREATE USER ACCOUNT");
                });
            }
        }).start();
    }
}
