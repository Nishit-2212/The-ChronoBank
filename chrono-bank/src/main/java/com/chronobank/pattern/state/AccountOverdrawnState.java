package com.chronobank.pattern.state;

import com.chronobank.model.account.TimeAccount;

public class AccountOverdrawnState implements AccountStatus {
    private static final long serialVersionUID = 1L;
    private static final String STATUS_NAME = "OVERDRAWN";

    @Override
    public void handleDeposit(TimeAccount account, double amount) {
        if (amount <= 0) {
            System.err.println("Deposit amount must be positive.");
            return;
        }
        account.performDeposit(amount);
        System.out.println("Account " + account.getAccountId() + " (Overdrawn): Deposited " + amount + ". New balance: " + account.getBalance());
        if (account.getBalance() >= 0) {
            account.setAccountStatus(new AccountActiveState());
            System.out.println("Account " + account.getAccountId() + " status changed to ACTIVE.");
        }
    }

    @Override
    public void handleWithdrawal(TimeAccount account, double amount) throws IllegalStateException {
        System.err.println("Account " + account.getAccountId() + " is OVERDRAWN. Withdrawals are not permitted.");
        throw new IllegalStateException("Account is overdrawn. Withdrawals are not permitted.");
    }

    @Override
    public String getStatusName() {
        return STATUS_NAME;
    }

    @Override
    public String toString() {
        return "AccountStatus: Overdrawn";
    }
}

