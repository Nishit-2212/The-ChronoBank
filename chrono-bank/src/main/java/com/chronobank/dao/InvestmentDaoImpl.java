package com.chronobank.dao;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.model.common.Investment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDaoImpl implements InvestmentDao {
    
    @Override
    public void saveInvestment(Investment investment) throws SQLException {
        String sql = "INSERT INTO investments (investment_id, account_id, investment_type, amount_invested, " +
                    "start_date, maturity_date, current_value, status, last_updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, investment.getInvestmentId());
            pstmt.setString(2, investment.getAccountId());
            pstmt.setString(3, investment.getInvestmentType());
            pstmt.setDouble(4, investment.getAmountInvested());
            pstmt.setTimestamp(5, investment.getStartDate());
            pstmt.setTimestamp(6, investment.getMaturityDate());
            pstmt.setDouble(7, investment.getCurrentValue());
            pstmt.setString(8, investment.getStatus());
            pstmt.setTimestamp(9, investment.getLastUpdatedAt());
            
            pstmt.executeUpdate();
            System.out.println("Investment saved successfully: " + investment.getInvestmentId());
        }
    }
    
    @Override
    public List<Investment> findByAccountId(String accountId) throws SQLException {
        List<Investment> investments = new ArrayList<>();
        String sql = "SELECT * FROM investments WHERE account_id = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Investment investment = new Investment(
                    rs.getString("account_id"),
                    rs.getString("investment_type"),
                    rs.getDouble("amount_invested"),
                    rs.getDouble("current_value"),
                    rs.getTimestamp("start_date"),
                    rs.getTimestamp("maturity_date")
                );
                investment.setInvestmentId(rs.getString("investment_id"));
                investment.setStatus(rs.getString("status"));
                investment.setLastUpdatedAt(rs.getTimestamp("last_updated_at"));
                investments.add(investment);
            }
        }
        return investments;
    }
    
    @Override
    public Investment findById(String investmentId) throws SQLException {
        String sql = "SELECT * FROM investments WHERE investment_id = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, investmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Investment investment = new Investment(
                    rs.getString("account_id"),
                    rs.getString("investment_type"),
                    rs.getDouble("amount_invested"),
                    rs.getDouble("current_value"),
                    rs.getTimestamp("start_date"),
                    rs.getTimestamp("maturity_date")
                );
                investment.setInvestmentId(rs.getString("investment_id"));
                investment.setStatus(rs.getString("status"));
                investment.setLastUpdatedAt(rs.getTimestamp("last_updated_at"));
                return investment;
            }
        }
        return null;
    }
    
    @Override
    public void updateInvestment(Investment investment) throws SQLException {
        String sql = "UPDATE investments SET account_id = ?, investment_type = ?, amount_invested = ?, " +
                    "start_date = ?, maturity_date = ?, current_value = ?, status = ?, last_updated_at = ? " +
                    "WHERE investment_id = ?";
                    
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, investment.getAccountId());
            pstmt.setString(2, investment.getInvestmentType());
            pstmt.setDouble(3, investment.getAmountInvested());
            pstmt.setTimestamp(4, investment.getStartDate());
            pstmt.setTimestamp(5, investment.getMaturityDate());
            pstmt.setDouble(6, investment.getCurrentValue());
            pstmt.setString(7, investment.getStatus());
            pstmt.setTimestamp(8, investment.getLastUpdatedAt());
            pstmt.setString(9, investment.getInvestmentId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Investment updated successfully: " + investment.getInvestmentId());
            } else {
                System.out.println("Investment with ID " + investment.getInvestmentId() + " not found for update.");
            }
        }
    }
    
    @Override
    public void deleteInvestment(String investmentId) throws SQLException {
        String sql = "DELETE FROM investments WHERE investment_id = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, investmentId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Investment deleted successfully: " + investmentId);
            } else {
                System.out.println("Investment with ID " + investmentId + " not found for deletion.");
            }
        }
    }
} 