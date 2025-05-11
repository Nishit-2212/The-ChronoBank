package com.chronobank;

import com.chronobank.db.DatabaseConnector;
import com.chronobank.service.AccountFactory;
import com.chronobank.model.account.AccountPreferences;
import com.chronobank.model.account.InvestorAccount;
import com.chronobank.model.account.LoanAccount;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.transaction.TransactionRecord;
import com.chronobank.model.user.User;
import com.chronobank.model.common.Investment;
import com.chronobank.pattern.strategy.FixedTimeRepaymentStrategy;
import com.chronobank.service.BankingFacade;
import com.chronobank.service.NotificationService;
import com.chronobank.pattern.command.CommandHistory;
import com.chronobank.pattern.command.TransferCommand;
import com.chronobank.model.admin.Admin;
import com.chronobank.dao.AdminDao;
import com.chronobank.dao.AdminDaoImpl;
import java.util.stream.Collectors;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Map; 
import java.util.HashMap;
import java.util.Comparator;
import java.util.ArrayList;

public class Main {
    private static BankingFacade facade;
    private static AccountPreferences currentAccountPreferences;
    private static User currentUser = null;
    private static Admin currentAdmin = null; 
    private static TimeAccount selectedAccount = null;

    private static Scanner scanner;
    private static NotificationService notificationService; 
    // private static NotificationService notificationService = new NotificationService();
    private static CommandHistory commandHistory; 
    // private static CommandHistory commandHistory = new CommandHistory();
    private static AdminDao adminDao;

    public static void main(String[] args) {
        System.out.println("ChronoBank System Initializing...");
        facade = new BankingFacade();
        currentAccountPreferences = new AccountPreferences.Builder().build(); 
        scanner = new Scanner(System.in);
        notificationService = new NotificationService(); 
        commandHistory = new CommandHistory(); 
        adminDao = new AdminDaoImpl(); 

        // System.out.println(" jdbc:postgresql://localhost:5432/chronobank(database) (port=5432) (user=postgres) (password=123)");

        if (!DatabaseConnector.testConnection()) {
            System.err.println("failed to connect to the database.");
            scanner.close();
            return;
        }
        // System.out.println("database connection test is successful.");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
                continue;
            }

