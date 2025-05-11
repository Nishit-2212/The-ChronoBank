package com.chronobank.model.transaction;

import com.chronobank.model.account.InvestorAccount;
import com.chronobank.model.common.Investment;

import java.sql.Timestamp;
import java.util.UUID;

public class InvestmentTransaction implements Transaction {
    private String transactionId;
    private Timestamp timestamp;
    private double amount;
    private TransactionStatus status;
    private String description;

    private InvestorAccount investorAccount;
    private Investment investmentDetails; 
    private InvestmentTransactionType investmentTransactionType;

    public enum InvestmentTransactionType {
        INVEST, 
        DIVEST  
    }

    public InvestmentTransaction(InvestorAccount investorAccount, InvestmentTransactionType type, double amount, Investment investmentDetails) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.investorAccount = investorAccount;
        this.investmentTransactionType = type;
        this.amount = amount; 
        this.investmentDetails = investmentDetails; 
        this.status = TransactionStatus.PENDING;
        this.description = type.name() + " for investment " + 
                           (investmentDetails != null ? investmentDetails.getInvestmentId() : "N/A") + 
                           " in account " + investorAccount.getAccountId() + " for amount " + amount;
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

    public InvestorAccount getInvestorAccount() {
        return investorAccount;
    }

    public Investment getInvestmentDetails() {
        return investmentDetails;
    }

    public InvestmentTransactionType getInvestmentTransactionType() {
        return investmentTransactionType;
    }

    @Override
    public void execute() throws Exception {
        if (investorAccount == null) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Investor account is null.";
            throw new IllegalArgumentException("Investor account cannot be null.");
        }
        if (investmentDetails == null) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Investment details are null.";
            throw new IllegalArgumentException("Investment details cannot be null.");
        }
        if (amount <= 0 && investmentTransactionType == InvestmentTransactionType.INVEST) {
            
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: Investment transaction amount must be positive for INVEST type.";
            throw new IllegalArgumentException("Investment transaction amount must be positive for INVEST type.");
        }

        try {
            switch (investmentTransactionType) {
                case INVEST:
                    
                    investorAccount.makeInvestment(investmentDetails); // amount is part of investmentDetails
                    this.description += " | Investment " + investmentDetails.getInvestmentId() + " made.";
                    break;
                case DIVEST:
                    
                    boolean divested = investorAccount.getCurrentInvestments().removeIf(inv -> inv.getInvestmentId().equals(investmentDetails.getInvestmentId()));
                    if (divested) {
                        investorAccount.deposit(this.amount); // Add proceeds to balance
                        investmentDetails.setStatus("SOLD"); // Update status of the local copy
                        this.description += " | Investment " + investmentDetails.getInvestmentId() + " divested. Proceeds: " + this.amount;
                    } else {
                        this.status = TransactionStatus.FAILED;
                        this.description += " | Failed: Investment " + investmentDetails.getInvestmentId() + " not found for divestment.";
                        throw new IllegalStateException("Investment not found for divestment.");
                    }
                    break;
                default:
                    this.status = TransactionStatus.FAILED;
                    this.description += " | Failed: Unknown investment transaction type.";
                    throw new IllegalStateException("Unknown investment transaction type: " + investmentTransactionType);
            }
            this.status = TransactionStatus.COMPLETED;
            this.description += " | Completed successfully.";
            System.out.println("Investment transaction executed: " + getTransactionDetails());
            investorAccount.notifyObservers("INVESTMENT_TRANSACTION_" + investmentTransactionType.name(), this);

        } catch (IllegalStateException | IllegalArgumentException e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: " + e.getMessage();
            System.err.println("Investment transaction failed for " + transactionId + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.status = TransactionStatus.FAILED;
            this.description += " | Failed: An unexpected error occurred - " + e.getMessage();
            System.err.println("Investment transaction failed for " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Investment transaction execution failed unexpectedly.", e);
        }
    }

    @Override
    public void undo() throws Exception {
        if (this.status != TransactionStatus.COMPLETED) {
            this.description += " | Undo Failed: Transaction was not completed successfully.";
            throw new IllegalStateException("Cannot undo investment transaction that was not completed. Status: " + this.status);
        }
        
        try {
            switch (investmentTransactionType) {
                case INVEST:
                    
                    boolean removed = investorAccount.getCurrentInvestments().removeIf(inv -> inv.getInvestmentId().equals(investmentDetails.getInvestmentId()));
                    if (removed) {
                        investorAccount.deposit(investmentDetails.getAmountInvested()); 
                        this.description += " | Investment " + investmentDetails.getInvestmentId() + " conceptually reverted.";
                    } else {
                        throw new IllegalStateException("Could not find investment to revert.");
                    }
                    break;
                case DIVEST:
                    
                    investorAccount.withdraw(this.amount); 
                    investmentDetails.setStatus("ACTIVE"); 
                    investorAccount.getCurrentInvestments().add(investmentDetails); 
                    this.description += " | Divestment of " + investmentDetails.getInvestmentId() + " conceptually reverted.";
                    break;
                default:
                    throw new IllegalStateException("Cannot undo unknown investment transaction type: " + investmentTransactionType);
            }
            this.status = TransactionStatus.REVERTED;
            this.description += " | Reverted successfully.";
            System.out.println("Investment transaction reverted: " + getTransactionDetails());
            investorAccount.notifyObservers("INVESTMENT_TRANSACTION_UNDO_" + investmentTransactionType.name(), this);
        } catch (IllegalStateException e) {
            this.description += " | Undo Failed: " + e.getMessage();
            System.err.println("Undo investment transaction failed for " + transactionId + ": " + e.getMessage());
            throw e;
        } catch (Exception e) {
            this.description += " | Undo Failed: An unexpected error occurred during undo - " + e.getMessage();
            System.err.println("Undo investment transaction failed for " + transactionId + " due to unexpected error: " + e.getMessage());
            throw new Exception("Investment transaction undo failed unexpectedly.", e);
        }
    }

    @Override
    public String getTransactionDetails() {
        return "InvestmentTransaction{" +
                "transactionId=\'" + transactionId + "\', " +
                "timestamp=" + timestamp +
                ", investorAccountId=\'" + (investorAccount != null ? investorAccount.getAccountId() : "N/A") + "\', " +
                "type=" + investmentTransactionType +
                ", amount=" + amount +
                ", investmentId=\'" + (investmentDetails != null ? investmentDetails.getInvestmentId() : "N/A") + "\', " +
                ", status=" + status +
                ", description=\'" + description + "\"" +
                '}';
    }
}

