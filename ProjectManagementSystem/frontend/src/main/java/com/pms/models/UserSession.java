package com.pms.models;

public class UserSession {
    private static UserSession instance;
    
    private String token;
    private String id;
    private String email;
    private String name;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setSession(String token, String id, String email, String name, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public void clearSession() {
        this.token = null;
        this.id = null;
        this.email = null;
        this.name = null;
        this.role = null;
    }

    public String getToken() { return token; }
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
