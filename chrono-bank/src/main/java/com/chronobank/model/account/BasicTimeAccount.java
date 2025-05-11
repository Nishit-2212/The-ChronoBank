package com.chronobank.model.account;

import com.chronobank.model.user.User;
import java.sql.Timestamp;

public class BasicTimeAccount extends TimeAccount {

    public BasicTimeAccount(User owner, AccountPreferences preferences, double initialDeposit) {
        super(owner, preferences, initialDeposit, "BASIC");
    }

    public BasicTimeAccount(String accountId, User owner, AccountPreferences preferences, double balanceFromDB, Timestamp creationDate) {
        super(accountId, owner, preferences, balanceFromDB, "BASIC", creationDate);
    }

    @Override
    public void withdraw(double amount) throws IllegalStateException {
        System.out.println("Attempting withdrawal from BasicTimeAccount: " + getAccountId());
        super.withdraw(amount);
        if (getBalance() < 0 && !(getAccountStatus().getStatusName().equals("OVERDRAWN"))) {
        }
    }

    @Override
    public String toString() {
        return "BasicTimeAccount{" +
                "accountId=\'" + accountId + "\', " +
                "owner=" + (owner != null ? owner.getUsername() : "null") +
                ", balance=" + balance +
                ", status=" + (currentStatus != null ? currentStatus.getStatusName() : "null") +
                ", accountType=\'" + accountType + "\', " +
                "creationDate=" + creationDate +
                '}';
    }
}

