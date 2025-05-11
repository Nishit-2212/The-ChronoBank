# ChronoBank System - Java OOP Architecture Design

This document outlines the Java Object-Oriented Programming (OOP) architecture for the ChronoBank system, a time-based digital banking platform. The design incorporates core OOP principles and specified design patterns to ensure security, transaction efficiency, access control, financial modeling, maintainability, and fairness.

## 1. Core Classes and Responsibilities

The system will be built around several core classes representing entities and functionalities:

### 1.1. User Management

*   **`User`**: Represents a client of ChronoBank.
    *   **Attributes**: `userId` (String, PK), `username` (String), `hashedPassword` (String), `email` (String), `reputationScore` (int), `riskScore` (int).
    *   **Methods**: Getters and setters for attributes.

### 1.2. Account Management

*   **`TimeAccount` (Abstract Class)**: Base class for all account types. Implements `Subject` for Observer pattern.
    *   **Attributes**: `accountId` (String, PK), `owner` (User), `balance` (double), `status` (AccountStatus - State Pattern), `preferences` (AccountPreferences), `creationDate` (Timestamp), `observers` (List<AccountObserver>).
    *   **Methods**: 
        *   `deposit(double amount)`
        *   `withdraw(double amount)` (behavior depends on AccountStatus state)
        *   `getBalance()`: double
        *   `getAccountId()`: String
        *   `getOwner()`: User
        *   `getStatus()`: AccountStatus
        *   `setStatus(AccountStatus status)`
        *   `getPreferences()`: AccountPreferences
        *   `setPreferences(AccountPreferences preferences)`
        *   `addObserver(AccountObserver observer)`
        *   `removeObserver(AccountObserver observer)`
        *   `notifyObservers(String messageType, Object data)`
        *   Abstract methods to be implemented by subclasses if specific behavior is needed beyond state pattern handling for common operations.

*   **`BasicTimeAccount` (Extends `TimeAccount`)**: Standard account for daily transactions.
    *   Specific attributes or methods if any, otherwise inherits all from `TimeAccount`.

*   **`InvestorAccount` (Extends `TimeAccount`)**: Account for investments, potentially with different interest calculation mechanisms.
    *   **Attributes**: `currentInvestments` (List<Investment>), `interestRate` (double).
    *   **Methods**: `makeInvestment(InvestmentDetails details)`, `calculateInterest()`.

*   **`LoanAccount` (Extends `TimeAccount`)**: Account for managing loans.
    *   **Attributes**: `loanAmount` (double), `interestRate` (double), `repaymentStrategy` (LoanRepaymentStrategy - Strategy Pattern), `dueDate` (Timestamp).
    *   **Methods**: `disburseLoan()`, `makeRepayment(double amount)`, `setRepaymentStrategy(LoanRepaymentStrategy strategy)`, `calculateRemainingBalance()`.

*   **`AccountPreferences`**: Holds customizable preferences for an account. Built using Builder Pattern.
    *   **Attributes**: `transactionLimitPerDay` (double), `notificationPreferences` (Map<String, Boolean>), `preferredInterestRateType` (String for Investor/Loan accounts).
    *   **Methods**: Getters for attributes.

*   **`AccountPreferencesBuilder` (Builder Pattern)**: Constructs `AccountPreferences` objects.
    *   **Methods**: `withTransactionLimit(double limit)`, `withNotificationPreference(String type, boolean enabled)`, `build()`: AccountPreferences.

### 1.3. Transaction Management

*   **`Transaction` (Interface - Command Pattern)**: Defines the contract for all transaction types.
    *   **Attributes (Conceptual, implemented by concrete classes)**: `transactionId` (String, PK), `timestamp` (Timestamp), `amount` (double), `status` (TransactionStatus: PENDING, COMPLETED, FAILED, REVERTED).
    *   **Methods**: `execute()`, `undo()`, `getTransactionDetails()`: String.

