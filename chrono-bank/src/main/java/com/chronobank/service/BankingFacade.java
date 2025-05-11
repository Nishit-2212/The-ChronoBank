package com.chronobank.service;

import com.chronobank.dao.AccountDao;
import com.chronobank.dao.AccountDaoImpl;
import com.chronobank.dao.AdminDao;
import com.chronobank.dao.AdminDaoImpl;
import com.chronobank.dao.TransactionDao;
import com.chronobank.dao.TransactionDaoImpl;
import com.chronobank.dao.UserDao;
import com.chronobank.dao.UserDaoImpl;
import com.chronobank.dao.InvestmentDao;
import com.chronobank.dao.InvestmentDaoImpl;
import com.chronobank.db.DatabaseConnector; 
import com.chronobank.model.account.*; 
import com.chronobank.model.common.Investment;
import com.chronobank.model.transaction.*;
import com.chronobank.model.user.User;
import com.chronobank.pattern.decorator.BonusTimeDecorator;
import com.chronobank.pattern.decorator.TimeTaxDecorator;
import com.chronobank.pattern.strategy.LoanRepaymentStrategy;
import com.chronobank.pattern.state.AccountStatus;
import com.chronobank.pattern.state.AccountActiveState;
import com.chronobank.pattern.state.AccountFrozenState;
import com.chronobank.pattern.state.AccountOverdrawnState;
import com.chronobank.util.IdGenerator;



