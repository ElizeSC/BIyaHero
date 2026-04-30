package com.biyahero.model;

public class User {
    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String role;

    public User(int userId, String fullName, String username, String password, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}