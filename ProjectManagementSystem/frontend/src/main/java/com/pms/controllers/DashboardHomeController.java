package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.models.UserSession;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardHomeController {

    @FXML private Label welcomeLabel;
    @FXML private VBox activityContainer;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome back, " + UserSession.getInstance().getName() + "!");
        loadRecentActivity();
    }

    private void loadRecentActivity() {
        new Thread(() -> {
            try {
                // We use the notifications table as our "Activity Feed"
                JsonNode notifications = DataService.fetchNotifications();
                Platform.runLater(() -> {
                    activityContainer.getChildren().clear();
                    int count = 0;
                    for (JsonNode n : notifications) {
                        if (count >= 10) break; // Only show last 10
                        activityContainer.getChildren().add(createActivityItem(n));
                        count++;
                    }
                    
                    if (activityContainer.getChildren().isEmpty()) {
                        Label empty = new Label("No recent activity to show.");
                        empty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
                        activityContainer.getChildren().add(empty);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createActivityItem(JsonNode n) {
        VBox item = new VBox(5);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #2D1E32; -fx-background-radius: 10; -fx-border-color: #3A2A42; -fx-border-radius: 10;");

        Label msg = new Label(n.path("message").asText());
        msg.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        msg.setFont(Font.font("System", 14));
        msg.setWrapText(true);

        String timeStr = n.path("created_at").asText();
        if (timeStr.length() > 16) timeStr = timeStr.substring(0, 10) + " " + timeStr.substring(11, 16);
        
        Label date = new Label(timeStr);
        date.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
        date.setFont(Font.font("System", 10));

        item.getChildren().addAll(msg, date);
        return item;
    }
}
