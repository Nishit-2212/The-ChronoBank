package com.chronobank.model.admin;

import java.sql.Timestamp;

public class Admin {
    private final String adminId;
    private final String username;
    private final String email;
    private final Timestamp createdAt;

    public Admin(String adminId, String username, String email, Timestamp createdAt) {
        this.adminId = adminId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 