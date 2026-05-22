package com.pms.controllers;

import com.pms.models.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SidebarController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleEmailLabel;
    @FXML private javafx.scene.control.Button btnNewProject;
    @FXML private javafx.scene.control.Button btnTeams;
    @FXML private javafx.scene.control.Button btnTasks;
    @FXML private javafx.scene.control.Button btnSubjects;
    @FXML private javafx.scene.control.Button btnUsers;

    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        updateProfile();
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
        // Re-call updateProfile if needed to ensure visibility is set after controller is set
        updateProfile();
    }

    public void updateProfile() {
        UserSession session = UserSession.getInstance();
        if (session.getToken() != null) {
            String role = session.getRole().toLowerCase();
            
            if (userNameLabel != null) userNameLabel.setText(session.getName());
            if (userRoleEmailLabel != null) {
                userRoleEmailLabel.setText(session.getEmail() + " | " + capitalize(session.getRole()));
            }

            // Role-based visibility
            if (btnNewProject != null) {
                boolean canCreate = role.equals("admin") || role.equals("faculty");
                btnNewProject.setVisible(canCreate);
                btnNewProject.setManaged(canCreate);
            }

            if (btnTasks != null) {
                boolean isStudent = role.equals("student");
                btnTasks.setVisible(isStudent);
                btnTasks.setManaged(isStudent);
            }

            if (btnTeams != null) {
                boolean canManage = role.equals("admin") || role.equals("faculty");
                btnTeams.setVisible(canManage);
                btnTeams.setManaged(canManage);
            }

            if (btnUsers != null) {
                boolean isAdmin = role.equals("admin");
                btnUsers.setVisible(isAdmin);
                btnUsers.setManaged(isAdmin);
            }
        }
    }

    @FXML private void handleViewDashboard() { if (dashboardController != null) dashboardController.handleViewDashboard(); }
    @FXML private void handleViewSubjects() { if (dashboardController != null) dashboardController.handleViewSubjects(); }
    @FXML private void handleViewProjects() { if (dashboardController != null) dashboardController.handleViewProjects(); }
    @FXML private void handleViewTeams() { if (dashboardController != null) dashboardController.handleViewTeams(); }
    @FXML private void handleViewNotifications() { if (dashboardController != null) dashboardController.handleViewNotifications(); }
    @FXML private void handleViewTasks() { if (dashboardController != null) dashboardController.handleViewTasks(); }
    @FXML private void handleViewUsers() { if (dashboardController != null) dashboardController.handleViewUsers(); }
    @FXML private void handleViewCreateProject() { if (dashboardController != null) dashboardController.handleViewCreateProject(); }
    @FXML private void handleLogout() { if (dashboardController != null) dashboardController.handleLogout(); }

    private String capitalize(String str) {
        if(str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
