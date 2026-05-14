package com.pms.controllers;

import com.pms.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {

    // Sidebar Controller Injection
    @FXML private SidebarController sidebarController;

    // Main Content
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        
        // Link the sidebar controller to this dashboard controller for navigation callbacks
        if (sidebarController != null) {
            sidebarController.setDashboardController(this);
            sidebarController.updateProfile();
        }

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to the SYNORA " + capitalize(session.getRole()) + " Dashboard!");
        }
    }

    @FXML
    public void handleViewDashboard() {
        if (contentArea != null && welcomeLabel != null) {
            contentArea.getChildren().setAll(welcomeLabel);
        }
    }

    @FXML
    public void handleViewSubjects() {
        loadView("/views/SubjectsView.fxml");
    }

    @FXML
    public void handleViewProjects() {
        loadView("/views/ProjectsView.fxml");
    }

    @FXML
    public void handleViewTeams() {
        loadView("/views/ApplicationsView.fxml");
    }

    @FXML
    public void handleViewNotifications() {
        loadView("/views/NotificationsView.fxml");
    }

    @FXML
    public void handleViewTasks() {
        loadView("/views/TasksView.fxml");
    }

    @FXML
    public void handleViewCreateProject() {
        loadView("/views/CreateProjectView.fxml");
    }

    @FXML
    public void handleLogout() {
        try {
            UserSession.getInstance().clearSession();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/LoginScreen.fxml"));
            javafx.scene.Parent loginRoot = loader.load();
            javafx.scene.Scene scene = contentArea.getScene(); // Use contentArea to get scene
            scene.setRoot(loginRoot);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            
            // Add smooth fade-in animation
            new animatefx.animation.FadeIn(view).setSpeed(1.5).play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String str) {
        if(str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
