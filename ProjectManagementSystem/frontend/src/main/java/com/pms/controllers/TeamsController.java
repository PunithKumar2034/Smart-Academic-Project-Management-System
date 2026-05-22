package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.services.AutoTeamService;
import com.pms.services.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

public class TeamsController {

    @FXML private ComboBox<JsonNode> projectFilter;
    @FXML private TextField searchField;
    @FXML private FlowPane teamsFlowPane;
    @FXML private Button btnAutoForm;

    private JsonNode allTeams;

    @FXML
    public void initialize() {
        // Setup Project Filter
        projectFilter.setConverter(new StringConverter<JsonNode>() {
            @Override
            public String toString(JsonNode node) {
                return node == null ? "All Projects" : node.path("project_name").asText();
            }
            @Override
            public JsonNode fromString(String string) { return null; }
        });

        projectFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterTeams());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTeams());

        loadProjects();
        loadTeams();
    }

    private void loadProjects() {
        new Thread(() -> {
            try {
                JsonNode projects = DataService.fetchProjects();
                Platform.runLater(() -> {
                    projectFilter.getItems().clear();
                    projectFilter.getItems().add(null); // Option for 'All Projects'
                    for (JsonNode proj : projects) {
                        projectFilter.getItems().add(proj);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadTeams() {
        new Thread(() -> {
            try {
                allTeams = DataService.fetchTeams();
                Platform.runLater(this::filterTeams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void filterTeams() {
        if (allTeams == null) return;

        teamsFlowPane.getChildren().clear();
        String searchText = searchField.getText().toLowerCase();
        JsonNode selectedProject = projectFilter.getValue();

        for (JsonNode team : allTeams) {
            String teamName = team.path("team_name").asText().toLowerCase();
            JsonNode proj = team.path("projects");
            int projectId = team.path("project_id").asInt();

            if (!searchText.isEmpty() && !teamName.contains(searchText)) continue;
            if (selectedProject != null && selectedProject.path("id").asInt() != projectId) continue;

            teamsFlowPane.getChildren().add(createTeamCard(team, proj));
        }
    }

    private VBox createTeamCard(JsonNode team, JsonNode project) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: #44334A; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 5);");

        Label nameLabel = new Label(team.path("team_name").asText());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#D1C0EC"));

        Label projLabel = new Label(project.path("project_name").asText());
        projLabel.setTextFill(Color.web("#8D77A8"));

        JsonNode members = team.path("team_members");
        int currentSize = members.size();
        int maxSize = project.path("max_team_size").asInt();

        Label sizeLabel = new Label("Strength: " + currentSize + "/" + maxSize);
        sizeLabel.setTextFill(Color.web("#C4ADDD"));

        String status = team.path("status").asText();
        Label statusLabel = new Label("Status: " + status.toUpperCase());
        statusLabel.setTextFill(status.equals("active") ? Color.LIGHTGREEN : Color.web("#8D77A8"));

        card.getChildren().addAll(nameLabel, projLabel, sizeLabel, statusLabel);

        // Add Members List
        if (currentSize > 0) {
            Label memTitle = new Label("Members:");
            memTitle.setTextFill(Color.WHITE);
            memTitle.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
            card.getChildren().add(memTitle);

            for (JsonNode member : members) {
                String mName = member.path("users").path("name").asText();
                Label ml = new Label("• " + mName);
                ml.setTextFill(Color.web("#A0B4C8"));
                card.getChildren().add(ml);
            }
        }

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #55445B; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(196, 173, 221, 0.3), 15, 0, 0, 0); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #44334A; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 5);"));

        return card;
    }

    @FXML
    private void handleAutoFormTeams() {
        JsonNode selectedProject = projectFilter.getValue();
        if (selectedProject == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a specific project from the filter first.");
            alert.show();
            return;
        }

        String mode = selectedProject.path("selection_mode").asText();
        if (!"auto".equals(mode)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "This project is not set to Auto Team Formation mode.");
            alert.show();
            return;
        }

        btnAutoForm.setDisable(true);
        btnAutoForm.setText("Forming Teams...");

        new Thread(() -> {
            try {
                int projectId = selectedProject.path("id").asInt();
                int maxSize = selectedProject.path("max_team_size").asInt();
                
                boolean success = AutoTeamService.autoFormTeams(projectId, maxSize);
                Platform.runLater(() -> {
                    if (success) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Teams formed and notifications sent successfully!");
                        alert.show();
                        loadTeams(); // Refresh UI
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage());
                    alert.show();
                });
            } finally {
                Platform.runLater(() -> {
                    btnAutoForm.setDisable(false);
                    btnAutoForm.setText("Form Teams (Auto)");
                });
            }
        }).start();
    }
}