import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class BankingFacade {
    private final UserDao userDao;
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final InvestmentDao investmentDao;
    private final AccountFactory accountFactory; 
    private final ChronoLedger chronoLedger;
    private final NotificationService notificationService;
    private final FraudDetectionService fraudDetectionService;
    private final AccessControlService accessControlService;
    private final TimeMarketService timeMarketService; 
    private final AdminDao adminDao;

   
    private final Map<String, List<Timestamp>> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long ATTEMPT_WINDOW_MS = 10 * 60 * 1000; 

    public BankingFacade() {
        this.userDao = new UserDaoImpl();
        this.accountDao = new AccountDaoImpl();
        this.transactionDao = new TransactionDaoImpl();
        this.investmentDao = new InvestmentDaoImpl();
        this.accountFactory = new AccountFactory(); 
        this.chronoLedger = ChronoLedger.getInstance();
        this.notificationService = new NotificationService();
        this.fraudDetectionService = new FraudDetectionService();
        this.accessControlService = new AccessControlService();
        this.timeMarketService = TimeMarketService.getInstance();
        this.adminDao = new AdminDaoImpl(); 
    }

   
    public User registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty() || email == null || !email.contains("@")) {
            System.err.println("Facade: Invalid user registration details.");
            return null;
        }
        if (userDao.findUserByUsername(username).isPresent()) {
            System.err.println("Facade: Username already exists: " + username);
            return null;
        }
        if (userDao.findUserByEmail(email).isPresent()) {
            System.err.println("Facade: Email already registered: " + email);
            return null;
        }

        String hashedPassword = "hashed_" + password; // Placeholder
       
        User newUser = new User(username, hashedPassword, email);
        userDao.saveUser(newUser);
        System.out.println("Facade: User registered successfully: " + username);
        return newUser;
    }

    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOpt = userDao.findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
           
            if (isUserFrozenDueToFailedAttempts(username)) {
                System.err.println("Facade: Account is frozen due to multiple failed login attempts. Please contact support.");
                return Optional.empty();
            }
            
            if (user.getHashedPassword().equals("hashed_" + password)) {
               
                failedLoginAttempts.remove(username);
                System.out.println("Facade: User login successful: " + username);
                return Optional.of(user);
            } else {
                
                recordFailedLoginAttempt(username);
                System.err.println("Facade: Invalid username or password for: " + username);
                return Optional.empty();
            }
        }
       
        recordFailedLoginAttempt(username);
        System.err.println("Facade: Invalid username or password for: " + username);
        return Optional.empty();
    }

    private void recordFailedLoginAttempt(String username) {
        List<Timestamp> attempts = failedLoginAttempts.computeIfAbsent(username, k -> new ArrayList<>());
        attempts.add(new Timestamp(System.currentTimeMillis()));
        
        
        long cutoffTime = System.currentTimeMillis() - ATTEMPT_WINDOW_MS;
        attempts.removeIf(timestamp -> timestamp.getTime() < cutoffTime);
        
       
        if (attempts.size() >= MAX_FAILED_ATTEMPTS) {
            freezeUserAccounts(username);
        }
    }

    private boolean isUserFrozenDueToFailedAttempts(String username) {
        List<Timestamp> attempts = failedLoginAttempts.get(username);
        if (attempts == null) return false;
        
        
        long cutoffTime = System.currentTimeMillis() - ATTEMPT_WINDOW_MS;
        attempts.removeIf(timestamp -> timestamp.getTime() < cutoffTime);
        
        return attempts.size() >= MAX_FAILED_ATTEMPTS;
    }

    private void freezeUserAccounts(String username) {
        try {
            Optional<User> userOpt = userDao.findUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<TimeAccount> accounts = findAccountsByUserId(user.getUserId());
                
                for (TimeAccount account : accounts) {
                    updateAccountStatus(account, "FROZEN");
                   
                    System.out.println("Facade: Account " + account.getAccountId() + " frozen due to multiple failed login attempts");
                }
                
               
                if (adminDao != null) {
                    adminDao.logAdminAction("SYSTEM", "SECURITY_FREEZE", user.getUserId(), 
                        "Account frozen due to " + MAX_FAILED_ATTEMPTS + " failed login attempts within " + 
                        (ATTEMPT_WINDOW_MS / 60000) + " minutes");
                }
            }
        } catch (SQLException e) {
            System.err.println("Facade: Error freezing accounts for user " + username + ": " + e.getMessage());
        }
    }

    public Optional<User> findUserById(String userId) {
        return userDao.findUserById(userId);
    }

   
    public TimeAccount createAccount(User owner, AccountFactory.AccountType type, AccountPreferences preferences, double initialDeposit, Object... additionalParams) throws SQLException {
        if (owner == null) {
            System.err.println("Facade: Owner cannot be null for account creation.");
            return null;
        }
        if (!accessControlService.canOpenAccountType(owner, type.name())) {
            System.err.println("Facade: User " + owner.getUsername() + " not permitted to open account type " + type.name());
            return null;
        }

        TimeAccount account = accountFactory.createTimeAccount(type, owner, preferences, initialDeposit, additionalParams);
        if (account != null) {
            account.addObserver(notificationService);
            account.addObserver(fraudDetectionService);
            try {
                accountDao.saveAccount(account);
               
            } catch (SQLException e) {
               
                throw e; 
            }

            System.out.println("Facade: Account created successfully: " + account.getAccountId() + " for user " + owner.getUsername());
        }
        return account;
    }

    public Optional<TimeAccount> findAccountById(String accountId) throws SQLException {
        Optional<TimeAccount> accountOpt = accountDao.findAccountById(accountId);
        accountOpt.ifPresent(acc -> {
            acc.addObserver(notificationService);
            acc.addObserver(fraudDetectionService);
        });
        return accountOpt;
    }

    public List<TimeAccount> findAccountsByUserId(String userId) throws SQLException {
        List<TimeAccount> accounts = accountDao.findAccountsByUserId(userId);
        accounts.forEach(acc -> {
            acc.addObserver(notificationService);
            acc.addObserver(fraudDetectionService);
        });
        return accounts;
    }

   
    public boolean performTransfer(TimeAccount fromAccount, TimeAccount toAccount, double amount, String description) {
        if (fromAccount == null || toAccount == null) {
            System.err.println("Facade: Source or destination account is null for transfer.");
            return false;
        }
        if (fromAccount.getAccountId().equals(toAccount.getAccountId())){
            System.err.println("Facade: Cannot transfer to the same account.");
            return false;
        }
       
        if (fromAccount.getPreferences() != null && amount > fromAccount.getPreferences().getTransactionLimitPerDay()) {
            System.err.println("Facade: Transfer amount of " + amount + " exceeds the daily transaction limit of " + fromAccount.getPreferences().getTransactionLimitPerDay() + " for account " + fromAccount.getAccountId());
            return false;
        }

        Transaction transferTx = new TransferTransaction(fromAccount, toAccount, amount);
        if (description != null) transferTx.setDescription(description);

        if (!accessControlService.canPerformTransaction(fromAccount.getOwner(), transferTx)) {
            System.err.println("Facade: Transfer denied by access control for user " + fromAccount.getOwner().getUsername());
            return false;
        }

        try {
            transferTx.execute();
            if (transferTx.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                transactionDao.saveTransaction(transferTx);
                chronoLedger.recordTransaction(transferTx, true);
                accountDao.updateAccount(fromAccount);
                accountDao.updateAccount(toAccount);
                System.out.println("Facade: Transfer successful: " + transferTx.getTransactionId());
                return true;
            } else {
                System.err.println("Facade: Transfer failed. Status: " + transferTx.getStatus() + ", Reason: " + transferTx.getDescription());
                chronoLedger.recordTransaction(transferTx, false);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Facade: Exception during transfer: " + e.getMessage());
            transferTx.setStatus(Transaction.TransactionStatus.FAILED);
            if (transferTx.getDescription() == null || transferTx.getDescription().isEmpty()) {
                transferTx.setDescription("Transfer failed due to system error: " + e.getMessage());
            } else {
                transferTx.setDescription(transferTx.getDescription() + " | System error: " + e.getMessage());
            }
            chronoLedger.recordTransaction(transferTx, false);
            return false;
        }
    }

    public boolean performDeposit(TimeAccount account, double amount, String description) {
        if (account == null) {
            System.err.println("Facade: Account is null for deposit.");
            return false;
        }
        try {
            account.deposit(amount);
            accountDao.updateAccount(account);
            System.out.println("Facade: Deposit of " + amount + " to account " + account.getAccountId() + " successful.");
            return true;
        } catch (Exception e) {
            System.err.println("Facade: Exception during deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean performWithdrawal(TimeAccount account, double amount, String description) {
        if (account == null) {
            System.err.println("Facade: Account is null for withdrawal.");
            return false;
        }
        // Check daily transaction limit
        if (account.getPreferences() != null && amount > account.getPreferences().getTransactionLimitPerDay()) {
            System.err.println("Facade: Withdrawal amount of " + amount + " exceeds the daily transaction limit of " + account.getPreferences().getTransactionLimitPerDay() + " for account " + account.getAccountId());
            return false;
        }
        try {
            account.withdraw(amount);
            accountDao.updateAccount(account);
            System.out.println("Facade: Withdrawal of " + amount + " from account " + account.getAccountId() + " successful.");
            return true;
        } catch (IllegalStateException e) {
            System.err.println("Facade: Withdrawal failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Facade: Exception during withdrawal: " + e.getMessage());
            return false;
        }
    }
    
   
    public boolean disburseLoan(LoanAccount loanAccount, TimeAccount targetAccount, double amount) {
        if (loanAccount == null || targetAccount == null) {
            System.err.println("Facade: Loan account or target account is null for disbursement.");
            return false;
        }
        if (amount <= 0 || amount > loanAccount.getLoanAmount() || amount > loanAccount.getRemainingLoanPrincipal()) {
            System.err.println("Facade: Invalid loan disbursement amount.");
            return false;
        }

        LoanTransaction loanTx = new LoanTransaction(loanAccount, LoanTransaction.LoanTransactionType.DISBURSEMENT, amount, targetAccount);
        
        if (!accessControlService.canPerformTransaction(loanAccount.getOwner(), loanTx)) {
            System.err.println("Facade: Loan disbursement denied by access control for user " + loanAccount.getOwner().getUsername());
            return false;
        }

        try {
            loanTx.execute();
            if (loanTx.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                transactionDao.saveTransaction(loanTx);
                chronoLedger.recordTransaction(loanTx, true);
                accountDao.updateAccount(loanAccount);
                accountDao.updateAccount(targetAccount);
                System.out.println("Facade: Loan disbursement successful: " + loanTx.getTransactionId());
                return true;
            } else {
                System.err.println("Facade: Loan disbursement failed. Status: " + loanTx.getStatus());
                chronoLedger.recordTransaction(loanTx, false);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Facade: Exception during loan disbursement: " + e.getMessage());
            loanTx.setStatus(Transaction.TransactionStatus.FAILED);
            chronoLedger.recordTransaction(loanTx, false);
            return false;
        }
    }

    public boolean makeLoanRepayment(LoanAccount loanAccount, TimeAccount sourceAccount, double amount) {
        if (loanAccount == null || sourceAccount == null) {
            System.err.println("Facade: Loan account or source account is null for repayment.");
            return false;
        }
        if (amount <= 0) {
            System.err.println("Facade: Repayment amount must be positive.");
            return false;
        }

        LoanTransaction loanTx = new LoanTransaction(loanAccount, LoanTransaction.LoanTransactionType.REPAYMENT, amount, sourceAccount);

        if (!accessControlService.canPerformTransaction(sourceAccount.getOwner(), loanTx)) {
            System.err.println("Facade: Loan repayment denied by access control for user " + sourceAccount.getOwner().getUsername());
            return false;
        }

        try {
            loanTx.execute();
            if (loanTx.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                transactionDao.saveTransaction(loanTx);
                chronoLedger.recordTransaction(loanTx, true);
                accountDao.updateAccount(loanAccount);
                accountDao.updateAccount(sourceAccount);
                System.out.println("Facade: Loan repayment successful: " + loanTx.getTransactionId());
                return true;
            } else {
                System.err.println("Facade: Loan repayment failed. Status: " + loanTx.getStatus());
                chronoLedger.recordTransaction(loanTx, false);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Facade: Exception during loan repayment: " + e.getMessage());
            loanTx.setStatus(Transaction.TransactionStatus.FAILED);
            chronoLedger.recordTransaction(loanTx, false);
            return false;
        }
    }

   
    public boolean makeInvestment(InvestorAccount investorAccount, String investmentType, double amount) {
        if (investorAccount == null) {
            System.err.println("Facade: Investor account is null for investment.");
            return false;
        }
        Investment investment = new Investment(investorAccount.getAccountId(), investmentType, amount, amount, new Timestamp(System.currentTimeMillis()), null);
       
        InvestmentTransaction investmentTx = new InvestmentTransaction(investorAccount, InvestmentTransaction.InvestmentTransactionType.INVEST, amount, investment);

        if (!accessControlService.canPerformTransaction(investorAccount.getOwner(), investmentTx)) {
            System.err.println("Facade: Investment denied by access control for user " + investorAccount.getOwner().getUsername());
            return false;
        }
        
        try {
            investmentTx.execute();
            if (investmentTx.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                // Save the investment to the database
                investmentDao.saveInvestment(investment);
                // Save the transaction
                transactionDao.saveTransaction(investmentTx);
                chronoLedger.recordTransaction(investmentTx, true);
                accountDao.updateAccount(investorAccount);
                System.out.println("Facade: Investment successful: " + investmentTx.getTransactionId());
                return true;
            } else {
                System.err.println("Facade: Investment failed. Status: " + investmentTx.getStatus());
                chronoLedger.recordTransaction(investmentTx, false);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Facade: Exception during investment: " + e.getMessage());
            investmentTx.setStatus(Transaction.TransactionStatus.FAILED);
            chronoLedger.recordTransaction(investmentTx, false);
            return false;
        }
    }

    
    public List<TransactionRecord> getTransactionHistory(String accountId) throws SQLException {
        return transactionDao.findByAccountId(accountId);
    }

    public List<TransactionRecord> getAllSystemTransactions() {
        return chronoLedger.getAllTransactions();
    }

    public void closeDatabaseConnection() {
        DatabaseConnector.closeConnection();
    }

    // admin related  methods
    public List<User> getAllUsers() throws SQLException {
        return userDao.findAll();
    }

    public List<TransactionRecord> getAllTransactions() throws SQLException {
        return transactionDao.findAll();
    }

    public List<TimeAccount> getAllAccounts() throws SQLException {
        return accountDao.findAll();
    }

    public void changeUserPassword(String userId, String newPassword) throws SQLException {
        userDao.updatePassword(userId, newPassword);
    }

    public void changeUserReputation(String userId, int newScore) throws SQLException {
        userDao.updateReputationScore(userId, newScore);
    }

    public boolean updateAccountStatus(TimeAccount account, String newStatus) {
        if (account == null) {
            System.err.println("Facade: Account is null for status update.");
            return false;
        }

        try {
           
            AccountStatus status;
            switch (newStatus.toUpperCase()) {
                case "ACTIVE":
                    status = new AccountActiveState();
                    break;
                case "FROZEN":
                    status = new AccountFrozenState();
                    break;
                case "OVERDRAWN":
                    status = new AccountOverdrawnState();
                    break;
                default:
                    System.err.println("Facade: Invalid account status: " + newStatus);
                    return false;
            }
            
            account.setAccountStatus(status);
            
            accountDao.updateAccount(account);
            
            account.notifyObservers("STATUS_CHANGE", "Account status changed to " + newStatus);
            
            System.out.println("Facade: Account " + account.getAccountId() + " status updated to " + newStatus);
            return true;
        } catch (Exception e) {
            System.err.println("Facade: Exception during account status update: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUserAccount(String userId) throws SQLException {
        Optional<User> userOpt = userDao.findUserById(userId);
        if (!userOpt.isPresent()) {
            System.err.println("Facade: User not found for deletion: " + userId);
            return false;
        }

        // Get all accounts for the user
        List<TimeAccount> userAccounts = findAccountsByUserId(userId);
        
        // Delete all accounts first
        for (TimeAccount account : userAccounts) {
            accountDao.deleteAccount(account.getAccountId());
        }

        // Delete the user
        userDao.deleteUser(userId);
        
        System.out.println("Facade: User and all associated accounts deleted successfully: " + userId);
        return true;
    }
}

