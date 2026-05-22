package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.models.UserSession;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import animatefx.animation.FadeInUp;
import java.util.HashSet;
import java.util.Set;

public class DashboardHomeController {

    @FXML private Label welcomeLabel;
    @FXML private VBox activityContainer;
    
    @FXML private HBox statsContainer;
    @FXML private VBox speedometerBox;
    @FXML private StackPane gaugePane;
    @FXML private Label gaugeLabel;
    @FXML private VBox capacityBox;
    @FXML private VBox capacityContainer;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome back, " + UserSession.getInstance().getName() + "!");
        loadRecentActivity();
        
        if ("faculty".equalsIgnoreCase(UserSession.getInstance().getRole())) {
            loadFacultyStats();
        } else {
            // Hide faculty widgets for other roles
            statsContainer.setVisible(false);
            statsContainer.setManaged(false);
        }
    }
    
    private void loadFacultyStats() {
        new Thread(() -> {
            try {
                int totalStudents = DataService.fetchTotalStudentsCount();
                JsonNode applications = DataService.fetchApplications();
                JsonNode projects = DataService.fetchProjects(); // need to filter for faculty projects
                
                Set<String> uniqueApplicants = new HashSet<>();
                for (JsonNode app : applications) {
                    uniqueApplicants.add(app.path("student_id").asText());
                }
                int appliedStudents = uniqueApplicants.size();
                
                Platform.runLater(() -> {
                    renderGauge(appliedStudents, totalStudents);
                    renderCapacity(projects);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> gaugeLabel.setText("Error loading"));
            }
        }).start();
    }
    
    private void renderGauge(int applied, int total) {
        double percentage = total == 0 ? 0 : (double) applied / total;
        gaugeLabel.setText(applied + " / " + total);
        
        Arc backgroundArc = new Arc(100, 100, 80, 80, 0, 180);
        backgroundArc.setType(ArcType.OPEN);
        backgroundArc.setStroke(Color.web("#3A2A42"));
        backgroundArc.setStrokeWidth(15);
        backgroundArc.setStrokeLineCap(StrokeLineCap.ROUND);
        backgroundArc.setFill(Color.TRANSPARENT);
        
        Arc valueArc = new Arc(100, 100, 80, 80, 180, 0); // startAngle 180, length goes negative for clockwise
        valueArc.setType(ArcType.OPEN);
        valueArc.setStroke(Color.web("#85A1C1"));
        valueArc.setStrokeWidth(15);
        valueArc.setStrokeLineCap(StrokeLineCap.ROUND);
        valueArc.setFill(Color.TRANSPARENT);
        
        gaugePane.getChildren().addAll(backgroundArc, valueArc);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(valueArc.lengthProperty(), 0)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(valueArc.lengthProperty(), -180 * percentage))
        );
        timeline.play();
    }
    
    private void renderCapacity(JsonNode projects) {
        capacityContainer.getChildren().clear();
        String myFacultyId = UserSession.getInstance().getId();
        int count = 0;
        
        for (JsonNode proj : projects) {
            if (!proj.path("faculty_id").asText().equals(myFacultyId)) continue;
            
            VBox item = new VBox(5);
            Label nameLabel = new Label(proj.path("project_name").asText());
            nameLabel.setTextFill(Color.web("#C4ADDD"));
            
            // For now just simulate capacity based on max_team_size, in a real app we'd fetch team members count
            int max = proj.path("max_team_size").asInt();
            ProgressBar pb = new ProgressBar(0.5); // hardcoded for UI demo
            pb.setPrefWidth(200);
            pb.setStyle("-fx-accent: #5C8EB8;");
            
            item.getChildren().addAll(nameLabel, pb);
            capacityContainer.getChildren().add(item);
            new FadeInUp(item).setDelay(Duration.millis(100 * count)).play();
            count++;
        }
        
        if (count == 0) {
            Label noProj = new Label("No projects yet.");
            noProj.setTextFill(Color.web("#8D77A8"));
            capacityContainer.getChildren().add(noProj);
        }
    }

    private void loadRecentActivity() {
        new Thread(() -> {
            try {
                JsonNode notifications = DataService.fetchNotifications();
                Platform.runLater(() -> {
                    activityContainer.getChildren().clear();
                    int count = 0;
                    for (JsonNode n : notifications) {
                        if (count >= 10) break; // Only show last 10
                        VBox item = createActivityItem(n);
                        activityContainer.getChildren().add(item);
                        new FadeInUp(item).setDelay(Duration.millis(100 * count)).play();
                        count++;
                    }
                    
                    if (activityContainer.getChildren().isEmpty()) {
                        Label empty = new Label("No recent activity to show.");
                        empty.setTextFill(Color.web("#8D77A8"));
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
        msg.setTextFill(Color.web("#D1C0EC"));
        msg.setFont(Font.font("System", 14));
        msg.setWrapText(true);

        String timeStr = n.path("created_at").asText();
        if (timeStr.length() > 16) timeStr = timeStr.substring(0, 10) + " " + timeStr.substring(11, 16);
        
        Label date = new Label(timeStr);
        date.setTextFill(Color.web("#8D77A8"));
        date.setFont(Font.font("System", 10));

        item.getChildren().addAll(msg, date);
        return item;
    }
}