            try {
                switch (choice) {
                    case 1: handleRegister(); break;
                    case 2: handleLogin(); break;
                    case 3: handleCustomizeAccountPreferences(); break; 
                    case 4: handleCreateAccount(); break; 
                    case 5: handleCheckBalance(); break; 
                    // case 12: handleNotificationDemos(); break;
                    case 6: handleWithdraw(); break; 
                    case 7: handleDeposit(); break; 
                    case 8: handleTransferMoney(); break; 
                    // case 9: handleSuspiciusactivity(); break;
                    case 9: handleLoanOperations(); break; 
                    case 10: handleMakeInvestment(); break; 
                    case 11: handleViewTransactionHistory(); break; 
                    case 12: handleNotificationDemos(); break; 
                    case 13: handleSelectAccount(); break; 
                    case 14: handleLogout(); break;
                    case 15: handleUndoTransaction(); break;
                    // case 15: handleUndoTransactionswithlimit(); break;
                    case 16: handleRedoTransaction(); break;
                    case 17: handleViewAccountStatus(); break;
                    // case 17: handleViewAccountStatuswithlimit(); break;
                    case 18: handleAdminLogin(); break;
                    case 19: handleAdminMenu(); break;
                    case 20: handleViewAllInvestments(); break;
                    case 0: running = false; break;
                    default: System.out.println("Invalid option.please try again.");
                }
            } catch (SQLException e) {
                System.err.println("SQL error occurred: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace(); 
            }
            if (running && choice !=0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        // System.out.println("Shutting down ChronoBank system...");
        System.out.println("ChronoBank System is Shutting down...");

        facade.closeDatabaseConnection();
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n------------ ChronoBank Menu --------------------");
        // if (currentUser == null && currentAdmin == null) {
        //     System.out.println("***************Welcome to ChronoBank!****************8 Please register or login.");
        // } else if (currentUser != null) {
        //     System.out.println("Welcome back, " + currentUser.getUsername() + "!");
        // }
        if (currentUser == null && currentAdmin == null) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("18. Admin Login");
        } else if (currentAdmin != null) {
            System.out.println("Logged in as Admin: " + currentAdmin.getUsername());
            System.out.println("19. Admin Menu");
            System.out.println("14. Logout");
        } else {
            System.out.println("Logged in as: " + currentUser.getUsername() + (selectedAccount != null ? " | Selected Account: " + selectedAccount.getAccountId() : " | No account selected"));
            System.out.println("--- Account Management & Preferences ---");
            System.out.println("3. Customize Account Creation Preferences (uses builder Pattern)"); 
            System.out.println("4. Create Bank Account (uses current preferences)");
            System.out.println("13. Select/Switch Account");
            System.out.println("--- Selected Account Operations (" + (selectedAccount != null ? selectedAccount.getAccountId() : "N/A") + ") ---");
            System.out.println("5. Check Balance");
            System.out.println("6. Withdraw");
            System.out.println("7. Deposit");
            System.out.println("8. Transfer Money");
            System.out.println("9. Loan Operations (for Loan Accounts)");
            System.out.println("10. Make Investment (for Investor Accounts)");
            System.out.println("20. View All Investments (for Investor Accounts)");
            System.out.println("11. View Transaction History");
            System.out.println("--- Pattern Demonstrations ---");
            System.out.println("12. Notification Demonstrations (uses observer Pattern)");
            System.out.println("15. Undo Last Transaction (uses command Pattern)");
            System.out.println("16. Redo Last Undone Transaction (uses command Pattern)");
            System.out.println("17. View Account Status (uses state Pattern)");
            System.out.println("--- Session ---");
            System.out.println("14. Logout");
        }
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void handleNotificationDemos() {
        if (currentUser == null) {
            System.out.println("please login first to see notification demonstrations.");
            return;
        }
        System.out.println("\n--- all types of Notification (observer Pattern) ---");
        System.out.println("The ChronoBank system uses the Observer pattern to notify users of important events.");
        System.out.println("notifications are typically logged to the console by services like NotificationService and FraudDetectionService.");
        System.out.println("these services 'observe' account activities and react if certain conditions are met based on account preferences.");

        System.out.println("\n1. Low Balance Notification Demo:");
        System.out.println("   - To see this: Select an account, ensure 'LOW_BALANCE' notification is enabled in its preferences (or via 'Customize Account Creation Preferences' for new accounts). Then, perform a withdrawal that brings the balance below a predefined threshold (e.g., 100).");
        // System.out.println("   - The NotificationService should log a low balance warning to the console.");

        System.out.println("\n2. Suspicious Transaction Notification Demo:");
        System.out.println("   - To see this: Select an account, ensure 'LARGE_TRANSACTION' notification is enabled. Then, perform a very large transfer or withdrawal (e.g., over 10000, or a value that triggers the FraudDetectionService's criteria).");
        System.out.println("   - The FraudDetectionService (acting as an observer) should log a suspicious transaction alert.");

        System.out.println("\n3. Loan Repayment Deadline Notification Demo (Simulated):");
        if (selectedAccount != null && selectedAccount instanceof LoanAccount) {
            LoanAccount loanAccount = (LoanAccount) selectedAccount;
            
            
            System.out.println("   Selected Loan Account: " + loanAccount.getAccountId());
            System.out.println("   Simulating a loan repayment reminder check...");
            
            if (currentAccountPreferences.getNotificationPreference("LOAN_REMINDER")) {
                 notificationService.sendNotification(loanAccount.getOwner().getUserId(), "LOAN_REMINDER", 
                    "Reminder: A repayment for your loan account " + loanAccount.getAccountId() + " is approaching/due. Please check your repayment schedule.");
                System.out.println("  --> Simulated LOAN_REMINDER notification sent (check console). Ensure this notification type is enabled in preferences.");
            } else {
                System.out.println("  --> LOAN_REMINDER notifications are currently disabled in the general account creation preferences. Enable it via option 3 to see this demo for new accounts, or ensure the specific loan account has it enabled.");
            }
        } else {
            System.out.println("   - To see this: First, select a Loan Account from your accounts list (Option 13).");
            System.out.println("   - Ensure 'LOAN_REMINDER' notification is enabled for it (via 'Customize Account Creation Preferences' when creating it). Then, select this option again.");
        }
        System.out.println("\nNote: Actual notifications depend on the account's specific preferences set at creation or if the account object itself stores and uses them.");
    }

    // private static void handleNotificationDemos() {
    //     if (currentUser == null) {
    //         System.out.println("Please login first to see notification demonstrations.");
    //         return;
    //     }
    //     System.out.println("\n--- Notification Demonstrations (Observer Pattern) ---");

    private static void handleCustomizeAccountPreferences() {
        if (currentUser == null) {
            System.out.println("Please login first to customize preferences for new accounts.");
            return;
        }
        System.out.println("\n--- Customize Account Creation Preferences ---");
        AccountPreferences.Builder prefBuilder = new AccountPreferences.Builder();

        System.out.println("Current preferences will be applied to nEWLY created accounts.");
        System.out.println("Current Transaction Limit: "+ currentAccountPreferences.getTransactionLimitPerDay() +". Current Interest Type: "+ currentAccountPreferences.getPreferredInterestRateType() + ".");
        System.out.println("Current Notifications: LOW_BALANCE ("+ currentAccountPreferences.getNotificationPreference("LOW_BALANCE") +"), LARGE_TRANSACTION ("+ currentAccountPreferences.getNotificationPreference("LARGE_TRANSACTION") +"), LOAN_REMINDER ("+ currentAccountPreferences.getNotificationPreference("LOAN_REMINDER") +").");

        System.out.print("Enter new daily transaction limit (e.g., 10000, press Enter to keep current): ");
        String limitInput = scanner.nextLine();
        if (!limitInput.isEmpty()) {
            try {
                prefBuilder.withTransactionLimit(Double.parseDouble(limitInput));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for limit. Keeping current.");
                prefBuilder.withTransactionLimit(currentAccountPreferences.getTransactionLimitPerDay());
            }
        } else {
             prefBuilder.withTransactionLimit(currentAccountPreferences.getTransactionLimitPerDay());
        }

        System.out.print("enter preferred interest rate type (e.g., FIXED, DYNAMIC, press enter to keep current): ");
        String rateTypeInput = scanner.nextLine();
        if (!rateTypeInput.isEmpty()) {
            prefBuilder.withPreferredInterestRateType(rateTypeInput.toUpperCase());
        } else {
            prefBuilder.withPreferredInterestRateType(currentAccountPreferences.getPreferredInterestRateType());
        }

        System.out.println("configure Notification Preferences (Y/N/Enter to keep current): ");
        String[] notificationTypes = {"LOW_BALANCE", "LARGE_TRANSACTION", "LOAN_REMINDER"};
        for (String type : notificationTypes) {
            System.out.print("Enable '" + type + "' notifications? (Currently: "+ (currentAccountPreferences.getNotificationPreference(type) ? "Y" : "N") +", Enter Y/N or press Enter to keep): ");
            String notifInput = scanner.nextLine().toUpperCase();
            if (notifInput.equals("Y")) {
                prefBuilder.withNotificationPreference(type, true);
            } else if (notifInput.equals("N")) {
                prefBuilder.withNotificationPreference(type, false);
            } else {
                prefBuilder.withNotificationPreference(type, currentAccountPreferences.getNotificationPreference(type));
            }
        }
        currentAccountPreferences = prefBuilder.build();
        System.out.println("Account creation preferences updated: " + currentAccountPreferences.toString());
        // System.out.println("checking these preferences will be used for the next account you create in this session.");
    }

    private static void handleRegister() throws SQLException {
        System.out.println("\n--- User Registration ---");
        // System.out.println("Please enter your details to register a new account.");
        // System.out.println("Note: If you are already registered, please login instead.");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        User newUser = facade.registerUser(username, password, email);
        if (newUser != null) {
            System.out.println("Registration successful for " + newUser.getUsername());
        } else {
            System.out.println("Registration failed. Username might already exist or other error.");
        }
    }

    private static void handleLogin() throws SQLException {
        System.out.println("\n--- User Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        Optional<User> userOpt = facade.loginUser(username, password);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            selectedAccount = null; 
            currentAccountPreferences = new AccountPreferences.Builder().build(); 
            System.out.println("Login successful. Welcome, " + currentUser.getUsername());
            System.out.println("Account creation preferences reset to default.");
            handleSelectAccount(); 
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private static void handleLogout() {
        if (currentAdmin != null) {
            currentAdmin = null;
            System.out.println("Admin logged out successfully.");
        } else if (currentUser != null) {
            currentUser = null;
            selectedAccount = null;
            currentAccountPreferences = new AccountPreferences.Builder().build(); 
            System.out.println("User logged out successfully.");
        }
    }

    private static void handleCreateAccount() throws SQLException {
        if (currentUser == null) {
            System.out.println("You must be logged in to create an account. Please login first.");
            return;
        }
        System.out.println("\n--- Create New Bank Account (using current preferences) ---");
        System.out.println("Current preferences for new account: " + currentAccountPreferences.toString());
        System.out.println("Select account type:");
        System.out.println("1. Basic Time Account");
        System.out.println("2. Investor Account");
        System.out.println("3. Loan Account");
        System.out.print("Enter choice: ");
        int typeChoice = -1;
        if (scanner.hasNextInt()) {
            typeChoice = scanner.nextInt();
            scanner.nextLine(); 
        } else {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }

        System.out.print("Enter initial deposit (0 for Loan account): ");
        double initialDeposit = scanner.nextDouble();
        scanner.nextLine(); 

        TimeAccount newAccount = null;
        switch (typeChoice) {
            case 1: 
                newAccount = facade.createAccount(currentUser, AccountFactory.AccountType.BASIC, currentAccountPreferences, initialDeposit);
                break;
            case 2: 
                System.out.print("Enter investor interest rate (e.g., 0.03 for 3%): ");
                double investorRate = scanner.nextDouble();
                scanner.nextLine();
                newAccount = facade.createAccount(currentUser, AccountFactory.AccountType.INVESTOR, currentAccountPreferences, initialDeposit, investorRate);
                break;
            case 3: 
                System.out.print("Enter loan amount: ");
                double loanAmount = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Enter loan interest rate (e.g., 0.05 for 5%): ");
                double loanRate = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Enter loan term in months: ");
                int termMonths = scanner.nextInt();
                scanner.nextLine();
                newAccount = facade.createAccount(currentUser, AccountFactory.AccountType.LOAN, currentAccountPreferences, 0, loanAmount, loanRate, termMonths, new FixedTimeRepaymentStrategy());
                break;
            default:
                System.out.println("Invalid account type selected");
                return;
        }

        if (newAccount != null) {
            System.out.println("Account created successfully ! Account ID is : " + newAccount.getAccountId());
            if(selectedAccount == null) selectedAccount = newAccount; 
        } else {
            System.out.println("Account creation failed");
        }
    }

    private static void handleSelectAccount() throws SQLException {
        if (currentUser == null) {
            System.out.println("Please login first");
            return;
        }
        List<TimeAccount> accounts = facade.findAccountsByUserId(currentUser.getUserId());
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for user: " + currentUser.getUsername());
            selectedAccount = null;
            return;
        }
        System.out.println("\n--- Select Account ---");
        for (int i = 0; i < accounts.size(); i++) {
            TimeAccount acc = accounts.get(i);
            System.out.println((i + 1) + ". Account ID: " + acc.getAccountId() + " (Type: " + acc.getAccountType() + ", Balance: " + String.format("%.2f", acc.getBalance()) + ")");
        }
        System.out.print("Select account number (or 0 to cancel): ");
        int accChoice = -1;
        if (scanner.hasNextInt()) {
            accChoice = scanner.nextInt();
            scanner.nextLine();
        } else {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }

        if (accChoice > 0 && accChoice <= accounts.size()) {
            selectedAccount = accounts.get(accChoice - 1);
            System.out.println("Account " + selectedAccount.getAccountId() + " selected.");
        } else if (accChoice == 0) {
            System.out.println("Account selection cancelled.");
        } else {
            System.out.println("Invalid account selection.");
        }
    }

    private static void handleCheckBalance() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        Optional<TimeAccount> accOpt = facade.findAccountById(selectedAccount.getAccountId());
        if(accOpt.isPresent()){
            selectedAccount = accOpt.get();
            System.out.println("\n--- Account Balance ---");
            System.out.println("Account ID: " + selectedAccount.getAccountId());
            System.out.println("Current Balance: " + String.format("%.2f", selectedAccount.getBalance()));
            if (selectedAccount instanceof LoanAccount) {
                LoanAccount la = (LoanAccount) selectedAccount;
                System.out.println("Remaining Loan Principal: " + String.format("%.2f", la.getRemainingLoanPrincipal()));
            }
        } else {
            System.out.println("Could not retrieve account details. It might have been closed.");
            selectedAccount = null;
        }
    }

    private static void handleWithdraw() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an account first.");
             if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        if (selectedAccount instanceof LoanAccount) {
            System.out.println("Withdrawals are not typically performed directly from a Loan account in this manner.");
            return;
        }
        System.out.println("\n----- Withdraw Funds ------");
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        boolean success = facade.performWithdrawal(selectedAccount, amount, description);
        if (success) {
            System.out.println("Withdrawal successful. New balance: " + String.format("%.2f", selectedAccount.getBalance()));
        } else {
            System.out.println("Withdrawal failed. Insufficient funds or other error. Current balance: " + String.format("%.2f", selectedAccount.getBalance()));
        }
    }

