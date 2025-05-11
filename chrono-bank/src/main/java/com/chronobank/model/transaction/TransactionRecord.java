package com.chronobank.model.transaction;

import java.sql.Timestamp;
import java.util.Objects;


public class TransactionRecord {
    private final String transactionId;
    private final String transactionType; 
    private final double amount;
    private final Timestamp loggedAt;
    private final String status; 
    private final String description;
    private final String fromAccountId; 
    private final String toAccountId;   

    public TransactionRecord(String transactionId, String transactionType, double amount, Timestamp loggedAt,
                             String status, String description, String fromAccountId, String toAccountId) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.loggedAt = loggedAt;
        this.status = status;
        this.description = description;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
    }

   
    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getLoggedAt() {
        return loggedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "transactionId=\'" + transactionId + "\', " +
                "transactionType=\'" + transactionType + "\', " +
                "amount=" + amount +
                ", loggedAt=" + loggedAt +
                ", status=\'" + status + "\', " +
                (fromAccountId != null ? ", fromAccountId=\'" + fromAccountId + "\', " : "") +
                (toAccountId != null ? ", toAccountId=\'" + toAccountId + "\', " : "") +
                "description=\'" + description + "\'" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRecord that = (TransactionRecord) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(loggedAt, that.loggedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, loggedAt);
    }
}

