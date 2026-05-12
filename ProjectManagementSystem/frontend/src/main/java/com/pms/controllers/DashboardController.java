package com.pms.controllers;

import com.pms.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    // Sidebar references
    @FXML private Button btnNewProject;
    @FXML private Button btnDashboard;
    @FXML private Button btnSubjects;
    @FXML private Button btnProjects;
    @FXML private Button btnTeams;
    @FXML private Button btnNotifications;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleEmailLabel;

    // Main Content
    @FXML private javafx.scene.layout.StackPane contentArea;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        
        if (userNameLabel != null) userNameLabel.setText(session.getName());
        if (userRoleEmailLabel != null) userRoleEmailLabel.setText(session.getEmail() + " | " + capitalize(session.getRole()));

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to the SYNORA " + capitalize(session.getRole()) + " Dashboard!");
        }

        configureRolePermissions(session.getRole());
        setupNavigation();
    }

    private void setupNavigation() {
        if (btnSubjects != null) btnSubjects.setOnAction(e -> loadView("/views/SubjectsView.fxml"));
        if (btnProjects != null) btnProjects.setOnAction(e -> loadView("/views/ProjectsView.fxml"));
        if (btnDashboard != null) btnDashboard.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(welcomeLabel);
        });
    }

    private void loadView(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureRolePermissions(String role) {
        if (role.equalsIgnoreCase("student")) {
            // Students cannot create projects or manage subjects
            if (btnNewProject != null) { btnNewProject.setVisible(false); btnNewProject.setManaged(false); }
            if (btnSubjects != null) { btnSubjects.setVisible(false); btnSubjects.setManaged(false); }
        }
    }

    private String capitalize(String str) {
        if(str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
