package com.chronobank.dao;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.model.user.User;
import com.chronobank.util.IdGenerator; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    @Override
    public void saveUser(User user) {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId(IdGenerator.generateUniqueId());
        }
        if (findUserById(user.getUserId()).isPresent()) {
            updateUser(user);
            return;
        }

        String sql = "INSERT INTO users (user_id, username, hashed_password, email, reputation_score, risk_score, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getHashedPassword());
            pstmt.setString(4, user.getEmail());
            pstmt.setInt(5, user.getReputationScore());
            pstmt.setInt(6, user.getRiskScore());
            pstmt.setTimestamp(7, user.getCreatedAt() != null ? user.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            
            pstmt.executeUpdate();
            System.out.println("User saved successfully: " + user.getUsername());
        } catch (SQLException e) {
            System.err.println("Error saving user " + user.getUsername() + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID " + userId + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username " + username + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email " + email + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET username = ?, hashed_password = ?, email = ?, reputation_score = ?, risk_score = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getReputationScore());
            pstmt.setInt(5, user.getRiskScore());
            pstmt.setString(6, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User updated successfully: " + user.getUsername());
            } else {
                System.out.println("User with ID " + user.getUserId() + " not found for update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user " + user.getUsername() + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User deleted successfully: " + userId);
            } else {
                System.out.println("User with ID " + userId + " not found for deletion.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user " + userId + ": " + e.getMessage());
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void updatePassword(String userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET hashed_password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateReputationScore(String userId, int newScore) throws SQLException {
        // Update users table
        String usersSql = "UPDATE users SET reputation_score = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(usersSql)) {
            stmt.setInt(1, newScore);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }

        // Update user_reputation table
        String reputationSql = "UPDATE user_reputation SET reputation_score = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(reputationSql)) {
            stmt.setInt(1, newScore);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<User> findById(String userId) throws SQLException {
        return findUserById(userId);
    }

    @Override
    public void save(User user) throws SQLException {
        saveUser(user);
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        return findUserByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        return findUserByEmail(email);
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("user_id"),
            rs.getString("username"),
            rs.getString("hashed_password"),
            rs.getString("email")
        );
        user.setReputationScore(rs.getInt("reputation_score"));
        user.setRiskScore(rs.getInt("risk_score"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}

