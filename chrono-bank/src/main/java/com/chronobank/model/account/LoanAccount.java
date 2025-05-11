package com.chronobank.model.account;

import com.chronobank.model.user.User;
import com.chronobank.pattern.strategy.LoanRepaymentStrategy;
import com.chronobank.pattern.strategy.FixedTimeRepaymentStrategy;
import java.sql.Timestamp;

public class LoanAccount extends TimeAccount {
    private double loanAmount; 
    private double interestRate; 
    private LoanRepaymentStrategy repaymentStrategy;
    private Timestamp dueDate;
    private int termInMonths; 
    private double remainingLoanPrincipal; 

   
    public LoanAccount(User owner, AccountPreferences preferences, double initialLoanAmount, double interestRate, int termInMonths, LoanRepaymentStrategy repaymentStrategy) {
        super(owner, preferences, 0, "LOAN"); 
        this.loanAmount = initialLoanAmount;
        this.remainingLoanPrincipal = initialLoanAmount;
        this.interestRate = interestRate;
        this.termInMonths = termInMonths;
        this.repaymentStrategy = (repaymentStrategy != null) ? repaymentStrategy : new FixedTimeRepaymentStrategy();
    }

    
    public LoanAccount(String accountId, User owner, AccountPreferences preferences, double balanceFromDB, String accountType, Timestamp creationDate, 
                       double loanAmount, double interestRate, int termInMonths, LoanRepaymentStrategy repaymentStrategy, Timestamp dueDate, double remainingLoanPrincipal) {
        super(accountId, owner, preferences, balanceFromDB, accountType, creationDate);
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.termInMonths = termInMonths;
        this.repaymentStrategy = (repaymentStrategy != null) ? repaymentStrategy : new FixedTimeRepaymentStrategy();
        this.dueDate = dueDate;
        this.remainingLoanPrincipal = remainingLoanPrincipal; 
    }


    public void disburseLoan() {
        System.out.println("Account " + getAccountId() + ": Loan of " + loanAmount + " disbursed (conceptually). Remaining principal: " + remainingLoanPrincipal);
        notifyObservers("LOAN_DISBURSED", loanAmount);
    }

    public void makeRepayment(double amount) {
        if (amount <= 0) {
            System.err.println("Repayment amount must be positive.");
            return;
        }
        
        double periodicInterestRate = this.interestRate / 12; 
        double interestForPeriod = this.remainingLoanPrincipal * periodicInterestRate;
        double principalPaid = amount - interestForPeriod;

        if (principalPaid < 0) { 
            System.err.println("Account " + getAccountId() + ": Repayment of " + amount + " does not cover interest of " + interestForPeriod);
            this.remainingLoanPrincipal -= amount; 
        } else {
            this.remainingLoanPrincipal -= principalPaid;
        }

        if (this.remainingLoanPrincipal < 0) {
            this.remainingLoanPrincipal = 0;
        }

        super.deposit(amount); 

        System.out.println("Account " + getAccountId() + ": Repayment of " + amount + " made. Interest for period: " + interestForPeriod + ". Principal paid: " + principalPaid + ". Remaining loan principal: " + remainingLoanPrincipal);
        notifyObservers("LOAN_REPAYMENT_MADE", amount);

        if (this.remainingLoanPrincipal == 0) {
            System.out.println("Account " + getAccountId() + ": Loan fully repaid.");
            notifyObservers("LOAN_FULLY_REPAID", this.accountId);
        }
    }

    public double calculateCurrentRepaymentAmount() {
        if (remainingLoanPrincipal <= 0) return 0;
        return repaymentStrategy.calculateRepayment(remainingLoanPrincipal, interestRate / 12, termInMonths);
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public LoanRepaymentStrategy getRepaymentStrategy() {
        return repaymentStrategy;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public int getTermInMonths() {
        return termInMonths;
    }

    public double getRemainingLoanPrincipal() {
        return remainingLoanPrincipal;
    }
    
    public void setRemainingLoanPrincipal(double remainingLoanPrincipal) {
        this.remainingLoanPrincipal = remainingLoanPrincipal;
    }

    public void setRepaymentStrategy(LoanRepaymentStrategy repaymentStrategy) {
        this.repaymentStrategy = repaymentStrategy;
        System.out.println("Account " + getAccountId() + ": Loan repayment strategy changed to " + repaymentStrategy.getStrategyName());
        notifyObservers("LOAN_STRATEGY_CHANGED", repaymentStrategy.getStrategyName());
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "LoanAccount{" +
                "accountId=\'" + accountId + "\', " +
                "owner=" + (owner != null ? owner.getUsername() : "null") +
                ", balance=" + balance + 
                ", status=" + (currentStatus != null ? currentStatus.getStatusName() : "null") +
                ", accountType=\'" + accountType + "\', " +
                "loanAmount=" + loanAmount +
                ", remainingLoanPrincipal=" + remainingLoanPrincipal +
                ", interestRate=" + interestRate +
                ", strategy=\'" + (repaymentStrategy != null ? repaymentStrategy.getStrategyName() : "null") + "\', " +
                "creationDate=" + creationDate +
                "}";
    }
}

