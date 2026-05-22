package com.pms.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.models.UserSession;
import com.pms.utils.ConfigService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    public static JsonNode fetchTeams() throws Exception {
        String url = getBaseUrl() + "/teams?select=*,projects(project_name,max_team_size,selection_mode),team_members(student_id,users(name))&order=created_at.desc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static JsonNode createProject(String name, String description, int subjectId, int maxTeamSize, String selectionMode) throws Exception {
        String url = getBaseUrl() + "/projects";
        String body = mapper.createObjectNode()
                .put("project_name", name)
                .put("description", description)
                .put("subject_id", subjectId)
                .put("faculty_id", UserSession.getInstance().getId())
                .put("max_team_size", maxTeamSize)
                .put("selection_mode", selectionMode)
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return mapper.readTree(response.body()).get(0);
        }
        return null;
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
        String role = UserSession.getInstance().getRole().toLowerCase();
        String url = getBaseUrl() + "/project_applications?select=*,projects(project_name,faculty_id),users:student_id(name,email)";
        
        if (!role.equals("admin")) {
            url += "&projects.faculty_id=eq." + UserSession.getInstance().getId();
        }
        
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

    // --- NOTIFICATIONS ---

    public static JsonNode fetchNotifications() throws Exception {
        String url = getBaseUrl() + "/notifications?user_id=eq." + UserSession.getInstance().getId() + "&order=created_at.desc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static boolean markNotificationAsRead(int notificationId) throws Exception {
        String url = getBaseUrl() + "/notifications?id=eq." + notificationId;
        String body = mapper.createObjectNode().put("is_read", true).toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 204 || response.statusCode() == 200;
    }

    // --- DEADLINES & SUBMISSIONS ---

    public static boolean createDeadline(int projectId, String title, String dueDate, int reminderDays) throws Exception {
        String url = getBaseUrl() + "/deadlines";
        String body = mapper.createObjectNode()
                .put("project_id", projectId)
                .put("title", title)
                .put("due_date", dueDate)
                .put("reminder_days_before", reminderDays)
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 201 || response.statusCode() == 200;
    }

    public static JsonNode fetchDeadlines(int projectId) throws Exception {
        String url = getBaseUrl() + "/deadlines?project_id=eq." + projectId + "&order=due_date.asc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }
    
    public static JsonNode fetchStudentDeadlines() throws Exception {
        String url = getBaseUrl() + "/deadlines?select=*,projects(project_name),submissions(id,submitted_at,grade)&order=due_date.asc";
        HttpRequest request = getAuthenticatedRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    public static boolean submitWork(int deadlineId, int teamId, String content) throws Exception {
        String url = getBaseUrl() + "/submissions";
        String body = mapper.createObjectNode()
                .put("deadline_id", deadlineId)
                .put("team_id", teamId)
                .put("student_id", UserSession.getInstance().getId())
                .put("content", content)
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 201 || response.statusCode() == 200;
    }

    // --- ADMIN USER MANAGEMENT ---

    public static boolean adminCreateUser(String email, String password, String name, String role) throws Exception {
        String url = ConfigService.getSupabaseUrl() + "/rest/v1/rpc/admin_create_user";
        String body = mapper.createObjectNode()
                .put("p_email", email)
                .put("p_password", password)
                .put("p_name", name)
                .put("p_role", role)
                .toString();

        HttpRequest request = getAuthenticatedRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 || response.statusCode() == 201;
    }
}
