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

    public static boolean login(String email, String password, String expectedRole) throws Exception {
        System.out.println("DEBUG: Attempting login for " + email + " as " + expectedRole);
        
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
        System.out.println("DEBUG: Auth Response Code: " + response.statusCode());

        if (response.statusCode() == 200) {
            JsonNode responseNode = mapper.readTree(response.body());
            String token = responseNode.path("access_token").asText();
            String userId = responseNode.path("user").path("id").asText();
            System.out.println("DEBUG: Auth Success. UserID: " + userId);

            // Fetch profile from public.users
            String profileUrl = ConfigService.getSupabaseUrl() + "/rest/v1/users?id=eq." + userId + "&select=*";
            System.out.println("DEBUG: Fetching profile from: " + profileUrl);
            
            HttpRequest profileReq = HttpRequest.newBuilder()
                    .uri(URI.create(profileUrl))
                    .header("apikey", ConfigService.getSupabaseAnonKey())
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
                    
            HttpResponse<String> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofString());
            System.out.println("DEBUG: Profile Response Code: " + profileRes.statusCode());
            System.out.println("DEBUG: Profile Body: " + profileRes.body());
            
            JsonNode profileArray = mapper.readTree(profileRes.body());

            if (profileArray.isArray() && profileArray.size() > 0) {
                JsonNode profile = profileArray.get(0);
                String actualRole = profile.path("role").asText();
                String name = profile.path("name").asText();
                System.out.println("DEBUG: Found Role: " + actualRole + ", Name: " + name);

                // Role Matching Logic
                if (expectedRole.equalsIgnoreCase("Faculty") && !actualRole.equalsIgnoreCase("faculty") && !actualRole.equalsIgnoreCase("admin")) {
                    throw new Exception("Role mismatch! Registered as " + actualRole);
                }
                if (expectedRole.equalsIgnoreCase("Student") && !actualRole.equalsIgnoreCase("student")) {
                    throw new Exception("Role mismatch! Registered as " + actualRole);
                }

                com.pms.models.UserSession.getInstance().setSession(token, userId, email, name, actualRole);
                return true;
            } else {
                System.err.println("DEBUG: Profile array empty or not found.");
                throw new Exception("Profile not found in users table.");
            }
        } else {
            System.err.println("DEBUG: Auth Error Body: " + response.body());
            JsonNode errorNode = mapper.readTree(response.body());
            String errorMsg = errorNode.path("error_description").asText();
            if (errorMsg == null || errorMsg.isEmpty()) errorMsg = errorNode.path("message").asText();
            if (errorMsg == null || errorMsg.isEmpty()) errorMsg = errorNode.path("error").asText();
            System.err.println("DEBUG: Auth Error: " + errorMsg);
            throw new Exception(errorMsg);
        }
    }
}