*   **`TransferTransaction` (Implements `Transaction`)**: For transferring time between two accounts.
    *   **Attributes**: `fromAccount` (TimeAccount), `toAccount` (TimeAccount), `amount` (double).
    *   **Methods**: Implements `execute()`, `undo()`, `getTransactionDetails()`.

*   **`LoanTransaction` (Implements `Transaction`)**: For disbursing or repaying loans.
    *   **Attributes**: `loanAccount` (LoanAccount), `transactionType` (DISBURSEMENT, REPAYMENT), `amount` (double).
    *   **Methods**: Implements `execute()`, `undo()`, `getTransactionDetails()`.

*   **`InvestmentTransaction` (Implements `Transaction`)**: For making or divesting investments.
    *   **Attributes**: `investorAccount` (InvestorAccount), `investmentDetails` (Object), `transactionType` (INVEST, DIVEST), `amount` (double).
    *   **Methods**: Implements `execute()`, `undo()`, `getTransactionDetails()`.

*   **`ChronoLedger` (Singleton Pattern)**: A single, central ledger for recording all transactions securely.
    *   **Attributes**: `instance` (static ChronoLedger), `transactionLog` (List<TransactionRecord> - or directly interacts with DB).
    *   **Methods**: `getInstance()`: ChronoLedger, `recordTransaction(Transaction transaction, boolean success)`: void, `getTransactionHistory(String accountId)`: List<TransactionRecord>, `getTransactionById(String transactionId)`: TransactionRecord.
    *   `TransactionRecord` would be a simple DTO or internal class holding transaction details for logging.

### 1.4. System Services and Facades

*   **`BankingFacade` (Facade Pattern)**: Provides a simplified, unified interface for common banking operations.
    *   **Methods**:
        *   `createUser(String username, String password, String email)`: User
        *   `loginUser(String username, String password)`: User
        *   `createAccount(User owner, String accountType, AccountPreferences preferences, double initialDeposit)`: TimeAccount
        *   `getAccountBalance(String accountId)`: double
        *   `getAccountDetails(String accountId)`: TimeAccount
        *   `performTransfer(String fromAccountId, String toAccountId, double amount)`: Transaction
        *   `requestLoan(String accountId, double amount, LoanRepaymentStrategy strategy)`: LoanAccount
        *   `makeInvestment(String accountId, double amount, InvestmentDetails details)`: InvestorAccount
        *   `getTransactionHistory(String accountId)`: List<TransactionRecord>
        *   `undoTransaction(String transactionId)`: boolean

*   **`AccountFactory` (Factory Pattern)**: Dynamically creates different types of accounts.
    *   **Methods**: `createTimeAccount(String type, User owner, AccountPreferences preferences, double initialDeposit)`: TimeAccount. (Types: "BASIC", "INVESTOR", "LOAN").

## 2. Design Pattern Implementation Details

### 2.1. Creational Design Patterns

*   **Factory Pattern**: `AccountFactory` will be used by `BankingFacade` to instantiate `BasicTimeAccount`, `InvestorAccount`, or `LoanAccount` based on a type string.
*   **Singleton Pattern**: `ChronoLedger` ensures only one instance exists, providing a global point of access for transaction recording and retrieval. This is crucial for maintaining the integrity of the transaction log.
*   **Builder Pattern**: `AccountPreferencesBuilder` allows for step-by-step construction of `AccountPreferences` objects. This is useful as account preferences might have many optional parameters (e.g., transaction limits, notification settings, interest rate preferences).
    ```java
    // Example Usage
    AccountPreferences prefs = new AccountPreferences.Builder()
                                .withTransactionLimit(1000.0)
                                .withNotificationPreference("LOW_BALANCE", true)
                                .build();
    ```

### 2.2. Structural Design Patterns

