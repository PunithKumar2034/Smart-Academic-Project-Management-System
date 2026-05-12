package com.pms.controllers;

import com.pms.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SidebarController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleEmailLabel;

    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        updateProfile();
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void updateProfile() {
        UserSession session = UserSession.getInstance();
        if (session.getToken() != null) {
            if (userNameLabel != null) userNameLabel.setText(session.getName());
            if (userRoleEmailLabel != null) {
                userRoleEmailLabel.setText(session.getEmail() + " | " + capitalize(session.getRole()));
            }
        }
    }

    @FXML private void handleViewDashboard() { if (dashboardController != null) dashboardController.handleViewDashboard(); }
    @FXML private void handleViewSubjects() { if (dashboardController != null) dashboardController.handleViewSubjects(); }
    @FXML private void handleViewProjects() { if (dashboardController != null) dashboardController.handleViewProjects(); }
    @FXML private void handleViewTeams() { if (dashboardController != null) dashboardController.handleViewTeams(); }
    @FXML private void handleViewCreateProject() { if (dashboardController != null) dashboardController.handleViewCreateProject(); }
    @FXML private void handleLogout() { if (dashboardController != null) dashboardController.handleLogout(); }

    private String capitalize(String str) {
        if(str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
