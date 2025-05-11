package com.chronobank.dao;

import com.chronobank.model.admin.Admin;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AdminDao {
    Optional<Admin> findByUsername(String username) throws SQLException;
    Optional<Admin> findById(String adminId) throws SQLException;
    List<Admin> findAll() throws SQLException;
    void save(Admin admin, String passwordHash) throws SQLException;
    void updatePassword(String adminId, String newPasswordHash) throws SQLException;
    void logAdminAction(String adminId, String actionType, String targetUserId, String actionDetails) throws SQLException;
    List<String> getAdminAuditLog(String adminId) throws SQLException;
} 