    private static void handleDeposit() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        System.out.println("\n--- Deposit Funds ---");
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        boolean success = facade.performDeposit(selectedAccount, amount, description);
        if (success) {
            System.out.println("Deposit successful. New balance: " + String.format("%.2f", selectedAccount.getBalance()));
        } else {
            System.out.println("Deposit failed.");
        }
    }

    private static void handleTransferMoney() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select a source account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        System.out.println("\n--- Transfer Money ---");
        System.out.print("Enter target account ID: ");
        String targetAccountId = scanner.nextLine();
        Optional<TimeAccount> targetAccountOpt = facade.findAccountById(targetAccountId);
        if (!targetAccountOpt.isPresent()) {
            System.out.println("Target account ID not found");
            return;
        }
        TimeAccount targetAccount = targetAccountOpt.get();
        if (targetAccount.getAccountId().equals(selectedAccount.getAccountId())){
            System.out.println("Cannot transfer to the same account");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        // created this  and execute transfer command pattern
        TransferCommand transferCommand = new TransferCommand(facade, selectedAccount, targetAccount, amount, description);
        commandHistory.executeCommand(transferCommand);

        System.out.println("Transfer command executed. New balance: " + String.format("%.2f", selectedAccount.getBalance()));
    }

    private static void handleLoanOperations() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select a loan account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        if (!(selectedAccount instanceof LoanAccount)) {
            System.out.println("The selected account is not a Loan account. Please select a Loan account for these operations.");
            handleSelectAccount(); 
            if (!(selectedAccount instanceof LoanAccount)) return;
        }
        LoanAccount loanAccount = (LoanAccount) selectedAccount;

        System.out.println("\n--- Loan Operations for Account: " + loanAccount.getAccountId() + " ---");
        System.out.println("Remaining Principal: " + String.format("%.2f", loanAccount.getRemainingLoanPrincipal()) + " | Current Balance (Repayments Made): " + String.format("%.2f", loanAccount.getBalance()));
        System.out.println("1. Make Repayment");
        System.out.println("2. Disburse Loan (if applicable)");
        System.out.println("0. Back to main menu");
        System.out.print("Enter choice: ");
        int loanOpChoice = -1;
        if (scanner.hasNextInt()) {
            loanOpChoice = scanner.nextInt();
            scanner.nextLine();
        } else {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }

        switch (loanOpChoice) {
            case 1: 
                System.out.print("Enter repayment amount (or type 'auto' for calculated amount): ");
                String repaymentInput = scanner.nextLine();
                double repaymentAmount;
                if ("auto".equalsIgnoreCase(repaymentInput)) {
                    repaymentAmount = loanAccount.calculateCurrentRepaymentAmount();
                    if (repaymentAmount == 0 && loanAccount.getRemainingLoanPrincipal() > 0) {
                        System.out.println("Calculated repayment is 0, but principal remains. Please enter a manual amount: ");
                        repaymentAmount = scanner.nextDouble();
                        scanner.nextLine();
                    }
                    System.out.println("Calculated repayment amount: " + String.format("%.2f", repaymentAmount));
                } else {
                    try {
                        repaymentAmount = Double.parseDouble(repaymentInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount entered.");
                        return;
                    }
                }
                if (repaymentAmount <= 0) {
                    System.out.println("Repayment amount must be positive.");
                    return;
                }
                
                List<TimeAccount> potentialSourceAccounts = facade.findAccountsByUserId(currentUser.getUserId());
                TimeAccount sourceRepaymentAccount = null;
                for(TimeAccount acc : potentialSourceAccounts) {
                    if (!acc.getAccountId().equals(loanAccount.getAccountId()) && (acc.getAccountType().equals("BASIC") || acc.getAccountType().equals("INVESTOR"))) {
                        if(acc.getBalance() >= repaymentAmount){
                            sourceRepaymentAccount = acc;
                            break;
                        }
                    }
                }
                if(sourceRepaymentAccount == null){
                    System.out.println("No suitable source account with sufficient funds found to make repayment. Please deposit into a Basic/Investor account first.");
                    return;
                }
                System.out.println("Using account " + sourceRepaymentAccount.getAccountId() + " (Balance: "+ String.format("%.2f",sourceRepaymentAccount.getBalance()) +") for repayment.");

                boolean repaySuccess = facade.makeLoanRepayment(loanAccount, sourceRepaymentAccount, repaymentAmount);
                if (repaySuccess) {
                    System.out.println("Loan repayment successful.");
                    System.out.println("Loan Account - New Remaining Principal: " + String.format("%.2f", loanAccount.getRemainingLoanPrincipal()) + ", New Balance: " + String.format("%.2f", loanAccount.getBalance()));
                    System.out.println("Source Account ("+sourceRepaymentAccount.getAccountId()+") - New Balance: " + String.format("%.2f", sourceRepaymentAccount.getBalance()));
                } else {
                    System.out.println("Loan repayment failed. Check source account balance or other issues.");
                }
                break;
            case 2: 
                System.out.print("Enter amount to disburse: ");
                double disburseAmount = scanner.nextDouble();
                scanner.nextLine();
                TimeAccount disburseTargetAccount = null;
                List<TimeAccount> userAccounts = facade.findAccountsByUserId(currentUser.getUserId());
                for(TimeAccount acc : userAccounts){
                    if(!acc.getAccountId().equals(loanAccount.getAccountId()) && acc.getAccountType().equals("BASIC")){
                        disburseTargetAccount = acc;
                        break;
                    }
                }
                if(disburseTargetAccount == null){
                    System.out.println("No Basic account found to disburse funds to. Please create one first.");
                    return;
                }
                System.out.println("Disbursing to Basic Account: " + disburseTargetAccount.getAccountId());

                boolean disburseSuccess = facade.disburseLoan(loanAccount, disburseTargetAccount, disburseAmount);
                if (disburseSuccess) {
                    System.out.println("Loan disbursement successful.");
                    System.out.println("Loan Account - Remaining Principal: " + String.format("%.2f", loanAccount.getRemainingLoanPrincipal()));
                    System.out.println("Target Account ("+disburseTargetAccount.getAccountId()+") - New Balance: " + String.format("%.2f", disburseTargetAccount.getBalance()));
                } else {
                    System.out.println("Loan disbursement failed. Amount might exceed available loan or other issue.");
                }
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid loan operation choice.");
        }
    }

    private static void handleMakeInvestment() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an Investor account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        if (!(selectedAccount instanceof InvestorAccount)) {
            System.out.println("The selected account is not an Investor account. Please select an Investor account for this operation.");
            handleSelectAccount();
            if (!(selectedAccount instanceof InvestorAccount)) return;
        }
        InvestorAccount investorAccount = (InvestorAccount) selectedAccount;
        System.out.println("\n--- Make Investment from Account: " + investorAccount.getAccountId() + " ---");
        System.out.print("Enter investment product ID (e.g., TIME_BONDS_SERIES_A): ");
        String productId = scanner.nextLine();
        System.out.print("Enter amount to invest: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        boolean success = facade.makeInvestment(investorAccount, productId, amount);
        if (success) {
            System.out.println("Investment of " + String.format("%.2f", amount) + " in " + productId + " successful.");
            System.out.println("Investor Account new balance: " + String.format("%.2f", investorAccount.getBalance()));
            System.out.println("Current Investments: " + investorAccount.getCurrentInvestments().size());
        } else {
            System.out.println("Investment failed. Insufficient funds or other error.");
        }
    }

    private static void handleViewTransactionHistory() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        System.out.println("\n--- Transaction History for Account: " + selectedAccount.getAccountId() + " ---");
        List<TransactionRecord> history = facade.getTransactionHistory(selectedAccount.getAccountId());
        if (history.isEmpty()) {
            System.out.println("No transactions found for this account.");
        } else {
            for (TransactionRecord record : history) {
                System.out.println(record.toString()); 
            }
        }
    }

    private static void handleUndoTransaction() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }
        if (commandHistory.canUndo()) {
            commandHistory.undo();
            System.out.println("Last transaction undone successfully.");
            if (selectedAccount != null) {
                System.out.println("Current balance: " + String.format("%.2f", selectedAccount.getBalance()));
            }
        } else {
            System.out.println("No transactions to undo.");
        }
    }

    private static void handleRedoTransaction() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }
        if (commandHistory.canRedo()) {
            commandHistory.redo();
            System.out.println("Last undone transaction redone successfully.");
            if (selectedAccount != null) {
                System.out.println("Current balance: " + String.format("%.2f", selectedAccount.getBalance()));
            }
        } else {
            System.out.println("No transactions to redo.");
        }
    }

    private static void handleViewAccountStatus() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        System.out.println("\n--- Account Status Information ---");
        System.out.println("Account ID: " + selectedAccount.getAccountId());
        System.out.println("Current Status: " + selectedAccount.getAccountStatus().getStatusName());
        System.out.println("Balance: " + String.format("%.2f", selectedAccount.getBalance()));
        
        String statusName = selectedAccount.getAccountStatus().getStatusName();
        switch (statusName) {
            case "ACTIVE":
                System.out.println("Account is fully operational.");
                break;
            case "OVERDRAWN":
                System.out.println("Account is overdrawn. Deposits are allowed but withdrawals are restricted.");
                break;
            case "FROZEN":
                System.out.println("Account is frozen due to suspicious activity or administrative action.");
                System.out.println("All transactions are restricted until the account is reviewed.");
                break;
            default:
                System.out.println("Unknown account status.");
        }
    }

    private static void handleAdminLogin() throws SQLException {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();

        Optional<Admin> adminOpt = adminDao.findByUsername(username);
        if (adminOpt.isPresent()) {
            
            if ("admin123".equals(password)) {
                currentAdmin = adminOpt.get();
                currentUser = null; 
                System.out.println("Admin login successful. Welcome, " + currentAdmin.getUsername());
            } else {
                System.out.println("Invalid admin password.");
            }
        } else {
            System.out.println("Admin not found.");
        }
    }

    private static void handleAdminMenu() throws SQLException {
        if (currentAdmin == null) {
            System.out.println("Please login as admin first.");
            return;
        }

        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. View All Users");
        System.out.println("2. View All Transactions");
        System.out.println("3. View All Loans");
        System.out.println("4. Change User Password");
        System.out.println("5. Change User Reputation Score");
        System.out.println("6. View Admin Audit Log");
        System.out.println("7. Fraud Detection - Rapid Transactions");
        System.out.println("8. Manage Account Status");
        System.out.println("9. Delete User Account");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1: handleViewAllUsers(); break;
            case 2: handleViewAllTransactions(); break;
            case 3: handleViewAllLoans(); break;
            case 4: handleChangeUserPassword(); break;
            case 5: handleChangeUserReputation(); break;
            case 6: handleViewAdminAuditLog(); break;
            case 7: handleRapidTransactionMonitoring(); break;
            case 8: handleManageAccountStatus(); break;
            case 9: handleDeleteUserAccount(); break;
            case 0: return;
            default: System.out.println("Invalid option.");
        }
    }

    private static void handleViewAllUsers() throws SQLException {
        List<User> users = facade.getAllUsers();
        System.out.println("\n--- All Users ---");
        for (User user : users) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Reputation Score: " + user.getReputationScore());
            System.out.println("-------------------");
        }
    }

    private static void handleViewAllTransactions() throws SQLException {
        List<TransactionRecord> transactions = facade.getAllTransactions();
        System.out.println("\n--- All Transactions ---");
        for (TransactionRecord tx : transactions) {
            System.out.println(tx.toString());
        }
    }

    private static void handleViewAllLoans() throws SQLException {
        List<TimeAccount> accounts = facade.getAllAccounts();
        System.out.println("\n--- All Loans ---");
        for (TimeAccount account : accounts) {
            if (account instanceof LoanAccount) {
                LoanAccount loan = (LoanAccount) account;
                System.out.println("Loan ID: " + loan.getAccountId());
                System.out.println("Owner: " + loan.getOwner().getUsername());
                System.out.println("Principal: " + loan.getRemainingLoanPrincipal());
                System.out.println("Status: " + loan.getAccountStatus().getStatusName());
                System.out.println("-------------------");
            }
        }
    }

    private static void handleChangeUserPassword() throws SQLException {
        System.out.println("\n--- Change User Password ---");
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();
        
        Optional<User> userOpt = facade.findUserById(userId);
        if (userOpt.isPresent()) {
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();
            
            facade.changeUserPassword(userId, "hashed_" + newPassword);
            adminDao.logAdminAction(currentAdmin.getAdminId(), "PASSWORD_CHANGE", userId, "Password changed by admin");
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void handleChangeUserReputation() throws SQLException {
        System.out.println("\n--- Change User Reputation Score ---");
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();
        
        Optional<User> userOpt = facade.findUserById(userId);
        if (userOpt.isPresent()) {
            System.out.print("Enter new reputation score (0-1000): ");
            int newScore = scanner.nextInt();
            scanner.nextLine(); 
            
            if (newScore >= 0 && newScore <= 1000) {
                facade.changeUserReputation(userId, newScore);
                adminDao.logAdminAction(currentAdmin.getAdminId(), "REPUTATION_CHANGE", userId, 
                    "Reputation score changed from " + userOpt.get().getReputationScore() + " to " + newScore);
                System.out.println("Reputation score updated successfully.");
            } else {
                System.out.println("Invalid reputation score. Must be between 0 and 1000.");
            }
        } else {
            System.out.println("User not found.");
        }
    }

    private static void handleViewAdminAuditLog() throws SQLException {
        List<String> auditLog = adminDao.getAdminAuditLog(currentAdmin.getAdminId());
        System.out.println("\n--- Admin Audit Log ---");
        for (String logEntry : auditLog) {
            System.out.println(logEntry);
        }
    }

    private static void handleRapidTransactionMonitoring() throws SQLException {
        System.out.println("\n--- Rapid Transaction Monitoring ---");
        System.out.println("1. View Large Transactions (>10000)");
        System.out.println("2. View Rapid Transaction Patterns (5+ transactions in 10 minutes)");
        System.out.println("3. View Combined Risk Analysis");
        System.out.println("0. Back to Admin Menu");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); 
        switch (choice) {
            case 1:
               // from this you can viiew large transaction
                List<TransactionRecord> transactions = facade.getAllTransactions();
                System.out.println("\n--- Large Transactions (>10000) ---");
                for (TransactionRecord tx : transactions) {
                    if (tx.getAmount() > 10000) {
                        System.out.println("Transaction ID: " + tx.getTransactionId());
                        System.out.println("Amount: " + tx.getAmount());
                        System.out.println("From Account: " + tx.getFromAccountId());
                        System.out.println("To Account: " + tx.getToAccountId());
                        System.out.println("Timestamp: " + tx.getLoggedAt());
                        System.out.println("-------------------");
                    }
                }
                break;

            case 2:
                
                System.out.println("\n--- Rapid Transaction Patterns ---");
                Map<String, List<TransactionRecord>> userTransactions = new HashMap<>();
                
                
                for (TransactionRecord tx : facade.getAllTransactions()) {
                    String userId = getUserIdFromAccount(tx.getFromAccountId());
                    if (userId != null) {
                        userTransactions.computeIfAbsent(userId, k -> new ArrayList<>()).add(tx);
                    }
                }

                boolean foundSuspiciousActivity = false;
                
                for (Map.Entry<String, List<TransactionRecord>> entry : userTransactions.entrySet()) {
                    String userId = entry.getKey();
                    List<TransactionRecord> userTxs = entry.getValue();
                    
                    
                    userTxs.sort(Comparator.comparing(TransactionRecord::getLoggedAt));
                    
                   
                    for (int i = 0; i < userTxs.size() - 4; i++) {
                        List<TransactionRecord> windowTxs = new ArrayList<>();
                        TransactionRecord firstTx = userTxs.get(i);
                        windowTxs.add(firstTx);
                        
                        
                        for (int j = i + 1; j < userTxs.size(); j++) {
                            TransactionRecord currentTx = userTxs.get(j);
                            long timeDiff = currentTx.getLoggedAt().getTime() - firstTx.getLoggedAt().getTime();
                            
                           
                            if (timeDiff <= 10 * 60 * 1000) { 
                                windowTxs.add(currentTx);
                                if (windowTxs.size() >= 5) {
                                    foundSuspiciousActivity = true;
                                    System.out.println("\nSuspicious Activity Detected for User ID: " + userId);
                                    Optional<User> userOpt = facade.findUserById(userId);
                                    if (userOpt.isPresent()) {
                                        System.out.println("Username: " + userOpt.get().getUsername());
                                    }
                                    System.out.println("Time Window: " + (timeDiff / 1000) + " seconds");
                                    System.out.println("Transactions in this window:");
                                    
                                    double totalAmount = 0;
                                    for (TransactionRecord tx : windowTxs) {
                                        System.out.println("Transaction ID: " + tx.getTransactionId());
                                        System.out.println("Amount: " + tx.getAmount());
                                        System.out.println("From Account: " + tx.getFromAccountId());
                                        System.out.println("To Account: " + tx.getToAccountId());
                                        System.out.println("Timestamp: " + tx.getLoggedAt());
                                        System.out.println("-------------------");
                                        totalAmount += tx.getAmount();
                                    }
                                    System.out.println("Total Amount in Window: " + totalAmount);
                                    
                                    
                                    adminDao.logAdminAction(currentAdmin.getAdminId(), "SUSPICIOUS_ACTIVITY", userId, 
                                        String.format("Detected %d transactions totaling %.2f within %d seconds", 
                                            windowTxs.size(), totalAmount, timeDiff / 1000));
                                    
                                    
                                    break;
                                }
                            } else {
                               
                                break;
                            }
                        }
                    }
                }
                
                if (!foundSuspiciousActivity) {
                    System.out.println("No suspicious rapid transaction patterns detected.");
                }
                break;

            case 3:
                
                System.out.println("\n--- Combined Risk Analysis ---");
                Map<String, Integer> userRiskScores = new HashMap<>();
                
                
                for (TransactionRecord tx : facade.getAllTransactions()) {
                    String userId = getUserIdFromAccount(tx.getFromAccountId());
                    if (userId != null) {
                        int riskScore = userRiskScores.getOrDefault(userId, 0);
                        
                        
                        if (tx.getAmount() > 10000) {
                            riskScore += 10;
                        }
                        
                        userRiskScores.put(userId, riskScore);
                    }
                }
                
                
                System.out.println("Users with High Risk Scores:");
                for (Map.Entry<String, Integer> entry : userRiskScores.entrySet()) {
                    if (entry.getValue() >= 20) {
                        String userId = entry.getKey();
                        Optional<User> userOpt = facade.findUserById(userId);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            System.out.println("\nUser ID: " + userId);
                            System.out.println("Username: " + user.getUsername());
                            System.out.println("Risk Score: " + entry.getValue());
                            System.out.println("Reputation Score: " + user.getReputationScore());
                            System.out.println("-------------------");
                        }
                    }
                }
                break;

            case 0:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static String getUserIdFromAccount(String accountId) {
        try {
            Optional<TimeAccount> accountOpt = facade.findAccountById(accountId);
            if (accountOpt.isPresent()) {
                return accountOpt.get().getOwner().getUserId();
            }
        } catch (SQLException e) {
            System.err.println("Error finding account: " + e.getMessage());
        }
        return null;
    }

    private static void handleManageAccountStatus() throws SQLException {
        System.out.println("\n--- Manage Account Status ---");
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();
        
        Optional<User> userOpt = facade.findUserById(userId);
        if (!userOpt.isPresent()) {
            System.out.println("User not found.");
            return;
        }

        
        List<TimeAccount> userAccounts = facade.findAccountsByUserId(userId);
        if (userAccounts.isEmpty()) {
            System.out.println("No accounts found for this user.");
            return;
        }

        
        System.out.println("\nCurrent Account Statuses:");
        for (TimeAccount account : userAccounts) {
            System.out.println("Account ID: " + account.getAccountId());
            System.out.println("Type: " + account.getAccountType());
            System.out.println("Current Status: " + account.getAccountStatus().getStatusName());
            System.out.println("Balance: " + account.getBalance());
            System.out.println("-------------------");
        }

        
        System.out.print("\nEnter account ID to modify: ");
        String accountId = scanner.nextLine();
        
        Optional<TimeAccount> accountOpt = facade.findAccountById(accountId);
        if (!accountOpt.isPresent()) {
            System.out.println("Account not found.");
            return;
        }

        TimeAccount selectedAccount = accountOpt.get();
        
        
        System.out.println("\nSelect new status for account " + accountId + ":");
        System.out.println("1. ACTIVE - Normal operation");
        System.out.println("2. FROZEN - No transactions allowed");
        System.out.println("3. OVERDRAWN - Limited operations");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");

        int statusChoice = scanner.nextInt();
        scanner.nextLine();

        String newStatus;
        String statusReason;
        switch (statusChoice) {
            case 1:
                newStatus = "ACTIVE";
                System.out.print("Enter reason for activating account: ");
                statusReason = scanner.nextLine();
                break;
            case 2:
                newStatus = "FROZEN";
                System.out.print("Enter reason for freezing account: ");
                statusReason = scanner.nextLine();
                break;
            case 3:
                newStatus = "OVERDRAWN";
                System.out.print("Enter reason for marking account as overdrawn: ");
                statusReason = scanner.nextLine();
                break;
            case 0:
                System.out.println("Status change cancelled.");
                return;
            default:
                System.out.println("Invalid status choice.");
                return;
        }

        
        boolean success = facade.updateAccountStatus(selectedAccount, newStatus);
        if (success) {
            System.out.println("Account status updated successfully to: " + newStatus);
            
           
            adminDao.logAdminAction(currentAdmin.getAdminId(), "ACCOUNT_STATUS_CHANGE", 
                userId, String.format("Changed account %s status to %s. Reason: %s", 
                    accountId, newStatus, statusReason));
            
           
            notificationService.sendNotification(userId, "ACCOUNT_STATUS_CHANGE",
                String.format("Your account %s status has been changed to %s. Reason: %s",
                    accountId, newStatus, statusReason));
        } else {
            System.out.println("Failed to update account status.");
        }
    }

    private static void handleViewAllInvestments() throws SQLException {
        if (currentUser == null || selectedAccount == null) {
            System.out.println("Please login and select an Investor account first.");
            if(currentUser != null && selectedAccount == null) handleSelectAccount();
            return;
        }
        if (!(selectedAccount instanceof InvestorAccount)) {
            System.out.println("The selected account is not an Investor account. Please select an Investor account for this operation.");
            handleSelectAccount();
            if (!(selectedAccount instanceof InvestorAccount)) return;
        }
        InvestorAccount investorAccount = (InvestorAccount) selectedAccount;
        System.out.println("\n--- All Investments for Account: " + investorAccount.getAccountId() + " ---");
        
        List<Investment> investments = investorAccount.getCurrentInvestments();
        if (investments.isEmpty()) {
            System.out.println("No investments found for this account.");
            return;
        }

        System.out.println("\nCurrent Active Investments:");
        System.out.println("------------------------");
        for (Investment inv : investments) {
            if ("ACTIVE".equals(inv.getStatus())) {
                System.out.println("Investment ID: " + inv.getInvestmentId());
                System.out.println("Type: " + inv.getInvestmentType());
                System.out.println("Amount Invested: " + String.format("%.2f", inv.getAmountInvested()));
                System.out.println("Current Value: " + String.format("%.2f", inv.getCurrentValue()));
                System.out.println("Start Date: " + inv.getStartDate());
                System.out.println("Maturity Date: " + (inv.getMaturityDate() != null ? inv.getMaturityDate() : "Not set"));
                System.out.println("Status: " + inv.getStatus());
                System.out.println("Last Updated: " + inv.getLastUpdatedAt());
                System.out.println("------------------------");
            }
        }

        System.out.println("\nPast Investments:");
        System.out.println("------------------------");
        for (Investment inv : investments) {
            if (!"ACTIVE".equals(inv.getStatus())) {
                System.out.println("Investment ID: " + inv.getInvestmentId());
                System.out.println("Type: " + inv.getInvestmentType());
                System.out.println("Amount Invested: " + String.format("%.2f", inv.getAmountInvested()));
                System.out.println("Final Value: " + String.format("%.2f", inv.getCurrentValue()));
                System.out.println("Start Date: " + inv.getStartDate());
                System.out.println("Maturity Date: " + (inv.getMaturityDate() != null ? inv.getMaturityDate() : "Not set"));
                System.out.println("Status: " + inv.getStatus());
                System.out.println("Last Updated: " + inv.getLastUpdatedAt());
                System.out.println("------------------------");
            }
        }

        // Show investment summary
        double totalInvested = investments.stream()
            .mapToDouble(Investment::getAmountInvested)
            .sum();
        double totalCurrentValue = investments.stream()
            .mapToDouble(Investment::getCurrentValue)
            .sum();
        double totalProfitLoss = totalCurrentValue - totalInvested;
        
        System.out.println("\nInvestment Summary:");
        System.out.println("Total Amount Invested: " + String.format("%.2f", totalInvested));
        System.out.println("Total Current Value: " + String.format("%.2f", totalCurrentValue));
        System.out.println("Total Profit/Loss: " + String.format("%.2f", totalProfitLoss) + 
            " (" + String.format("%.2f", (totalProfitLoss/totalInvested)*100) + "%)");
    }

    private static void handleDeleteUserAccount() throws SQLException {
        System.out.println("\n--- Delete User Account ---");
        System.out.print("Enter user ID to delete: ");
        String userId = scanner.nextLine();
        
        Optional<User> userOpt = facade.findUserById(userId);
        if (userOpt.isPresent()) {
            System.out.println("Warning: This will delete the user and all their accounts.");
            System.out.println("User details:");
            System.out.println("Username: " + userOpt.get().getUsername());
            System.out.println("Email: " + userOpt.get().getEmail());
            
            System.out.print("Are you sure you want to delete this user? (yes/no): ");
            String confirmation = scanner.nextLine().toLowerCase();
            
            if (confirmation.equals("yes")) {
                if (facade.deleteUserAccount(userId)) {
                    adminDao.logAdminAction(currentAdmin.getAdminId(), "USER_DELETION", userId, 
                        "User and all associated accounts deleted by admin");
                    System.out.println("User and all associated accounts deleted successfully.");
                } else {
                    System.out.println("Failed to delete user account.");
                }
            } else {
                System.out.println("User deletion cancelled.");
            }
        } else {
            System.out.println("User not found.");
        }
    }
}

