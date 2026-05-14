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
        
        // Student Application Button
        if (com.pms.models.UserSession.getInstance().getRole().equalsIgnoreCase("student")) {
            javafx.scene.control.Button btnApply = new javafx.scene.control.Button("APPLY NOW");
            btnApply.getStyleClass().add("modern-button");
            btnApply.setPrefWidth(280);
            btnApply.setPrefHeight(35);
            btnApply.setStyle("-fx-font-size: 11px; -fx-background-color: #8D77A8;");
            
            btnApply.setOnAction(e -> handleApply(project.path("id").asInt(), btnApply));
            card.getChildren().add(btnApply);
        }

        // Deadlines Button
        javafx.scene.control.Button btnDeadlines = new javafx.scene.control.Button("VIEW DEADLINES");
        btnDeadlines.getStyleClass().add("modern-button");
        btnDeadlines.setPrefWidth(280);
        btnDeadlines.setPrefHeight(30);
        btnDeadlines.setStyle("-fx-font-size: 10px; -fx-background-color: transparent; -fx-border-color: #8D77A8; -fx-text-fill: #8D77A8;");
        
        VBox deadlinesContainer = new VBox(5);
        deadlinesContainer.setManaged(false);
        deadlinesContainer.setVisible(false);

        btnDeadlines.setOnAction(e -> {
            if (deadlinesContainer.isVisible()) {
                deadlinesContainer.setVisible(false);
                deadlinesContainer.setManaged(false);
            } else {
                deadlinesContainer.setVisible(true);
                deadlinesContainer.setManaged(true);
                deadlinesContainer.getChildren().clear();
                Label loading = new Label("Loading...");
                loading.setTextFill(javafx.scene.paint.Color.web("#C4ADDD"));
                deadlinesContainer.getChildren().add(loading);

                new Thread(() -> {
                    try {
                        JsonNode deadlines = DataService.fetchDeadlines(project.path("id").asInt());
                        Platform.runLater(() -> {
                            deadlinesContainer.getChildren().clear();
                            if (deadlines.isEmpty()) {
                                Label empty = new Label("No deadlines.");
                                empty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
                                deadlinesContainer.getChildren().add(empty);
                            } else {
                                for (JsonNode d : deadlines) {
                                    Label dLabel = new Label("- " + d.path("title").asText() + " (" + d.path("due_date").asText().substring(0, 10) + ")");
                                    dLabel.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
                                    dLabel.setFont(Font.font("System", 11));
                                    deadlinesContainer.getChildren().add(dLabel);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        card.getChildren().addAll(btnDeadlines, deadlinesContainer);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #C4ADDD; -fx-border-width: 1; -fx-border-radius: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-border-color: transparent;"));

        return card;
    }

    private void handleApply(int projectId, javafx.scene.control.Button btn) {
        btn.setDisable(true);
        btn.setText("APPLYING...");
        
        new Thread(() -> {
            try {
                boolean success = DataService.applyForProject(projectId);
                Platform.runLater(() -> {
                    if (success) {
                        btn.setText("APPLIED ✓");
                        btn.setStyle("-fx-background-color: #4CAF50;");
                    } else {
                        btn.setDisable(false);
                        btn.setText("FAILED");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("ALREADY APPLIED");
                    btn.setStyle("-fx-background-color: #E57373;");
                });
            }
        }).start();
    }
}
