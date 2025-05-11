package com.chronobank.dao;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.model.account.*;
import com.chronobank.model.user.User;
import com.chronobank.pattern.state.*;
import com.chronobank.pattern.strategy.DynamicInterestRepaymentStrategy;
import com.chronobank.pattern.strategy.FixedTimeRepaymentStrategy;
import com.chronobank.pattern.strategy.LoanRepaymentStrategy;
import com.chronobank.service.TimeMarketService;
import com.google.gson.Gson;
import org.postgresql.util.PGobject; // Added for jsonb handling

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AccountDaoImpl implements AccountDao {
    private final Gson gson = new Gson();
    private final UserDao userDao = new UserDaoImpl();

    @Override
    public void saveAccount(TimeAccount account) throws SQLException { // Added throws SQLException
        if (account.getAccountId() == null || account.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID must be set before saving.");
        }

        String sql = "INSERT INTO accounts (account_id, user_id, account_type, balance, status, preferences_json, creation_date, " +
                     "loan_amount, loan_interest_rate, loan_due_date, loan_repayment_strategy, investor_interest_rate, remaining_loan_principal) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountId());
            pstmt.setString(2, account.getOwner().getUserId());
            pstmt.setString(3, account.getAccountType());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setString(5, account.getAccountStatus().getStatusName());
            
            String preferencesJsonString = gson.toJson(account.getPreferences());
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(preferencesJsonString);
            pstmt.setObject(6, jsonObject);
            
            pstmt.setTimestamp(7, account.getCreationDate());

            if (account instanceof LoanAccount) {
                LoanAccount loanAccount = (LoanAccount) account;
                pstmt.setDouble(8, loanAccount.getLoanAmount());
                pstmt.setDouble(9, loanAccount.getInterestRate());
                pstmt.setTimestamp(10, loanAccount.getDueDate());
                pstmt.setString(11, loanAccount.getRepaymentStrategy() != null ? loanAccount.getRepaymentStrategy().getStrategyName() : null);
                pstmt.setNull(12, Types.DECIMAL); // investor_interest_rate
                pstmt.setDouble(13, loanAccount.getRemainingLoanPrincipal());
            } else if (account instanceof InvestorAccount) {
                InvestorAccount investorAccount = (InvestorAccount) account;
                pstmt.setNull(8, Types.DECIMAL); // loan_amount
                pstmt.setNull(9, Types.DECIMAL); // loan_interest_rate
                pstmt.setNull(10, Types.TIMESTAMP); // loan_due_date
                pstmt.setNull(11, Types.VARCHAR); // loan_repayment_strategy
                pstmt.setDouble(12, investorAccount.getConfiguredInterestRate());
                pstmt.setNull(13, Types.DECIMAL); // remaining_loan_principal
            } else { // BasicTimeAccount
                pstmt.setNull(8, Types.DECIMAL);
                pstmt.setNull(9, Types.DECIMAL);
                pstmt.setNull(10, Types.TIMESTAMP);
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.DECIMAL);
                pstmt.setNull(13, Types.DECIMAL);
            }

            pstmt.executeUpdate();
            System.out.println("Account saved successfully: " + account.getAccountId());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { 
                System.out.println("Account " + account.getAccountId() + " already exists. Attempting update.");
                updateAccount(account); 
            } else {
                System.err.println("Error saving account " + account.getAccountId() + ": " + e.getMessage());
               
                throw e; 
            }
        }
    }

    @Override
    public Optional<TimeAccount> findAccountById(String accountId) {
        String sql = "SELECT *, remaining_loan_principal FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding account by ID " + accountId + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<TimeAccount> findAccountsByUserId(String userId) {
        List<TimeAccount> accounts = new ArrayList<>();
        String sql = "SELECT *, remaining_loan_principal FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                accounts.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding accounts by user ID " + userId + ": " + e.getMessage());
        }
        return accounts;
    }

    @Override
    public List<TimeAccount> findAll() throws SQLException {
        List<TimeAccount> accounts = new ArrayList<>();
        String sql = "SELECT *, remaining_loan_principal FROM accounts";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accounts.add(mapRowToAccount(rs));
            }
        }
        return accounts;
    }

    @Override
    public void updateAccount(TimeAccount account) throws SQLException { // Added throws SQLException
        String sql = "UPDATE accounts SET user_id = ?, account_type = ?, balance = ?, status = ?, preferences_json = ?, " +
                     "loan_amount = ?, loan_interest_rate = ?, loan_due_date = ?, loan_repayment_strategy = ?, investor_interest_rate = ?, remaining_loan_principal = ? " +
                     "WHERE account_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getOwner().getUserId());
            pstmt.setString(2, account.getAccountType());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setString(4, account.getAccountStatus().getStatusName());

            // Handle preferences_json with PGobject for jsonb type
            String preferencesJsonStringUpdate = gson.toJson(account.getPreferences());
            PGobject jsonObjectUpdate = new PGobject();
            jsonObjectUpdate.setType("jsonb");
            jsonObjectUpdate.setValue(preferencesJsonStringUpdate);
            pstmt.setObject(5, jsonObjectUpdate);

            if (account instanceof LoanAccount) {
                LoanAccount loanAccount = (LoanAccount) account;
                pstmt.setDouble(6, loanAccount.getLoanAmount());
                pstmt.setDouble(7, loanAccount.getInterestRate());
                pstmt.setTimestamp(8, loanAccount.getDueDate());
                pstmt.setString(9, loanAccount.getRepaymentStrategy() != null ? loanAccount.getRepaymentStrategy().getStrategyName() : null);
                pstmt.setNull(10, Types.DECIMAL); // investor_interest_rate
                pstmt.setDouble(11, loanAccount.getRemainingLoanPrincipal());
            } else if (account instanceof InvestorAccount) {
                InvestorAccount investorAccount = (InvestorAccount) account;
                pstmt.setNull(6, Types.DECIMAL); // loan_amount
                pstmt.setNull(7, Types.DECIMAL); // loan_interest_rate
                pstmt.setNull(8, Types.TIMESTAMP); // loan_due_date
                pstmt.setNull(9, Types.VARCHAR); // loan_repayment_strategy
                pstmt.setDouble(10, investorAccount.getConfiguredInterestRate());
                pstmt.setNull(11, Types.DECIMAL); // remaining_loan_principal
            } else { // BasicTimeAccount
                pstmt.setNull(6, Types.DECIMAL);
                pstmt.setNull(7, Types.DECIMAL);
                pstmt.setNull(8, Types.TIMESTAMP);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.DECIMAL);
                pstmt.setNull(11, Types.DECIMAL);
            }
            pstmt.setString(12, account.getAccountId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Account updated successfully: " + account.getAccountId());
            } else {
                System.out.println("Account with ID " + account.getAccountId() + " not found for update, or data was identical.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating account " + account.getAccountId() + ": " + e.getMessage());
            // e.printStackTrace(); // Original line
            throw e; // Re-throw the exception
        }
    }

    @Override
    public void deleteAccount(String accountId) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting account " + accountId + ": " + e.getMessage());
        }
    }

    private TimeAccount mapRowToAccount(ResultSet rs) throws SQLException {
        String accountId = rs.getString("account_id");
        String userId = rs.getString("user_id");
        String accountType = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        String statusName = rs.getString("status");
        String preferencesJson = rs.getString("preferences_json");
        Timestamp creationDate = rs.getTimestamp("creation_date");

        User owner = userDao.findUserById(userId)
                .orElseThrow(() -> new SQLException("User not found for account " + accountId + " with user ID " + userId));
        
        AccountPreferences preferences = gson.fromJson(preferencesJson, AccountPreferences.class);
        if (preferences == null) { preferences = new AccountPreferences.Builder().build(); }

        TimeAccount account;

        switch (accountType) {
            case "BASIC":
                account = new BasicTimeAccount(accountId, owner, preferences, balance, creationDate);
                break;
            case "INVESTOR":
                double investorInterestRate = rs.getDouble("investor_interest_rate");
                account = new InvestorAccount(accountId, owner, preferences, balance, "INVESTOR", creationDate, investorInterestRate);
                break;
            case "LOAN":
                double loanAmount = rs.getDouble("loan_amount");
                double loanInterestRate = rs.getDouble("loan_interest_rate");
                Timestamp loanDueDate = rs.getTimestamp("loan_due_date");
                String strategyName = rs.getString("loan_repayment_strategy");
                int termInMonthsPlaceholder = 0; 
                double remainingLoanPrincipal = rs.getDouble("remaining_loan_principal");

                LoanRepaymentStrategy strategy = null;
                if (strategyName != null) {
                    if ("FIXED_TIME_REPAYMENT".equals(strategyName)) strategy = new FixedTimeRepaymentStrategy();
                    else if ("DYNAMIC_INTEREST_REPAYMENT".equals(strategyName)) strategy = new DynamicInterestRepaymentStrategy(TimeMarketService.getInstance());
                }
                account = new LoanAccount(accountId, owner, preferences, balance, "LOAN", creationDate, 
                                          loanAmount, loanInterestRate, termInMonthsPlaceholder, strategy, loanDueDate, remainingLoanPrincipal);
                break;
            default:
                throw new SQLException("Unknown account type: " + accountType);
        }
        
        AccountStatus currentDbStatus;
        switch (statusName) {
            case "ACTIVE": currentDbStatus = new AccountActiveState(); break;
            case "OVERDRAWN": currentDbStatus = new AccountOverdrawnState(); break;
            case "FROZEN": currentDbStatus = new AccountFrozenState(); break;
            default: currentDbStatus = new AccountActiveState(); System.err.println("Unknown status from DB: " + statusName + ". Defaulting to ACTIVE.");
        }
        account.setAccountStatus(currentDbStatus);

        return account;
    }
}

