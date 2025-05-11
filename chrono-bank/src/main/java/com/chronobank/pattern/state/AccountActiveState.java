package com.chronobank.pattern.state;

import com.chronobank.model.account.TimeAccount;

public class AccountActiveState implements AccountStatus {
    private static final long serialVersionUID = 1L;
    private static final String STATUS_NAME = "ACTIVE";

    @Override
    public void handleDeposit(TimeAccount account, double amount) {
        if (amount <= 0) {
            System.err.println("Deposit amount must be positive.");
            return;
        }
        account.performDeposit(amount);
        System.out.println("Account " + account.getAccountId() + ": Deposited " + amount + ". New balance: " + account.getBalance());
    }

    @Override
    public void handleWithdrawal(TimeAccount account, double amount) throws IllegalStateException {
        if (amount <= 0) {
            System.err.println("Withdrawal amount must be positive.");
            return;
        }
        if (account.getBalance() >= amount) {
            account.performWithdrawal(amount);
            System.out.println("Account " + account.getAccountId() + ": Withdrew " + amount + ". New balance: " + account.getBalance());
        } else {
            System.err.println("Account " + account.getAccountId() + ": Insufficient funds for withdrawal of " + amount + ". Balance: " + account.getBalance());
            throw new IllegalStateException("Insufficient funds for withdrawal. Current balance: " + account.getBalance());
        }
    }

    @Override
    public String getStatusName() {
        return STATUS_NAME;
    }

    @Override
    public String toString() {
        return "AccountStatus: Active";
    }
}

