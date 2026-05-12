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

    @FXML
    public void initialize() {
        loadSubjects();
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
        card.getStyleClass().add("card"); // Uses the .card style from style.css

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
        
        // Hover animation logic can be added here
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #C4ADDD; -fx-border-width: 1; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: transparent;"));

        return card;
    }
}
