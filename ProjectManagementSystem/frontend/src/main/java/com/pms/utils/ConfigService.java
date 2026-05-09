package com.pms.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConfigService {
    private static String supabaseUrl;
    private static String supabaseAnonKey;

    static {
        try {
            // Read from root directory config folder
            File configFile = new File("config/auth.json");
            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(configFile);
                supabaseUrl = root.path("supabaseUrl").asText();
                supabaseAnonKey = root.path("supabaseAnonKey").asText();
            } else {
                System.err.println("auth.json not found in config directory!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSupabaseUrl() { return supabaseUrl; }
    public static String getSupabaseAnonKey() { return supabaseAnonKey; }
}