*   **Facade Pattern**: `BankingFacade` simplifies interactions with the complex subsystem of accounts, transactions, and ledger. Clients interact with the facade rather than individual components.
*   **Adapter Pattern**: To integrate with hypothetical legacy banking systems. An `LegacySystemAdapter` class would implement a `ChronoBankSystemInterface` and translate calls to the legacy system's API. This is more conceptual for this project unless a specific legacy API is to be mocked.
*   **Decorator Pattern**: `TransactionRuleDecorator` (abstract) will allow dynamic addition of rules to `Transaction` objects.
    *   **`TransactionRuleDecorator` (Abstract Decorator)**: Extends/Implements `Transaction`.
        *   **Attributes**: `wrappedTransaction` (Transaction).
        *   **Constructor**: `TransactionRuleDecorator(Transaction transaction)`.
        *   **Methods**: `execute()` (calls `wrappedTransaction.execute()` and adds own logic), `undo()` (calls `wrappedTransaction.undo()`).
    *   **`TimeTaxDecorator` (Concrete Decorator)**: Adds a time tax deduction to a transaction.
    *   **`BonusTimeDecorator` (Concrete Decorator)**: Adds bonus time to a transaction.
    ```java
    // Example Usage
    Transaction transfer = new TransferTransaction(acc1, acc2, 100);
    Transaction taxedTransfer = new TimeTaxDecorator(transfer, 0.05); // 5% tax
    taxedTransfer.execute();
    ```

### 2.3. Behavioral Design Patterns

*   **Observer Pattern**: `TimeAccount` acts as the Subject. Observers like `NotificationService` and `FraudDetectionService` can register with accounts.
    *   **`AccountObserver` (Interface)**: `update(TimeAccount account, String messageType, Object data)`.
    *   **`NotificationService` (Implements `AccountObserver`)**: Sends notifications to users (e.g., email, SMS mock) on events like low balance, large transactions, loan repayment deadlines.
    *   **`FraudDetectionService` (Implements `AccountObserver`)**: Monitors account activity for suspicious patterns upon notification of certain transaction types or amounts.

*   **Strategy Pattern**: `LoanAccount` will use a `LoanRepaymentStrategy` interface to define different loan repayment algorithms.
    *   **`LoanRepaymentStrategy` (Interface)**: `calculateRepayment(double principal, double interestRate, int term)`: double, `getStrategyName()`: String.
    *   **`FixedTimeRepaymentStrategy` (Concrete Strategy)**: Implements fixed repayment amounts.
    *   **`DynamicInterestRepaymentStrategy` (Concrete Strategy)**: Implements repayments based on dynamic interest rates (potentially influenced by `TimeMarketService`).

*   **Command Pattern**: Each `Transaction` (e.g., `TransferTransaction`) is a command object. It encapsulates a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.
    *   The `execute()` method performs the action.
    *   The `undo()` method reverts the action. A `TransactionHistory` service could manage a stack of executed commands for undo functionality.

*   **State Pattern**: Manages the `status` of a `TimeAccount` (e.g., Active, Overdrawn, Frozen).
    *   **`AccountStatus` (Interface/Abstract Class)**: Defines methods that depend on the state, e.g., `handleWithdrawal(TimeAccount account, double amount)`, `handleDeposit(TimeAccount account, double amount)`.
    *   **`AccountActiveState` (Concrete State)**: Allows all normal operations.
    *   **`AccountOverdrawnState` (Concrete State)**: May restrict withdrawals or apply fees.
    *   **`AccountFrozenState` (Concrete State)**: Restricts most operations, typically due to fraud detection.
    *   `TimeAccount` will have a `currentState` field of type `AccountStatus` and delegate state-specific behavior to it.

## 3. Additional Features & Challenges - Architectural Considerations

*   **Fraud Detection & Prevention**: `FraudDetectionService` will analyze transaction patterns. It can be an Observer to `TimeAccount` or transactions. It might use rules or simple heuristics (e.g., rapid large transfers, transfers to new accounts with no history).
    *   Upon detecting fraud, it can trigger the `AccountStatus` to change to `FrozenState` via the `TimeAccount`'s `setStatus` method.

