package com.pms.controllers;

import com.pms.services.BulkUploadService;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;

public class BulkUploadDialogController {

    @FXML private VBox sheetSelectionBox;
    @FXML private ComboBox<String> sheetSelector;
    @FXML private VBox mappingContainer;
    @FXML private VBox validationContainer;
    @FXML private Button confirmButton;

    private File currentFile;
    private BulkUploadService.MappedData currentData;
    private Runnable onSuccess;

    public void setFile(File file, Runnable onSuccess) {
        this.currentFile = file;
        this.onSuccess = onSuccess;
        processFile(null);
    }

    private void processFile(String sheetName) {
        new Thread(() -> {
            try {
                currentData = BulkUploadService.previewFile(currentFile, sheetName);
                Platform.runLater(() -> {
                    displayMapping();
                    displayValidation();
                    
                    if (currentData.sheets != null && currentData.sheets.size() > 1 && sheetName == null) {
                        sheetSelectionBox.setVisible(true);
                        sheetSelectionBox.setManaged(true);
                        sheetSelector.getItems().setAll(currentData.sheets);
                        sheetSelector.setOnAction(e -> processFile(sheetSelector.getValue()));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayMapping() {
        mappingContainer.getChildren().clear();
        for (Map.Entry<String, String> entry : currentData.columnMapping.entrySet()) {
            Label l = new Label("✓ AI Matched '" + entry.getValue() + "' to database field [" + entry.getKey().toUpperCase() + "]");
            l.setTextFill(javafx.scene.paint.Color.web("#81C784"));
            mappingContainer.getChildren().add(l);
        }
        for (String missing : currentData.missingFields) {
            Label l = new Label("⚠ AI could not find a match for [" + missing.toUpperCase() + "]");
            l.setTextFill(javafx.scene.paint.Color.web("#FFB74D"));
            mappingContainer.getChildren().add(l);
        }
    }

    private void displayValidation() {
        validationContainer.getChildren().clear();
        boolean hasErrors = false;
        
        if (!currentData.missingFields.isEmpty()) {
            hasErrors = true;
            addValidationError("CRITICAL: Missing mandatory columns. Import cannot proceed until headers are fixed.");
        }

        int duplicates = 0;
        int missingDataRows = 0;

        for (Map<String, String> row : currentData.rows) {
            String emailKey = currentData.columnMapping.get("email");
            String nameKey = currentData.columnMapping.get("name");
            
            if (emailKey == null || row.get(emailKey) == null || row.get(emailKey).isEmpty()) {
                missingDataRows++;
            }
            if (nameKey == null || row.get(nameKey) == null || row.get(nameKey).isEmpty()) {
                missingDataRows++;
            }
        }

        if (missingDataRows > 0) {
            addValidationError("GAP FOUND: " + missingDataRows + " rows have missing names or emails.");
            hasErrors = true;
        }

        confirmButton.setDisable(hasErrors);
    }

    private void addValidationError(String msg) {
        Label l = new Label(msg);
        l.setTextFill(javafx.scene.paint.Color.web("#E57373"));
        l.setWrapText(true);
        validationContainer.getChildren().add(l);
    }

    @FXML
    private void handleConfirm() {
        confirmButton.setDisable(true);
        confirmButton.setText("IMPORTING...");
        
        new Thread(() -> {
            try {
                String nameKey = currentData.columnMapping.get("name");
                String emailKey = currentData.columnMapping.get("email");
                String roleKey = currentData.columnMapping.get("role");

                for (Map<String, String> row : currentData.rows) {
                    String name = row.get(nameKey);
                    String email = row.get(emailKey);
                    String role = (roleKey != null && row.get(roleKey) != null) ? row.get(roleKey).toLowerCase() : "student";
                    
                    if (name != null && email != null && !name.isEmpty() && !email.isEmpty()) {
                        DataService.adminCreateUser(email, "Password123!", name, role);
                    }
                }
                Platform.runLater(() -> {
                    if (onSuccess != null) onSuccess.run();
                    handleCancel();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        ((Stage) mappingContainer.getScene().getWindow()).close();
    }
}
