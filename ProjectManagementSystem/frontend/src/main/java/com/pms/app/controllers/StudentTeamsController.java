package com.pms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.models.UserSession;
import com.pms.services.DataService;
import com.pms.utils.ConfigService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import javafx.geometry.Insets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StudentTeamsController {

    @FXML private ComboBox<JsonNode> projectFilter;
    @FXML private TextField searchField;
    @FXML private FlowPane teamsFlowPane;
    @FXML private Button btnCreateTeam;

    private JsonNode allTeams;
    private JsonNode allProjects;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
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
                this.allProjects = projects;
                Platform.runLater(() -> {
                    projectFilter.getItems().clear();
                    projectFilter.getItems().add(null);
                    for (JsonNode proj : projects) {
                        // Only add projects that are in student selection mode
                        if ("student".equalsIgnoreCase(proj.path("selection_mode").asText())) {
                            projectFilter.getItems().add(proj);
                        }
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

        String currentStudentId = UserSession.getInstance().getId();

        for (JsonNode team : allTeams) {
            JsonNode proj = team.path("projects");
            String selectionMode = proj.path("selection_mode").asText();
            
            // Only show teams for "student" mode projects
            if (!"student".equalsIgnoreCase(selectionMode)) continue;

            String teamName = team.path("team_name").asText().toLowerCase();
            int projectId = team.path("project_id").asInt();

            if (!searchText.isEmpty() && !teamName.contains(searchText)) continue;
            if (selectedProject != null && selectedProject.path("id").asInt() != projectId) continue;

            // Check if student is already in ANY team for this project
            boolean isAlreadyInAProjectTeam = false;
            for (JsonNode t : allTeams) {
                if (t.path("project_id").asInt() == projectId) {
                    for (JsonNode mem : t.path("team_members")) {
                        if (mem.path("student_id").asText().equals(currentStudentId)) {
                            isAlreadyInAProjectTeam = true;
                            break;
                        }
                    }
                }
                if(isAlreadyInAProjectTeam) break;
            }

            teamsFlowPane.getChildren().add(createTeamCard(team, proj, isAlreadyInAProjectTeam, currentStudentId));
        }
    }

    private VBox createTeamCard(JsonNode team, JsonNode project, boolean isAlreadyInAProjectTeam, String currentStudentId) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: #2D1E32; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.4), 10, 0, 0, 5);");

        Label nameLabel = new Label("🛡 " + team.path("team_name").asText());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#FFFFFF"));

        Label projLabel = new Label("Project: " + project.path("project_name").asText());
        projLabel.setTextFill(Color.web("#A0B4C8"));
        projLabel.setWrapText(true);

        JsonNode members = team.path("team_members");
        int currentSize = members.size();
        int maxSize = project.path("max_team_size").asInt();
        int remaining = maxSize - currentSize;

        Label sizeLabel = new Label("Slots: " + currentSize + " / " + maxSize);
        sizeLabel.setTextFill(Color.web("#C4ADDD"));

        // Progress Bar
        ProgressBar progressBar = new ProgressBar((double) currentSize / maxSize);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #5C8EB8;");

        // Determine if current user is in THIS specific team
        boolean isInThisTeam = false;
        for (JsonNode mem : members) {
            if (mem.path("student_id").asText().equals(currentStudentId)) {
                isInThisTeam = true;
                break;
            }
        }

        // Action Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button joinBtn = new Button(isInThisTeam ? "Joined" : (remaining <= 0 ? "Full" : "Join Team"));
        joinBtn.setPrefWidth(100);
        joinBtn.setStyle("-fx-background-color: " + (isInThisTeam ? "#4E9F76" : (remaining <= 0 ? "#D9534F" : "#5C8EB8")) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        joinBtn.setDisable(isAlreadyInAProjectTeam || remaining <= 0);

        if (!joinBtn.isDisabled() && !isInThisTeam) {
            joinBtn.setOnAction(e -> handleJoinTeam(team.path("id").asInt(), team.path("team_name").asText(), joinBtn));
        }

        Button detailsBtn = new Button("Details");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A0B4C8; -fx-border-color: #A0B4C8; -fx-border-radius: 6; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> showTeamDetails(team, project));

        buttonBox.getChildren().addAll(joinBtn, detailsBtn);

        card.getChildren().addAll(nameLabel, projLabel, sizeLabel, progressBar, buttonBox);

        // Hover Animations
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #382A3D; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(92, 142, 184, 0.4), 15, 0, 0, 0);");
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #2D1E32; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.4), 10, 0, 0, 5);");
            card.setTranslateY(0);
        });

        return card;
    }

    private void handleJoinTeam(int teamId, String teamName, Button sourceBtn) {
        sourceBtn.setText("Joining...");
        sourceBtn.setDisable(true);
        new Thread(() -> {
            try {
                String addMemberUrl = ConfigService.getSupabaseUrl() + "/rest/v1/team_members";
                String memberBody = mapper.createObjectNode()
                        .put("team_id", teamId)
                        .put("student_id", UserSession.getInstance().getId())
                        .toString();

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(addMemberUrl))
                    .header("apikey", ConfigService.getSupabaseAnonKey())
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(memberBody))
                    .build();

                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() == 201 || res.statusCode() == 200) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully joined " + teamName + "!");
                        alert.show();
                        loadTeams(); // Refresh to update logic
                    });
                } else {
                    throw new Exception(res.body());
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to join team: " + e.getMessage());
                    alert.show();
                    sourceBtn.setText("Join Team");
                    sourceBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void showTeamDetails(JsonNode team, JsonNode project) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Team Details");
        alert.setHeaderText(team.path("team_name").asText() + " - " + project.path("project_name").asText());

        StringBuilder sb = new StringBuilder();
        sb.append("Maximum Team Size: ").append(project.path("max_team_size").asInt()).append("\n");
        sb.append("Current Members:\n");
        for (JsonNode mem : team.path("team_members")) {
            sb.append("• ").append(mem.path("users").path("name").asText()).append("\n");
        }
        if (team.path("team_members").size() == 0) {
            sb.append("(No members yet)\n");
        }

        alert.setContentText(sb.toString());
        alert.show();
    }

    @FXML
    private void handleCreateNewTeam() {
        JsonNode proj = projectFilter.getValue();
        if (proj == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please filter by a specific project first to create a team for it.");
            alert.show();
            return;
        }

        int projectId = proj.path("id").asInt();
        String currentStudentId = UserSession.getInstance().getId();
        
        // Prevent creating a new team if they are already in one
        if (allTeams != null) {
            for (JsonNode t : allTeams) {
                if (t.path("project_id").asInt() == projectId) {
                    for (JsonNode mem : t.path("team_members")) {
                        if (mem.path("student_id").asText().equals(currentStudentId)) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "You are already in a team for this project!");
                            alert.show();
                            return;
                        }
                    }
                }
            }
        }

        TextInputDialog dialog = new TextInputDialog("My Awesome Team");
        dialog.setTitle("Create New Team");
        dialog.setHeaderText("Create a new team for " + proj.path("project_name").asText());
        dialog.setContentText("Please enter your team name:");

        dialog.showAndWait().ifPresent(teamName -> {
            new Thread(() -> {
                try {
                    // Create team
                    String createTeamUrl = ConfigService.getSupabaseUrl() + "/rest/v1/teams";
                    String teamBody = mapper.createObjectNode()
                            .put("project_id", projectId)
                            .put("team_name", teamName)
                            .put("status", "forming")
                            .toString();

                    HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(createTeamUrl))
                        .header("apikey", ConfigService.getSupabaseAnonKey())
                        .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=representation")
                        .POST(HttpRequest.BodyPublishers.ofString(teamBody))
                        .build();

                    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() != 201 && res.statusCode() != 200) throw new Exception(res.body());

                    JsonNode createdTeam = mapper.readTree(res.body());
                    int teamId = createdTeam.get(0).get("id").asInt();

                    // Join team immediately
                    String addMemberUrl = ConfigService.getSupabaseUrl() + "/rest/v1/team_members";
                    String memberBody = mapper.createObjectNode()
                            .put("team_id", teamId)
                            .put("student_id", currentStudentId)
                            .toString();

                    HttpRequest req2 = HttpRequest.newBuilder()
                        .uri(URI.create(addMemberUrl))
                        .header("apikey", ConfigService.getSupabaseAnonKey())
                        .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(memberBody))
                        .build();
                    client.send(req2, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Team created and joined successfully!");
                        alert.show();
                        loadTeams();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create team: " + e.getMessage());
                        alert.show();
                    });
                }
            }).start();
        });
    }
}
