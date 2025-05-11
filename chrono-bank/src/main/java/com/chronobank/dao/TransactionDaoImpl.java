package com.chronobank.dao;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.transaction.*;
import com.chronobank.util.IdGenerator;
import org.postgresql.util.PGobject;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDaoImpl implements TransactionDao {
    private final AccountDao accountDao = new AccountDaoImpl(); 

    @Override
    public void saveTransaction(Transaction transaction) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID must be set before saving.");
        }

        String sql = "INSERT INTO transactions (transaction_id, transaction_type, from_account_id, to_account_id, " +
                     "amount, timestamp, status, description, related_investment_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getClass().getSimpleName());
            
            String fromAccountId = null;
            String toAccountId = null;
            String relatedInvestmentId = null;

            if (transaction instanceof TransferTransaction) {
                TransferTransaction tt = (TransferTransaction) transaction;
                if (tt.getFromAccount() != null) fromAccountId = tt.getFromAccount().getAccountId();
                if (tt.getToAccount() != null) toAccountId = tt.getToAccount().getAccountId();
            } else if (transaction instanceof LoanTransaction) {
                LoanTransaction lt = (LoanTransaction) transaction;
                if (lt.getLoanAccount() != null) {
                    if (lt.getLoanTransactionType() == LoanTransaction.LoanTransactionType.DISBURSEMENT) {
                        fromAccountId = lt.getLoanAccount().getAccountId();
                        if (lt.getTargetAccountForDisbursement() != null) {
                            toAccountId = lt.getTargetAccountForDisbursement().getAccountId();
                        }
                    } else { // REPAYMENT
                        toAccountId = lt.getLoanAccount().getAccountId();
                    }
                }
            } else if (transaction instanceof InvestmentTransaction) {
                InvestmentTransaction it = (InvestmentTransaction) transaction;
                if (it.getInvestorAccount() != null) {
                    if (it.getInvestmentTransactionType() == InvestmentTransaction.InvestmentTransactionType.INVEST) {
                        fromAccountId = it.getInvestorAccount().getAccountId();
                    } else if (it.getInvestmentTransactionType() == InvestmentTransaction.InvestmentTransactionType.DIVEST) {
                        toAccountId = it.getInvestorAccount().getAccountId();
                    }
                }
                if (it.getInvestmentDetails() != null) {
                    relatedInvestmentId = it.getInvestmentDetails().getInvestmentId();
                }
            }

            if (fromAccountId != null) pstmt.setString(3, fromAccountId); else pstmt.setNull(3, Types.VARCHAR);
            if (toAccountId != null) pstmt.setString(4, toAccountId); else pstmt.setNull(4, Types.VARCHAR);
            
            pstmt.setDouble(5, transaction.getAmount());
            pstmt.setTimestamp(6, transaction.getTimestamp());
            pstmt.setString(7, transaction.getStatus().name());
            pstmt.setString(8, transaction.getDescription());
            if (relatedInvestmentId != null) pstmt.setString(9, relatedInvestmentId); else pstmt.setNull(9, Types.VARCHAR);

            pstmt.executeUpdate();
            System.out.println("Transaction saved successfully: " + transaction.getTransactionId());

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { 
                System.out.println("Transaction " + transaction.getTransactionId() + " already exists. Attempting update.");
                updateTransaction(transaction); 
            } else {
                System.err.println("Error saving transaction " + transaction.getTransactionId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public Optional<Transaction> findById(String transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToTransaction(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<TransactionRecord> findByAccountId(String accountId) throws SQLException {
        List<TransactionRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(mapRowToTransactionRecord(rs));
            }
        }
        return records;
    }

    @Override
    public List<TransactionRecord> findAll() throws SQLException {
        List<TransactionRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRowToTransactionRecord(rs));
            }
        }
        return records;
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET transaction_type = ?, from_account_id = ?, to_account_id = ?, " +
                     "amount = ?, timestamp = ?, status = ?, description = ?, related_investment_id = ? " +
                     "WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getClass().getSimpleName());
            
            String fromAccountId = null;
            String toAccountId = null;
            String relatedInvestmentId = null;

            if (transaction instanceof TransferTransaction) {
                TransferTransaction tt = (TransferTransaction) transaction;
                if (tt.getFromAccount() != null) fromAccountId = tt.getFromAccount().getAccountId();
                if (tt.getToAccount() != null) toAccountId = tt.getToAccount().getAccountId();
            } else if (transaction instanceof LoanTransaction) {
                LoanTransaction lt = (LoanTransaction) transaction;
                 if (lt.getLoanAccount() != null) {
                    if (lt.getLoanTransactionType() == LoanTransaction.LoanTransactionType.DISBURSEMENT) {
                        fromAccountId = lt.getLoanAccount().getAccountId();
                        if (lt.getTargetAccountForDisbursement() != null) toAccountId = lt.getTargetAccountForDisbursement().getAccountId();
                    } else { 
                        toAccountId = lt.getLoanAccount().getAccountId();
                    }
                }
            } else if (transaction instanceof InvestmentTransaction) {
                InvestmentTransaction it = (InvestmentTransaction) transaction;
                if (it.getInvestorAccount() != null) {
                     if (it.getInvestmentTransactionType() == InvestmentTransaction.InvestmentTransactionType.INVEST) fromAccountId = it.getInvestorAccount().getAccountId();
                     else if (it.getInvestmentTransactionType() == InvestmentTransaction.InvestmentTransactionType.DIVEST) toAccountId = it.getInvestorAccount().getAccountId();
                }
                if (it.getInvestmentDetails() != null) relatedInvestmentId = it.getInvestmentDetails().getInvestmentId();
            }

            if (fromAccountId != null) pstmt.setString(2, fromAccountId); else pstmt.setNull(2, Types.VARCHAR);
            if (toAccountId != null) pstmt.setString(3, toAccountId); else pstmt.setNull(3, Types.VARCHAR);
            
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setTimestamp(5, transaction.getTimestamp());
            pstmt.setString(6, transaction.getStatus().name());
            pstmt.setString(7, transaction.getDescription());
            if (relatedInvestmentId != null) pstmt.setString(8, relatedInvestmentId); else pstmt.setNull(8, Types.VARCHAR);
            pstmt.setString(9, transaction.getTransactionId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Transaction updated successfully: " + transaction.getTransactionId());
            } else {
                System.out.println("Transaction with ID " + transaction.getTransactionId() + " not found for update, or data was identical.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating transaction " + transaction.getTransactionId() + ": " + e.getMessage());
        }
    }

    @Override
    public void saveTransactionRecordToLog(TransactionRecord record) {
        String sql = "INSERT INTO transaction_log (transaction_id, event_description, snapshot_details, logged_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getTransactionId());
            pstmt.setString(2, record.getDescription());
            pstmt.setNull(3, Types.OTHER); 
            pstmt.setTimestamp(4, record.getLoggedAt());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving transaction log record for TX ID " + record.getTransactionId() + ": " + e.getMessage());
        }
    }

    @Override
    public List<TransactionRecord> findTransactionRecordsFromLog(String transactionId) {
        List<TransactionRecord> records = new ArrayList<>();
        String sql = "SELECT tl.transaction_id, tl.logged_at, tl.event_description, t.transaction_type, t.amount, t.status, t.from_account_id, t.to_account_id " +
                     "FROM transaction_log tl JOIN transactions t ON tl.transaction_id = t.transaction_id " +
                     "WHERE tl.transaction_id = ? ORDER BY tl.logged_at DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new TransactionRecord(
                    rs.getString("transaction_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("logged_at"),
                    rs.getString("status"),
                    rs.getString("event_description"),
                    rs.getString("from_account_id"),
                    rs.getString("to_account_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transaction log records for TX ID " + transactionId + ": " + e.getMessage());
        }
        return records;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        String transactionId = rs.getString("transaction_id");
        String transactionType = rs.getString("transaction_type");
        String fromAccountIdStr = rs.getString("from_account_id");
        String toAccountIdStr = rs.getString("to_account_id");
        double amount = rs.getDouble("amount");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        Transaction.TransactionStatus status = Transaction.TransactionStatus.valueOf(rs.getString("status"));
        String description = rs.getString("description");

        Optional<TimeAccount> fromAccountOpt = (fromAccountIdStr != null) ? accountDao.findAccountById(fromAccountIdStr) : Optional.empty();
        Optional<TimeAccount> toAccountOpt = (toAccountIdStr != null) ? accountDao.findAccountById(toAccountIdStr) : Optional.empty();

        Transaction transaction;

        switch (transactionType) {
            case "TransferTransaction":
                transaction = new TransferTransaction(fromAccountOpt.orElse(null), toAccountOpt.orElse(null), amount);
                break;
            
            default:
                System.err.println("mapRowToTransaction: Cannot fully reconstruct specific transaction type: " + transactionType + ". This method needs to be expanded or Transaction hierarchy revised for simple DB loading.");
                
                throw new SQLException("Cannot map to specific transaction type '" + transactionType + "' with current Transaction class structure. mapRowToTransaction needs rework or this specific transaction type is not supported for direct loading via findTransactionById.");
        }
        
        
        transaction.setStatus(status);
        transaction.setDescription(description);

        return transaction; 
    }

    private TransactionRecord mapRowToTransactionRecord(ResultSet rs) throws SQLException {
        return new TransactionRecord(
            rs.getString("transaction_id"),
            rs.getString("transaction_type"),
            rs.getDouble("amount"),
            rs.getTimestamp("timestamp"),
            rs.getString("status"),
            rs.getString("description"),
            rs.getString("from_account_id"),
            rs.getString("to_account_id")
        );
    }
}

