package com.chronobank.model.transaction;

import com.chronobank.model.account.TimeAccount;
import java.sql.Timestamp;
import java.util.UUID;

public class TransferTransaction implements Transaction {
    private String transactionId;
    private Timestamp timestamp;
    private double amount;
    private TransactionStatus status;
    private String description;

    private TimeAccount fromAccount;
    private TimeAccount toAccount;

    public TransferTransaction(TimeAccount fromAccount, TimeAccount toAccount, double amount) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.description = "Transfer from " + (fromAccount != null ? fromAccount.getAccountId() : "N/A") + 
                           " to " + (toAccount != null ? toAccount.getAccountId() : "N/A") + 
                           " for amount " + amount;
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

    public TimeAccount getFromAccount() {
        return fromAccount;
    }

    public TimeAccount getToAccount() {
        return toAccount;
    }

    @Override
    public void execute() throws Exception {
        if (fromAccount == null || toAccount == null) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Source or destination account is null.";
            throw new IllegalArgumentException("Source or destination account cannot be null.");
        }
        if (amount <= 0) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Transaction amount must be positive.";
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
        if (fromAccount.equals(toAccount)){
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Source and destination accounts cannot be the same.";
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }

        try {
            
            fromAccount.withdraw(amount); 
            toAccount.deposit(amount);   
            
            this.status = TransactionStatus.COMPLETED;
            this.description += " | Completed successfully.";
            System.out.println("Transfer executed: " + getTransactionDetails());

           
            fromAccount.notifyObservers("TRANSFER_OUT", this);
            toAccount.notifyObservers("TRANSFER_IN", this);

        } catch (IllegalStateException e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: " + e.getMessage();
            System.err.println("Transfer failed for transaction " + transactionId + ": " + e.getMessage());
            
            throw e; 
        } catch (Exception e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: An unexpected error occurred - " + e.getMessage();
            System.err.println("Transfer failed for transaction " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Transfer execution failed unexpectedly.", e);
        }
    }

    @Override
    public void undo() throws Exception {
        if (this.status != TransactionStatus.COMPLETED) {
            this.description += " | Undo Failed: Transaction was not completed successfully.";
            throw new IllegalStateException("Cannot undo transaction that was not completed successfully. Current status: " + this.status);
        }

      
        try {
            
            toAccount.withdraw(amount);
            fromAccount.deposit(amount);
            
            this.status = TransactionStatus.REVERTED;
            this.description += " | Reverted successfully.";
            System.out.println("Transfer reverted: " + getTransactionDetails());

            
            fromAccount.notifyObservers("TRANSFER_UNDO_IN", this);
            toAccount.notifyObservers("TRANSFER_UNDO_OUT", this);

        } catch (IllegalStateException e) {
            this.description += " | Undo Failed: " + e.getMessage();
            System.err.println("Undo transfer failed for transaction " + transactionId + ": " + e.getMessage());
           
            throw e; 
        } catch (Exception e) {
            this.description += " | Undo Failed: An unexpected error occurred during undo - " + e.getMessage();
            System.err.println("Undo transfer failed for transaction " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Transfer undo failed unexpectedly.", e);
        }
    }

    @Override
    public String getTransactionDetails() {
        return "TransferTransaction{" +
                "transactionId=\'" + transactionId + "\', " +
                "timestamp=" + timestamp +
                ", fromAccountId=\'" + (fromAccount != null ? fromAccount.getAccountId() : "N/A") + "\', " +
                "toAccountId=\'" + (toAccount != null ? toAccount.getAccountId() : "N/A") + "\', " +
                "amount=" + amount +
                ", status=" + status +
                ", description=\'" + description + "\'" +
                '}';
    }
}

