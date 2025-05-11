package com.chronobank.service;

import com.chronobank.model.account.AccountPreferences;
import com.chronobank.model.account.BasicTimeAccount;
import com.chronobank.model.account.InvestorAccount;
import com.chronobank.model.account.LoanAccount;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.user.User;
import com.chronobank.pattern.strategy.LoanRepaymentStrategy;

public class AccountFactory {

    public enum AccountType {
        BASIC, INVESTOR, LOAN
    }

    /**
     * @param type This is the type of account you want to create (BASIC, INVESTOR, LOAN).
     * @param owner 
     * @param preferences 
     * @param initialDeposit 
     * @param additionalParams
     * @return 
     * @throws IllegalArgumentException 
     */
    public TimeAccount createTimeAccount(AccountType type, User owner, AccountPreferences preferences,
                                         double initialDeposit, Object... additionalParams) {
        if (owner == null) {
            throw new IllegalArgumentException("Account owner cannot be null.");
        }
        
        AccountPreferences actualPreferences = (preferences != null) ? preferences : new AccountPreferences.Builder().build();

        switch (type) {
            case BASIC:
                return new BasicTimeAccount(owner, actualPreferences, initialDeposit);
            case INVESTOR:
                if (additionalParams.length < 1 || !(additionalParams[0] instanceof Double)) {
                    throw new IllegalArgumentException("InvestorAccount requires a configuredInterestRate (Double) as an additional parameter.");
                }
                double investorInterestRate = (Double) additionalParams[0];
                return new InvestorAccount(owner, actualPreferences, initialDeposit, investorInterestRate);
            case LOAN:
                if (additionalParams.length < 3 || 
                    !(additionalParams[0] instanceof Double) ||
                    !(additionalParams[1] instanceof Double) || 
                    !(additionalParams[2] instanceof Integer)) {
                    throw new IllegalArgumentException("LoanAccount requires loanAmount (Double), interestRate (Double), and termInMonths (Integer) as additional parameters.");
                }
                double loanAmount = (Double) additionalParams[0];
                double loanInterestRate = (Double) additionalParams[1];
                int termInMonths = (Integer) additionalParams[2];
                LoanRepaymentStrategy strategy = null;
                if (additionalParams.length > 3 && additionalParams[3] instanceof LoanRepaymentStrategy) {
                    strategy = (LoanRepaymentStrategy) additionalParams[3];
                }
               
                return new LoanAccount(owner, actualPreferences, loanAmount, loanInterestRate, termInMonths, strategy);
            default:
                throw new IllegalArgumentException("Unknown account type: " + type);
        }
    }
}

