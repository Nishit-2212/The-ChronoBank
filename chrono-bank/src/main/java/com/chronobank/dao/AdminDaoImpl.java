package com.chronobank.dao;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.model.admin.Admin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDaoImpl implements AdminDao {
    @Override
    public Optional<Admin> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToAdmin(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Admin> findById(String adminId) throws SQLException {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToAdmin(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Admin> findAll() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admins.add(mapRowToAdmin(rs));
            }
        }
        return admins;
    }

    @Override
    public void save(Admin admin, String passwordHash) throws SQLException {
        String sql = "INSERT INTO admins (admin_id, username, password_hash, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, admin.getAdminId());
            stmt.setString(2, admin.getUsername());
            stmt.setString(3, passwordHash);
            stmt.setString(4, admin.getEmail());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updatePassword(String adminId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE admins SET password_hash = ? WHERE admin_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, adminId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void logAdminAction(String adminId, String actionType, String targetUserId, String actionDetails) throws SQLException {
        String sql = "INSERT INTO admin_audit_log (admin_id, action_type, target_user_id, action_details) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminId);
            stmt.setString(2, actionType);
            stmt.setString(3, targetUserId);
            stmt.setString(4, actionDetails);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> getAdminAuditLog(String adminId) throws SQLException {
        List<String> auditLog = new ArrayList<>();
        String sql = "SELECT * FROM admin_audit_log WHERE admin_id = ? ORDER BY performed_at DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String logEntry = String.format("[%s] %s - %s - %s",
                    rs.getTimestamp("performed_at"),
                    rs.getString("action_type"),
                    rs.getString("target_user_id"),
                    rs.getString("action_details"));
                auditLog.add(logEntry);
            }
        }
        return auditLog;
    }

    private Admin mapRowToAdmin(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getString("admin_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getTimestamp("created_at")
        );
    }
} 