*   **Loan & Investment System**: `InvestorAccount` and `LoanAccount` will handle these. Interest rates can be influenced by a `TimeMarketService`.
    *   **`TimeMarketService`**: A conceptual service that provides current market conditions (e.g., base interest rate for time). This could be a simple mock that returns fluctuating values or reads from a configuration.
    *   Auto-adjusting interest rates for loans can be part of the `DynamicInterestRepaymentStrategy` which consults `TimeMarketService`.

*   **Access Control & Security**: `AccessControlService` will be responsible for this.
    *   **`AccessControlService`**: Methods like `canPerformTransaction(User user, TransactionType type, double amount)` would check user's `reputationScore` and `riskScore` against predefined rules.
    *   This service would be invoked by `BankingFacade` before executing sensitive operations.

## 4. Database Schema (Conceptual - PostgreSQL)

*   **`users`**
    *   `user_id` VARCHAR(255) PRIMARY KEY
    *   `username` VARCHAR(255) UNIQUE NOT NULL
    *   `hashed_password` VARCHAR(255) NOT NULL
    *   `email` VARCHAR(255) UNIQUE NOT NULL
    *   `reputation_score` INT DEFAULT 0
    *   `risk_score` INT DEFAULT 0
    *   `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

*   **`accounts`**
    *   `account_id` VARCHAR(255) PRIMARY KEY
    *   `user_id` VARCHAR(255) REFERENCES `users`(`user_id`)
    *   `account_type` VARCHAR(50) NOT NULL (e.g., 'BASIC', 'INVESTOR', 'LOAN')
    *   `balance` DECIMAL(15, 2) NOT NULL DEFAULT 0.00
    *   `status` VARCHAR(50) NOT NULL (e.g., 'ACTIVE', 'OVERDRAWN', 'FROZEN')
    *   `preferences_json` JSONB (for AccountPreferences)
    *   `creation_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    *   `loan_amount` DECIMAL(15,2) NULL (for LoanAccount)
    *   `loan_interest_rate` DECIMAL(5,4) NULL (for LoanAccount)
    *   `loan_due_date` TIMESTAMP NULL (for LoanAccount)
    *   `investor_interest_rate` DECIMAL(5,4) NULL (for InvestorAccount)

