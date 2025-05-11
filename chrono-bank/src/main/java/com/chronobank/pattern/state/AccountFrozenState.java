package com.chronobank.pattern.state;

import com.chronobank.model.account.TimeAccount;

public class AccountFrozenState implements AccountStatus {
    private static final long serialVersionUID = 1L;
    private static final String STATUS_NAME = "FROZEN";

    @Override
    public void handleDeposit(TimeAccount account, double amount) {
        if (amount <= 0) {
            System.err.println("Deposit amount must be positive.");
            return;
        }
        account.performDeposit(amount);
        System.out.println("Account " + account.getAccountId() + " (FROZEN): Deposited " + amount + ". New balance: " + account.getBalance());
        System.out.println("Account remains FROZEN. Requires administrative review to unfreeze.");
    }

    @Override
    public void handleWithdrawal(TimeAccount account, double amount) throws IllegalStateException {
        System.err.println("Account " + account.getAccountId() + " is FROZEN. All withdrawals are suspended.");
        throw new IllegalStateException("Account is FROZEN. Withdrawals are not permitted.");
    }

    @Override
    public String getStatusName() {
        return STATUS_NAME;
    }

    @Override
    public String toString() {
        return "AccountStatus: Frozen";
    }
}

