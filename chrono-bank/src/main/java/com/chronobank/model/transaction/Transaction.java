package com.chronobank.model.transaction;

import java.sql.Timestamp;

public interface Transaction {
    String getTransactionId();
    Timestamp getTimestamp();
    double getAmount();
    TransactionStatus getStatus();
    void setStatus(TransactionStatus status);
    String getDescription();
    void setDescription(String description);

    /**
     * @throws Exception if the transaction cannot be completed.
     */
    void execute() throws Exception;

    /**
     * @throws Exception if the transaction cannot be undone or if undo is not supported.
     */
    void undo() throws Exception;

    /**
     * @return A string containing transaction details.
     */
    String getTransactionDetails();

    enum TransactionStatus {
        PENDING,   
        COMPLETED, 
        FAILED,   
        REVERTED 
    }
}

