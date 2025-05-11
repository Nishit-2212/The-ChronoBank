package com.chronobank.pattern.decorator;

import com.chronobank.model.transaction.Transaction;
import java.sql.Timestamp;


public abstract class TransactionRuleDecorator implements Transaction {
    protected Transaction wrappedTransaction;

    public TransactionRuleDecorator(Transaction transaction) {
        this.wrappedTransaction = transaction;
    }

    @Override
    public String getTransactionId() {
        return wrappedTransaction.getTransactionId();
    }

    @Override
    public Timestamp getTimestamp() {
        return wrappedTransaction.getTimestamp();
    }

    @Override
    public double getAmount() {
        
        return wrappedTransaction.getAmount();
    }

    @Override
    public TransactionStatus getStatus() {
        return wrappedTransaction.getStatus();
    }

    @Override
    public void setStatus(TransactionStatus status) {
        wrappedTransaction.setStatus(status);
    }

    @Override
    public String getDescription() {
        return wrappedTransaction.getDescription(); 
    }

    @Override
    public void setDescription(String description) {
        wrappedTransaction.setDescription(description);
    }

    @Override
    public void execute() throws Exception {
        
        wrappedTransaction.execute();
    }

    @Override
    public void undo() throws Exception {
        
        wrappedTransaction.undo();
    }

    @Override
    public String getTransactionDetails() {
        
        return wrappedTransaction.getTransactionDetails();
    }
}

