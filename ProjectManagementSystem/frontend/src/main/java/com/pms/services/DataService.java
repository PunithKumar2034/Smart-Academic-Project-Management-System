package com.pms.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.models.UserSession;
import com.pms.utils.ConfigService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class DataService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String getBaseUrl() {
        return ConfigService.getSupabaseUrl() + "/rest/v1";
    }

    private static HttpRequest.Builder getAuthenticatedRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", ConfigService.getSupabaseAnonKey())
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "application/json");
    }

    // --- SUBJECTS ---

    public static JsonNode fetchSubjects() throws Exception {
        String url = getBaseUrl() + "/subjects?select=*&order=subject_name.asc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static boolean addSubject(String name, String description) throws Exception {
        String url = getBaseUrl() + "/subjects";
        String body = mapper.createObjectNode()
                .put("subject_name", name)
                .put("description", description)
                .put("created_by", UserSession.getInstance().getId())
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 201 || response.statusCode() == 200;
    }

    // --- PROJECTS ---

    public static JsonNode fetchProjects() throws Exception {
        String url = getBaseUrl() + "/projects?select=*,subjects(subject_name),users(name)&order=created_at.desc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static boolean createProject(String name, String description, int subjectId, int maxTeamSize) throws Exception {
        String url = getBaseUrl() + "/projects";
        String body = mapper.createObjectNode()
                .put("project_name", name)
                .put("description", description)
                .put("subject_id", subjectId)
                .put("faculty_id", UserSession.getInstance().getId())
                .put("max_team_size", maxTeamSize)
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 201 || response.statusCode() == 200;
    }

    // --- APPLICATIONS ---

    public static boolean applyForProject(int projectId) throws Exception {
        String url = getBaseUrl() + "/project_applications";
        String body = mapper.createObjectNode()
                .put("project_id", projectId)
                .put("student_id", UserSession.getInstance().getId())
                .put("status", "pending")
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 409) {
            throw new Exception("You have already applied for this project!");
        }
        
        return response.statusCode() == 201 || response.statusCode() == 200;
    }

    public static JsonNode fetchApplications() throws Exception {
        // Fetch applications for projects managed by current faculty
        String url = getBaseUrl() + "/project_applications?select=*,projects(project_name,faculty_id),users(name,email)&projects.faculty_id=eq." + UserSession.getInstance().getId();
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static boolean updateApplicationStatus(int applicationId, String status) throws Exception {
        String url = getBaseUrl() + "/project_applications?id=eq." + applicationId;
        String body = mapper.createObjectNode().put("status", status).toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 204 || response.statusCode() == 200;
    }
}
