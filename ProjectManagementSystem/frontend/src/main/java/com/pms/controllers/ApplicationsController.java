package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ApplicationsController {

    @FXML private VBox applicationsContainer;

    @FXML
    public void initialize() {
        loadApplications();
    }

    private void loadApplications() {
        new Thread(() -> {
            try {
                JsonNode apps = DataService.fetchApplications();
                Platform.runLater(() -> {
                    applicationsContainer.getChildren().clear();
                    for (JsonNode app : apps) {
                        // PostgREST returns nested objects. We need to filter nulls if join failed
                        if (!app.path("projects").isMissingNode()) {
                            applicationsContainer.getChildren().add(createApplicationRow(app));
                        }
                    }
                    if (applicationsContainer.getChildren().isEmpty()) {
                        Label empty = new Label("No pending applications found.");
                        empty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
                        applicationsContainer.getChildren().add(empty);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createApplicationRow(JsonNode app) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.getStyleClass().add("card");
        row.setStyle("-fx-background-color: #44334A; -fx-background-radius: 8;");

        VBox studentInfo = new VBox(5);
        Label name = new Label(app.path("users").path("name").asText());
        name.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        name.setFont(Font.font("System", FontWeight.BOLD, 15));
        
        Label project = new Label("Applied for: " + app.path("projects").path("project_name").asText());
        project.setTextFill(javafx.scene.paint.Color.web("#C4ADDD"));
        project.setFont(Font.font("System", 13));
        
        studentInfo.getChildren().addAll(name, project);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(app.path("status").asText().toUpperCase());
        status.setStyle("-fx-background-color: #1B0E20; -fx-padding: 5 10 5 10; -fx-background-radius: 15;");
        status.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        status.setFont(Font.font("System", FontWeight.BOLD, 10));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (app.path("status").asText().equals("pending")) {
            Button btnApprove = new Button("APPROVE");
            btnApprove.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");
            btnApprove.setOnAction(e -> handleStatusUpdate(app.path("id").asInt(), "approved", row));

            Button btnReject = new Button("REJECT");
            btnReject.setStyle("-fx-background-color: #E57373; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");
            btnReject.setOnAction(e -> handleStatusUpdate(app.path("id").asInt(), "rejected", row));

            actions.getChildren().addAll(btnApprove, btnReject);
        }

        row.getChildren().addAll(studentInfo, spacer, status, actions);
        return row;
    }

    private void handleStatusUpdate(int appId, String status, HBox row) {
        new Thread(() -> {
            try {
                boolean success = DataService.updateApplicationStatus(appId, status);
                if (success) {
                    Platform.runLater(this::loadApplications);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
