package com.chronobank.model.transaction;

import com.chronobank.model.account.LoanAccount;
import com.chronobank.model.account.TimeAccount; 

import java.sql.Timestamp;
import java.util.UUID;

public class LoanTransaction implements Transaction {
    private String transactionId;
    private Timestamp timestamp;
    private double amount;
    private TransactionStatus status;
    private String description;

    private LoanAccount loanAccount;
    private TimeAccount targetAccountForDisbursement; 
    private LoanTransactionType loanTransactionType; 

    public enum LoanTransactionType {
        DISBURSEMENT, 
        REPAYMENT    
    }

    public LoanTransaction(LoanAccount loanAccount, LoanTransactionType type, double amount, TimeAccount targetAccountForDisbursement) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.loanAccount = loanAccount;
        this.loanTransactionType = type;
        this.amount = amount;
        this.targetAccountForDisbursement = targetAccountForDisbursement; 
        this.status = TransactionStatus.PENDING;
        this.description = type.name() + " for loan " + loanAccount.getAccountId() + " amount " + amount;
    }

    
    public LoanTransaction(LoanAccount loanAccount, LoanTransactionType type, double amount) {
        this(loanAccount, type, amount, null);
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public LoanAccount getLoanAccount() {
        return loanAccount;
    }

    public LoanTransactionType getLoanTransactionType() {
        return loanTransactionType;
    }

    public TimeAccount getTargetAccountForDisbursement() {
        return targetAccountForDisbursement;
    }

    @Override
    public void execute() throws Exception {
        if (loanAccount == null) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Loan account is null.";
            throw new IllegalArgumentException("Loan account cannot be null.");
        }
        if (amount <= 0) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Transaction amount must be positive.";
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }

        try {
            switch (loanTransactionType) {
                case DISBURSEMENT:
                    if (targetAccountForDisbursement == null) {
                        this.status = TransactionStatus.FAILED;
                        this.description += " | Failed: Target account for disbursement is null.";
                        throw new IllegalArgumentException("Target account for disbursement cannot be null for a disbursement transaction.");
                    }
                    
                    
                    targetAccountForDisbursement.deposit(amount);
                    this.description += " | Disbursed to account " + targetAccountForDisbursement.getAccountId();
                    break;
                case REPAYMENT:
                    
                    loanAccount.makeRepayment(amount);
                    this.description += " | Repayment made on loan account.";
                    break;
                default:
                    this.status = TransactionStatus.FAILED;
                    this.description += " | Failed: Unknown loan transaction type.";
                    throw new IllegalStateException("Unknown loan transaction type: " + loanTransactionType);
            }
            this.status = TransactionStatus.COMPLETED;
            this.description += " | Completed successfully.";
            System.out.println("Loan transaction executed: " + getTransactionDetails());
            loanAccount.notifyObservers("LOAN_TRANSACTION_" + loanTransactionType.name(), this);

        } catch (IllegalStateException | IllegalArgumentException e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: " + e.getMessage();
            System.err.println("Loan transaction failed for " + transactionId + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: An unexpected error occurred - " + e.getMessage();
            System.err.println("Loan transaction failed for " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Loan transaction execution failed unexpectedly.", e);
        }
    }

    @Override
    public void undo() throws Exception {
        if (this.status != TransactionStatus.COMPLETED) {
            this.description += " | Undo Failed: Transaction was not completed successfully.";
            throw new IllegalStateException("Cannot undo loan transaction that was not completed. Status: " + this.status);
        }

        try {
            switch (loanTransactionType) {
                case DISBURSEMENT:
                    
                    if (targetAccountForDisbursement == null) {
                         throw new IllegalStateException("Cannot undo disbursement: target account was null.");
                    }
                    targetAccountForDisbursement.withdraw(amount);
                    
                    System.out.println("Conceptual undo of loan disbursement for " + amount);
                    this.description += " | Disbursement of " + amount + " conceptually reverted.";
                    break;
                case REPAYMENT:
                    
                    System.out.println("Conceptual undo of loan repayment for " + amount);
                    this.description += " | Repayment of " + amount + " conceptually reverted.";
                    break;
                default:
                    throw new IllegalStateException("Cannot undo unknown loan transaction type: " + loanTransactionType);
            }
            this.status = TransactionStatus.REVERTED;
            this.description += " | Reverted successfully.";
            System.out.println("Loan transaction reverted: " + getTransactionDetails());
            loanAccount.notifyObservers("LOAN_TRANSACTION_UNDO_" + loanTransactionType.name(), this);
        } catch (IllegalStateException e) {
            this.description += " | Undo Failed: " + e.getMessage();
            System.err.println("Undo loan transaction failed for " + transactionId + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.description += " | Undo Failed: An unexpected error occurred during undo - " + e.getMessage();
            System.err.println("Undo loan transaction failed for " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Loan transaction undo failed unexpectedly.", e);
        }
        
    }

    @Override
    public String getTransactionDetails() {
        return "LoanTransaction{" +
                "transactionId=\'" + transactionId + "\', " +
                "timestamp=" + timestamp +
                ", loanAccountId=\'" + (loanAccount != null ? loanAccount.getAccountId() : "N/A") + "\', " +
                "type=" + loanTransactionType +
                ", amount=" + amount +
                (targetAccountForDisbursement != null ? ", targetDisbursementAccountId=\'" + targetAccountForDisbursement.getAccountId() + "\', " : "") +
                ", status=" + status +
                ", description=\'" + description + "\"" +
                '}';
    }
}

