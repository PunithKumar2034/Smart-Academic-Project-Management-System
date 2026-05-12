package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProjectsController {

    @FXML private FlowPane projectsContainer;

    @FXML
    public void initialize() {
        loadProjects();
    }

    private void loadProjects() {
        new Thread(() -> {
            try {
                JsonNode projects = DataService.fetchProjects();
                Platform.runLater(() -> {
                    projectsContainer.getChildren().clear();
                    for (JsonNode project : projects) {
                        projectsContainer.getChildren().add(createProjectCard(project));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createProjectCard(JsonNode project) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("card");

        Label title = new Label(project.path("project_name").asText());
        title.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setWrapText(true);

        Label subject = new Label(project.path("subjects").path("subject_name").asText());
        subject.setTextFill(javafx.scene.paint.Color.web("#C4ADDD"));
        subject.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label desc = new Label(project.path("description").asText());
        desc.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
        desc.setFont(Font.font("System", 13));
        desc.setWrapText(true);
        desc.setPrefHeight(60);

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label faculty = new Label("By: " + project.path("users").path("name").asText());
        faculty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
        faculty.setFont(Font.font("System", 11));

        Label teamSize = new Label("Max Team: " + project.path("max_team_size").asText());
        teamSize.setStyle("-fx-background-color: #1B0E20; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        teamSize.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        teamSize.setFont(Font.font("System", 10));

        footer.getChildren().addAll(faculty, new javafx.scene.layout.Region() {{ HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS); }}, teamSize);

        card.getChildren().addAll(subject, title, desc, footer);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #C4ADDD; -fx-border-width: 1; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: transparent;"));

        return card;
    }
}
