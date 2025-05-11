package com.chronobank.service;

import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.transaction.Transaction;
import com.chronobank.pattern.observer.AccountObserver;
import com.chronobank.pattern.state.AccountFrozenState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FraudDetectionService implements AccountObserver {
    private static final double RAPID_TRANSFER_THRESHOLD_AMOUNT = 10000.0; // Amount considered large for rapid checks
    private static final long RAPID_TRANSFER_WINDOW_MS = 60 * 1000; // 1 minute window for rapid transfers
    private static final int MAX_RAPID_TRANSFERS_IN_WINDOW = 3;

    private final Map<String, List<Long>> accountRecentTransactionTimestamps;

    public FraudDetectionService() {
        this.accountRecentTransactionTimestamps = new ConcurrentHashMap<>();
    }

    @Override
    public void update(TimeAccount account, String messageType, Object data) {
        if (account == null || messageType == null || data == null) {
            return;
        }

        if (data instanceof Transaction) {
            Transaction transaction = (Transaction) data;
            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                checkRapidTransfers(account, transaction);
                checkLargeTransaction(account, transaction);
            }
        } else if ("STATUS_CHANGE".equals(messageType)) {
            if (AccountFrozenState.class.getSimpleName().toUpperCase().contains(((String)data).toUpperCase())) {
                System.out.println("FraudDetectionService: Account " + account.getAccountId() + " is now FROZEN. Noted.");
            }
        }
    }

    private void checkRapidTransfers(TimeAccount account, Transaction transaction) {
        if (transaction.getAmount() < RAPID_TRANSFER_THRESHOLD_AMOUNT / MAX_RAPID_TRANSFERS_IN_WINDOW) {
        }

        String accountId = account.getAccountId();
        long currentTime = System.currentTimeMillis();

        accountRecentTransactionTimestamps.putIfAbsent(accountId, new CopyOnWriteArrayList<>());
        List<Long> timestamps = accountRecentTransactionTimestamps.get(accountId);

        timestamps.add(currentTime);

        timestamps.removeIf(ts -> (currentTime - ts) > RAPID_TRANSFER_WINDOW_MS);

        if (timestamps.size() >= MAX_RAPID_TRANSFERS_IN_WINDOW) {
            System.out.println("FraudDetectionService: SUSPICIOUS ACTIVITY DETECTED for account " + accountId + 
                               " - Possible rapid transfers. Count: " + timestamps.size() + " within " + (RAPID_TRANSFER_WINDOW_MS / 1000) + "s.");
            account.setAccountStatus(new AccountFrozenState());
            account.notifyObservers("SUSPICIOUS_TRANSACTION", "Rapid transfer pattern detected. Account frozen.");
            timestamps.clear(); 
        }
    }

    private void checkLargeTransaction(TimeAccount account, Transaction transaction) {
        if (transaction.getAmount() >= RAPID_TRANSFER_THRESHOLD_AMOUNT) { 
            System.out.println("FraudDetectionService: Large transaction detected on account " + account.getAccountId() + 
                               ". Amount: " + transaction.getAmount() + ", Type: " + transaction.getClass().getSimpleName());
          
            if (transaction.getAmount() > RAPID_TRANSFER_THRESHOLD_AMOUNT * 5) { 
                 account.notifyObservers("SUSPICIOUS_TRANSACTION", "Unusually large transaction of " + transaction.getAmount() + " detected.");
                 
                 if(account.getOwner() != null) {
                    account.getOwner().setRiskScore(account.getOwner().getRiskScore() + 10); 
                    System.out.println("FraudDetectionService: Risk score for user " + account.getOwner().getUsername() + " increased.");
                 }
            }
        }
    }

    

    public void resetForTesting() {
        accountRecentTransactionTimestamps.clear();
    }
}

