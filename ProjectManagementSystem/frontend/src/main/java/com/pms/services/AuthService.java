package com.pms.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pms.utils.ConfigService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean login(String email, String password, String expectedRole) {
        try {
            String url = ConfigService.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
            
            ObjectNode jsonBody = mapper.createObjectNode();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", ConfigService.getSupabaseAnonKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseNode = mapper.readTree(response.body());
                String token = responseNode.path("access_token").asText();
                String userId = responseNode.path("user").path("id").asText();

                // Fetch profile from public.users
                String profileUrl = ConfigService.getSupabaseUrl() + "/rest/v1/users?id=eq." + userId + "&select=*";
                HttpRequest profileReq = HttpRequest.newBuilder()
                        .uri(URI.create(profileUrl))
                        .header("apikey", ConfigService.getSupabaseAnonKey())
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();
                HttpResponse<String> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofString());
                JsonNode profileArray = mapper.readTree(profileRes.body());

                if (profileArray.isArray() && profileArray.size() > 0) {
                    JsonNode profile = profileArray.get(0);
                    String actualRole = profile.path("role").asText();
                    String name = profile.path("name").asText();

                    // Role Matching Logic
                    if (expectedRole.equalsIgnoreCase("Faculty") && !actualRole.equals("faculty") && !actualRole.equals("admin")) {
                        System.err.println("Role mismatch! Expected Faculty but got " + actualRole);
                        return false;
                    }
                    if (expectedRole.equalsIgnoreCase("Student") && !actualRole.equals("student")) {
                        System.err.println("Role mismatch! Expected Student but got " + actualRole);
                        return false;
                    }

                    com.pms.models.UserSession.getInstance().setSession(token, userId, email, name, actualRole);
                    return true;
                } else {
                    System.err.println("Profile not found in public.users table.");
                    return false;
                }
            } else {
                JsonNode errorNode = mapper.readTree(response.body());
                System.err.println("Login Failed: " + errorNode.path("error_description").asText());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Login Error Exception: " + e.getMessage());
            return false;
        }
    }
}
