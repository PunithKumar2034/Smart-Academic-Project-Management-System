package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SubjectsController {

    @FXML private FlowPane subjectsContainer;
    @FXML private VBox addSubjectBox;
    @FXML private javafx.scene.control.TextField nameField;
    @FXML private javafx.scene.control.TextField descField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        String role = com.pms.models.UserSession.getInstance().getRole().toLowerCase();
        if (role.equals("admin")) {
            addSubjectBox.setVisible(true);
            addSubjectBox.setManaged(true);
        }
        loadSubjects();
    }

    @FXML
    private void handleAddSubject() {
        String name = nameField.getText();
        String desc = descField.getText();

        if (name.isEmpty() || desc.isEmpty()) {
            statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
            statusLabel.setText("Please fill in both name and description.");
            return;
        }

        new Thread(() -> {
            try {
                boolean success = DataService.addSubject(name, desc);
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setTextFill(javafx.scene.paint.Color.LIGHTGREEN);
                        statusLabel.setText("Subject added!");
                        nameField.clear();
                        descField.clear();
                        loadSubjects();
                    } else {
                        statusLabel.setTextFill(javafx.scene.paint.Color.TOMATO);
                        statusLabel.setText("Failed to add subject.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSubjects() {
        new Thread(() -> {
            try {
                JsonNode subjects = DataService.fetchSubjects();
                Platform.runLater(() -> {
                    subjectsContainer.getChildren().clear();
                    for (JsonNode subject : subjects) {
                        subjectsContainer.getChildren().add(createSubjectCard(subject));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createSubjectCard(JsonNode subject) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");

        Label title = new Label(subject.path("subject_name").asText());
        title.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setWrapText(true);

        Label desc = new Label(subject.path("description").asText());
        desc.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
        desc.setFont(Font.font("System", 13));
        desc.setWrapText(true);
        desc.setPrefHeight(60);

        card.getChildren().addAll(title, desc);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #C4ADDD; -fx-border-width: 1; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: transparent;"));

        return card;
    }
}
