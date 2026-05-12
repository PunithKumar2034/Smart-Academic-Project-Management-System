package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class CreateProjectController {

    @FXML private TextField nameField;
    @FXML private ComboBox<JsonNode> subjectSelector;
    @FXML private TextArea descArea;
    @FXML private Spinner<Integer> teamSizeSpinner;
    @FXML private Button submitButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        teamSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4));
        loadSubjects();
        
        // Setup ComboBox to display subject names
        subjectSelector.setConverter(new StringConverter<JsonNode>() {
            @Override
            public String toString(JsonNode node) {
                return node == null ? "" : node.path("subject_name").asText();
            }
            @Override
            public JsonNode fromString(String string) {
                return null; // Not needed
            }
        });
    }

    private void loadSubjects() {
        new Thread(() -> {
            try {
                JsonNode subjects = DataService.fetchSubjects();
                Platform.runLater(() -> {
                    subjectSelector.getItems().clear();
                    for (JsonNode subject : subjects) {
                        subjectSelector.getItems().add(subject);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSubmit() {
        String name = nameField.getText();
        String desc = descArea.getText();
        JsonNode subject = subjectSelector.getValue();
        int teamSize = teamSizeSpinner.getValue();

        if (name.isEmpty() || desc.isEmpty() || subject == null) {
            statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        submitButton.setDisable(true);
        submitButton.setText("SUBMITTING...");

        new Thread(() -> {
            try {
                boolean success = DataService.createProject(name, desc, subject.path("id").asInt(), teamSize);
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setTextFill(javafx.scene.paint.Color.LIGHTGREEN);
                        statusLabel.setText("Project created successfully!");
                        nameField.clear();
                        descArea.clear();
                        subjectSelector.getSelectionModel().clearSelection();
                    } else {
                        statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                        statusLabel.setText("Failed to create project.");
                    }
                    submitButton.setDisable(false);
                    submitButton.setText("SUBMIT PROJECT");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                    statusLabel.setText("Error: " + e.getMessage());
                    submitButton.setDisable(false);
                });
            }
        }).start();
    }
}
