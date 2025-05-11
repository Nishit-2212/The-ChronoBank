package com.chronobank.model.account;

import com.chronobank.model.user.User;
import com.chronobank.model.common.Investment;
import com.chronobank.dao.InvestmentDao;
import com.chronobank.dao.InvestmentDaoImpl;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InvestorAccount extends TimeAccount {
    private List<Investment> currentInvestments;
    private double configuredInterestRate;
    private final InvestmentDao investmentDao;

    public InvestorAccount(User owner, AccountPreferences preferences, double initialDeposit, double configuredInterestRate) {
        super(owner, preferences, initialDeposit, "INVESTOR");
        this.currentInvestments = new ArrayList<>();
        this.configuredInterestRate = configuredInterestRate;
        this.investmentDao = new InvestmentDaoImpl();
    }

    public InvestorAccount(String accountId, User owner, AccountPreferences preferences, double balanceFromDB, String accountType, Timestamp creationDate, double configuredInterestRate) {
        super(accountId, owner, preferences, balanceFromDB, accountType, creationDate);
        this.currentInvestments = new ArrayList<>();
        this.configuredInterestRate = configuredInterestRate;
        this.investmentDao = new InvestmentDaoImpl();
        
        // Load investments from database
        try {
            List<Investment> investments = investmentDao.findByAccountId(accountId);
            this.currentInvestments.addAll(investments);
        } catch (SQLException e) {
            System.err.println("Error loading investments for account " + accountId + ": " + e.getMessage());
        }
    }

    public void makeInvestment(Investment investment) {
        if (investment != null && investment.getAmountInvested() > 0) {
            if (this.balance >= investment.getAmountInvested()) {
                this.withdraw(investment.getAmountInvested());
                this.currentInvestments.add(investment);
                System.out.println("Account " + getAccountId() + ": Investment of " + investment.getAmountInvested() + " in " + investment.getInvestmentType() + " made.");
                notifyObservers("NEW_INVESTMENT", investment);
            } else {
                System.err.println("Account " + getAccountId() + ": Insufficient balance to make investment of " + investment.getAmountInvested());
            }
        } else {
            System.err.println("Invalid investment details provided.");
        }
    }

    public void accrueInterest() {
        if (this.balance > 0 && this.configuredInterestRate > 0) {
            double interestEarned = this.balance * this.configuredInterestRate;
            this.deposit(interestEarned);
            System.out.println("Account " + getAccountId() + ": Interest accrued: " + interestEarned);
            notifyObservers("INTEREST_ACCRUED", interestEarned);
        }
    }

    public List<Investment> getCurrentInvestments() {
        return new ArrayList<>(currentInvestments);
    }

    public void setCurrentInvestments(List<Investment> investments) {
        this.currentInvestments = new ArrayList<>(investments);
    }

    public double getConfiguredInterestRate() {
        return configuredInterestRate;
    }

    public void setConfiguredInterestRate(double configuredInterestRate) {
        this.configuredInterestRate = configuredInterestRate;
    }

    @Override
    public String toString() {
        return "InvestorAccount{" +
                "accountId=\'" + accountId + "\', " +
                "owner=" + (owner != null ? owner.getUsername() : "null") +
                ", balance=" + balance +
                ", status=" + (currentStatus != null ? currentStatus.getStatusName() : "null") +
                ", accountType=\'" + accountType + "\', " +
                "configuredInterestRate=" + configuredInterestRate +
                ", investmentsCount=" + currentInvestments.size() +
                ", creationDate=" + creationDate +
                '}';
    }
}

