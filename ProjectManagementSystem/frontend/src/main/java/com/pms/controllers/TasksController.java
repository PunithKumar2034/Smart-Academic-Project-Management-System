package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TasksController {

    @FXML private VBox tasksContainer;

    @FXML
    public void initialize() {
        loadTasks();
    }

    private void loadTasks() {
        new Thread(() -> {
            try {
                // Fetch student deadlines. The DataService method includes project names.
                JsonNode deadlines = DataService.fetchStudentDeadlines();
                Platform.runLater(() -> {
                    tasksContainer.getChildren().clear();
                    if (deadlines.isEmpty()) {
                        Label empty = new Label("No upcoming deadlines.");
                        empty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
                        tasksContainer.getChildren().add(empty);
                        return;
                    }
                    
                    for (JsonNode deadline : deadlines) {
                        tasksContainer.getChildren().add(createTaskCard(deadline));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createTaskCard(JsonNode deadline) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #2D1E32; -fx-background-radius: 10; -fx-border-color: #8D77A8; -fx-border-radius: 10;");

        String title = deadline.path("title").asText();
        String projectName = deadline.path("projects").path("project_name").asText();
        String dueDate = deadline.path("due_date").asText().substring(0, 10);
        
        Label titleLabel = new Label(title + " - " + projectName);
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        titleLabel.setFont(Font.font("System Bold", 16));

        Label dateLabel = new Label("Due: " + dueDate);
        dateLabel.setTextFill(javafx.scene.paint.Color.web("#C4ADDD"));

        card.getChildren().addAll(titleLabel, dateLabel);

        // Check if there are submissions
        JsonNode submissions = deadline.path("submissions");
        if (submissions.isArray() && submissions.size() > 0) {
            Label submittedLabel = new Label("Status: Submitted ✓");
            submittedLabel.setTextFill(javafx.scene.paint.Color.web("#4CAF50"));
            card.getChildren().add(submittedLabel);
        } else {
            HBox submitBox = new HBox(10);
            TextField linkField = new TextField();
            linkField.setPromptText("Enter submission link...");
            linkField.setStyle("-fx-background-color: #1B0E20; -fx-text-fill: #D1C0EC; -fx-border-color: #8D77A8; -fx-border-radius: 5;");
            linkField.setPrefWidth(300);

            Button submitBtn = new Button("Submit");
            submitBtn.setStyle("-fx-background-color: #8D77A8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            submitBtn.setOnAction(e -> {
                String content = linkField.getText();
                if (!content.isEmpty()) {
                    new Thread(() -> {
                        try {
                            // Assume teamId is known or can be fetched. For now hardcode or pass 0 if not needed by DB trigger
                            DataService.submitWork(deadline.path("id").asInt(), 1, content); // FIXME: Need actual team_id
                            Platform.runLater(this::loadTasks);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            });

            submitBox.getChildren().addAll(linkField, submitBtn);
            card.getChildren().add(submitBox);
        }

        return card;
    }
}
