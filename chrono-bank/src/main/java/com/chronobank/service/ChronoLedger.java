package com.chronobank.service;

import com.chronobank.model.transaction.Transaction;
import com.chronobank.model.transaction.TransactionRecord; // Assuming TransactionRecord DTO/class

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChronoLedger {
    private static volatile ChronoLedger instance;
    private final List<TransactionRecord> transactionLog;
    private final Map<String, TransactionRecord> transactionMap;

    private ChronoLedger() {
        this.transactionLog = Collections.synchronizedList(new ArrayList<>());
        this.transactionMap = new ConcurrentHashMap<>();
        if (instance != null) {
            throw new IllegalStateException("Singleton already constructed");
        }
    }

    public static ChronoLedger getInstance() {
        if (instance == null) {
            synchronized (ChronoLedger.class) {
                if (instance == null) {
                    instance = new ChronoLedger();
                }
            }
        }
        return instance;
    }

    /**
     * @param transaction this is for transaction to record.
     * @param success whether the transaction of execution was successful or not?.
     */
    public synchronized void recordTransaction(Transaction transaction, boolean success) {
        if (transaction == null) {
            System.err.println("ChronoLedger: Attempted to record a null transaction.");
            return;
        }

      
        TransactionRecord record = new TransactionRecord(
                transaction.getTransactionId(),
                transaction.getClass().getSimpleName(), 
                transaction.getAmount(),
                new Timestamp(System.currentTimeMillis()), 
                transaction.getStatus().name(), 
                transaction.getDescription(),
                getAccountIdFromTransaction(transaction, "FROM"),
                getAccountIdFromTransaction(transaction, "TO")
        );

        transactionLog.add(record);
        transactionMap.put(record.getTransactionId(), record);
        System.out.println("ChronoLedger: Recorded transaction - ID: " + record.getTransactionId() + ", Status: " + record.getStatus() + ", Success: " + success);
        
       
    }

    private String getAccountIdFromTransaction(Transaction tx, String role) {
        if (tx instanceof com.chronobank.model.transaction.TransferTransaction) {
            com.chronobank.model.transaction.TransferTransaction transfer = (com.chronobank.model.transaction.TransferTransaction) tx;
            if ("FROM".equals(role) && transfer.getFromAccount() != null) return transfer.getFromAccount().getAccountId();
            if ("TO".equals(role) && transfer.getToAccount() != null) return transfer.getToAccount().getAccountId();
        } else if (tx instanceof com.chronobank.model.transaction.LoanTransaction) {
            com.chronobank.model.transaction.LoanTransaction loanTx = (com.chronobank.model.transaction.LoanTransaction) tx;
            if (("FROM".equals(role) || "TO".equals(role)) && loanTx.getLoanAccount() != null) {
                 if (loanTx.getLoanTransactionType() == com.chronobank.model.transaction.LoanTransaction.LoanTransactionType.DISBURSEMENT) {
                     if ("FROM".equals(role)) return loanTx.getLoanAccount().getAccountId(); 
                     if ("TO".equals(role) && loanTx.getTargetAccountForDisbursement() != null) return loanTx.getTargetAccountForDisbursement().getAccountId();
                 } else if (loanTx.getLoanTransactionType() == com.chronobank.model.transaction.LoanTransaction.LoanTransactionType.REPAYMENT) {
                     if ("TO".equals(role)) return loanTx.getLoanAccount().getAccountId();
                 }
            }
        } else if (tx instanceof com.chronobank.model.transaction.InvestmentTransaction) {
            com.chronobank.model.transaction.InvestmentTransaction invTx = (com.chronobank.model.transaction.InvestmentTransaction) tx;
            if (("FROM".equals(role) || "TO".equals(role)) && invTx.getInvestorAccount() != null) {
                
                return invTx.getInvestorAccount().getAccountId(); 
            }
        }
        return null;
    }

    /**
     * @param accountId the ID of the account to get history for.
     * @return the list of TransactionRecords related to the account.
     */
    public synchronized List<TransactionRecord> getTransactionHistory(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return transactionLog.stream()
                .filter(record -> (record.getFromAccountId() != null && record.getFromAccountId().equals(accountId)) || 
                                 (record.getToAccountId() != null && record.getToAccountId().equals(accountId)))
                .collect(Collectors.toList());
    }

    /**
     * @param transactionId The ID of the transaction.
     * @return The TransactionRecord, or null if not found.
     */
    public synchronized TransactionRecord getTransactionById(String transactionId) {
        return transactionMap.get(transactionId);
    }

    /**
     * @return An unmodifiable list of all transaction records.
     */
    public synchronized List<TransactionRecord> getAllTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(transactionLog));
    }

    public synchronized void resetForTesting() {
        transactionLog.clear();
        transactionMap.clear();
    }
}

