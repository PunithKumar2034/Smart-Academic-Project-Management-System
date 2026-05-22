package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox; 
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class NotificationsController {

    @FXML private VBox notificationsContainer;

    @FXML
    public void initialize() {
        loadNotifications();
    }

    private void loadNotifications() {
        new Thread(() -> {
            try {
                JsonNode notifications = DataService.fetchNotifications();
                Platform.runLater(() -> {
                    notificationsContainer.getChildren().clear();
                    for (JsonNode n : notifications) {
                        notificationsContainer.getChildren().add(createNotificationCard(n));
                    }
                    if (notificationsContainer.getChildren().isEmpty()) {
                        Label empty = new Label("No notifications yet.");
                        empty.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
                        notificationsContainer.getChildren().add(empty);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createNotificationCard(JsonNode n) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        
        boolean isRead = n.path("is_read").asBoolean();
        String bgColor = isRead ? "#3A2A42" : "#4A3A52";
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-border-color: " + (isRead ? "transparent" : "#8D77A8") + "; -fx-border-radius: 10;");

        Label msg = new Label(n.path("message").asText());
        msg.setTextFill(javafx.scene.paint.Color.web("#D1C0EC"));
        msg.setWrapText(true);
        msg.setFont(Font.font("System", 14));

        Label date = new Label(n.path("created_at").asText().substring(0, 10));
        date.setTextFill(javafx.scene.paint.Color.web("#8D77A8"));
        date.setFont(Font.font("System", 10));

        card.getChildren().addAll(msg, date);
        
        if (!isRead) {
            card.setOnMouseClicked(e -> {
                new Thread(() -> {
                    try {
                        DataService.markNotificationAsRead(n.path("id").asInt());
                        Platform.runLater(this::loadNotifications);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });
        }

        return card;
    }
}
