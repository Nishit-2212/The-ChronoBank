package com.chronobank.model.user;

import com.chronobank.util.IdGenerator;
import java.sql.Timestamp;
import java.util.Objects;

public class User {
    private String userId;
    private String username;
    private String hashedPassword;
    private String email;
    private int reputationScore;
    private int riskScore;
    private Timestamp createdAt;

   
    public User(String username, String hashedPassword, String email) {
        this.userId = IdGenerator.generateUserId();
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.reputationScore = 0;
        this.riskScore = 0;       
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    
    public User(String userId, String username, String hashedPassword, String email, int reputationScore, int riskScore, Timestamp createdAt) {
        this.userId = userId;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.reputationScore = reputationScore;
        this.riskScore = riskScore;
        this.createdAt = createdAt;
    }

   
    public User(String userId, String username, String hashedPassword, String email) {
        this(userId, username, hashedPassword, email, 0, 0, new Timestamp(System.currentTimeMillis())); 
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getEmail() { return email; }
    public int getReputationScore() { return reputationScore; }
    public int getRiskScore() { return riskScore; }
    public Timestamp getCreatedAt() { return createdAt; }

   
    public void setUserId(String userId) { this.userId = userId; } 
    public void setUsername(String username) { this.username = username; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setEmail(String email) { this.email = email; }
    public void setReputationScore(int reputationScore) { this.reputationScore = reputationScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=\'" + userId + "\'," +
                " username=\'" + username + "\'," +
                " email=\'" + email + "\'," +
                " reputationScore=" + reputationScore +
                ", riskScore=" + riskScore +
                ", createdAt=" + createdAt +
                "}";
    }
}