*   **`transactions`**
    *   `transaction_id` VARCHAR(255) PRIMARY KEY
    *   `transaction_type` VARCHAR(50) NOT NULL (e.g., 'TRANSFER', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT', 'INVESTMENT', 'INTEREST_ACCRUAL')
    *   `from_account_id` VARCHAR(255) REFERENCES `accounts`(`account_id`) NULL
    *   `to_account_id` VARCHAR(255) REFERENCES `accounts`(`account_id`) NULL
    *   `amount` DECIMAL(15, 2) NOT NULL
    *   `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    *   `status` VARCHAR(50) NOT NULL (e.g., 'PENDING', 'COMPLETED', 'FAILED', 'REVERTED')
    *   `description` TEXT NULL

*   **`transaction_log` (Can be merged with `transactions` or be a separate audit log if `ChronoLedger` needs more detail)**
    *   `log_id` SERIAL PRIMARY KEY
    *   `transaction_id` VARCHAR(255) REFERENCES `transactions`(`transaction_id`)
    *   `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    *   `details` TEXT (e.g., snapshot of accounts before/after, or specific event description)

*   **`investments` (For InvestorAccount)**
    *   `investment_id` VARCHAR(255) PRIMARY KEY
    *   `account_id` VARCHAR(255) REFERENCES `accounts`(`account_id`)
    *   `investment_type` VARCHAR(100)
    *   `amount_invested` DECIMAL(15,2)
    *   `start_date` TIMESTAMP
    *   `end_date` TIMESTAMP NULL
    *   `current_value` DECIMAL(15,2)
    *   `status` VARCHAR(50) (e.g., 'ACTIVE', 'MATURED', 'SOLD')

## 5. Java Project Structure (Conceptual)

```
chrono-bank/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── chronobank/
│   │   │           ├── model/
│   │   │           │   ├── user/
│   │   │           │   │   └── User.java
│   │   │           │   ├── account/
│   │   │           │   │   ├── TimeAccount.java
│   │   │           │   │   ├── BasicTimeAccount.java
│   │   │           │   │   ├── InvestorAccount.java
│   │   │           │   │   ├── LoanAccount.java
│   │   │           │   │   ├── AccountPreferences.java
│   │   │           │   │   └── AccountPreferencesBuilder.java
│   │   │           │   ├── transaction/
│   │   │           │   │   ├── Transaction.java
│   │   │           │   │   ├── TransferTransaction.java
│   │   │           │   │   ├── LoanTransaction.java
│   │   │           │   │   ├── InvestmentTransaction.java
│   │   │           │   │   └── TransactionRecord.java
│   │   │           │   └── common/
│   │   │           │       └── Investment.java // (or InvestmentDetails)
│   │   │           ├── service/
│   │   │           │   ├── BankingFacade.java
│   │   │           │   ├── ChronoLedger.java
│   │   │           │   ├── AccountFactory.java
│   │   │           │   ├── NotificationService.java
│   │   │           │   ├── FraudDetectionService.java
│   │   │           │   ├── AccessControlService.java
│   │   │           │   └── TimeMarketService.java
│   │   │           ├── pattern/
│   │   │           │   ├── command/ (Transaction itself is command)
│   │   │           │   ├── state/
│   │   │           │   │   ├── AccountStatus.java
│   │   │           │   │   ├── AccountActiveState.java
│   │   │           │   │   ├── AccountOverdrawnState.java
│   │   │           │   │   └── AccountFrozenState.java
│   │   │           │   ├── strategy/
│   │   │           │   │   ├── LoanRepaymentStrategy.java
│   │   │           │   │   ├── FixedTimeRepaymentStrategy.java
│   │   │           │   │   └── DynamicInterestRepaymentStrategy.java
│   │   │           │   ├── observer/
│   │   │           │   │   └── AccountObserver.java
│   │   │           │   └── decorator/
│   │   │           │       ├── TransactionRuleDecorator.java
│   │   │           │       ├── TimeTaxDecorator.java
│   │   │           │       └── BonusTimeDecorator.java
│   │   │           ├── db/
│   │   │           │   └── DatabaseConnector.java // (JDBC connection helper)
│   │   │           ├── dao/
│   │   │           │   ├── UserDao.java
│   │   │           │   ├── AccountDao.java
│   │   │           │   └── TransactionDao.java
│   │   │           ├── util/
│   │   │           │   └── IdGenerator.java
│   │   │           └── Main.java // (Application entry point for CLI testing)
│   │   └── resources/
│   │       └── db_schema.sql // (SQL for creating tables)
│   └── test/
│       └── java/
│           └── com/
│               └── chronobank/ // (Unit and integration tests)
├── pom.xml (if using Maven) or build.gradle (if using Gradle)
└── README.md
```

## 6. Optional GUI (Swing)

If implemented, a separate package `com.chronobank.gui` would contain Swing components. This would interact with the `BankingFacade`.

*   `MainDashboardFrame`
*   `LoginPanel`
*   `AccountViewPanel`
*   `TransactionHistoryPanel`
*   `FraudAlertPopup`

This GUI part is optional and will be considered after the core backend logic is stable.

This architecture provides a modular and extensible foundation for the ChronoBank system. Each component has clear responsibilities, and the use of design patterns promotes flexibility and maintainability. The next step will be to define the PostgreSQL database schema in more detail and then proceed with the Java implementation of these classes and their interactions.

