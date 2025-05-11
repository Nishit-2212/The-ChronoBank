package com.chronobank.model.account;

import com.chronobank.model.user.User;
import com.chronobank.pattern.observer.AccountObserver;
import com.chronobank.pattern.state.AccountStatus;
import com.chronobank.pattern.state.AccountActiveState; 
import com.chronobank.util.IdGenerator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TimeAccount {
    protected final String accountId;
    protected User owner;
    protected double balance;
    protected AccountStatus currentStatus;
    protected AccountPreferences preferences;
    protected final Timestamp creationDate;
    protected List<AccountObserver> observers; 
    protected String accountType; 

   
    public TimeAccount(User owner, AccountPreferences preferences, double initialDeposit, String accountType) {
        this(IdGenerator.generateUniqueId(), owner, preferences, initialDeposit, accountType, new Timestamp(System.currentTimeMillis()));
    }

    public TimeAccount(String accountId, User owner, AccountPreferences preferences, double initialBalanceFromDB, String accountType, Timestamp creationDate) {
        this.accountId = accountId;
        this.owner = owner;
        this.preferences = (preferences != null) ? preferences : new AccountPreferences.Builder().build();
        this.balance = 0; 
        this.creationDate = creationDate;
        this.observers = new ArrayList<>();
        this.currentStatus = new AccountActiveState(); 
        this.accountType = accountType;
        
       
        if (initialBalanceFromDB != 0) {
            this.balance = initialBalanceFromDB; 
        }
    }

    
    public void addObserver(AccountObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(AccountObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String messageType, Object data) {
        List<AccountObserver> observersCopy = new ArrayList<>(observers);
        for (AccountObserver observer : observersCopy) {
            observer.update(this, messageType, data);
        }
    }

   
    public void deposit(double amount) {
        currentStatus.handleDeposit(this, amount);
        notifyObservers("DEPOSIT_COMPLETED", amount);
        if (this.balance >= 0 && "OVERDRAWN".equals(this.currentStatus.getStatusName())) {
             setAccountStatus(new AccountActiveState());
        }
    }

    public void withdraw(double amount) throws IllegalStateException {
        currentStatus.handleWithdrawal(this, amount); 
        notifyObservers("WITHDRAWAL_COMPLETED", amount);

        
        final double LOW_BALANCE_THRESHOLD = 100.0;
        if (this.balance < LOW_BALANCE_THRESHOLD) {
            if (this.preferences != null && this.preferences.getNotificationPreference("LOW_BALANCE")) {
                notifyObservers("LOW_BALANCE", this.balance);
            }
        }
    }

   
    public void performDeposit(double amount) {
        this.balance += amount;
    }

    public void performWithdrawal(double amount) {
        this.balance -= amount;
    }

   
    public String getAccountId() {
        return accountId;
    }

    public User getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getAccountStatus() {
        return currentStatus;
    }

    public AccountPreferences getPreferences() {
        return preferences;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public String getAccountType() {
        return accountType;
    }

   
    public void setAccountStatus(AccountStatus newStatus) {
        if (newStatus != null && this.currentStatus != newStatus) {
            this.currentStatus = newStatus;
            System.out.println("Account " + accountId + " status changed to: " + newStatus.getStatusName());
            notifyObservers("STATUS_CHANGE", newStatus.getStatusName());
        }
    }

    public void setPreferences(AccountPreferences preferences) {
        this.preferences = preferences;
        notifyObservers("PREFERENCES_UPDATED", preferences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeAccount that = (TimeAccount) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "TimeAccount{" +
                "accountId=\'" + accountId + "\', " +
                "owner=" + (owner != null ? owner.getUsername() : "null") +
                ", balance=" + balance +
                ", status=" + (currentStatus != null ? currentStatus.getStatusName() : "null") +
                ", accountType=\'" + accountType + "\', " +
                "creationDate=" + creationDate +
                '}';
    }
}

