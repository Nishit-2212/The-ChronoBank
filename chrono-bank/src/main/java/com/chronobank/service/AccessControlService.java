package com.chronobank.service;

import com.chronobank.model.user.User;
import com.chronobank.model.transaction.Transaction;

public class AccessControlService {

    private static final int MIN_REPUTATION_FOR_LARGE_LOAN = 50;
    private static final int MAX_RISK_SCORE_FOR_ANY_TRANSACTION = 80;
    private static final double LARGE_LOAN_AMOUNT_THRESHOLD = 50000.0; 

    public AccessControlService() {
    }

    /**
     * @param user The user attempting the transaction.
     * @param transaction The transaction being attempted (can be used to get type, amount).
     * @return true if the user is allowed, false otherwise.
     */
    public boolean canPerformTransaction(User user, Transaction transaction) {
        if (user == null || transaction == null) {
            System.err.println("AccessControlService: User or Transaction cannot be null.");
            return false;
        }

        if (user.getRiskScore() > MAX_RISK_SCORE_FOR_ANY_TRANSACTION) {
            System.out.println("AccessControlService: User " + user.getUsername() + " denied. Risk score " + user.getRiskScore() + " exceeds maximum allowed " + MAX_RISK_SCORE_FOR_ANY_TRANSACTION + ".");
            return false;
        }

        if (transaction instanceof com.chronobank.model.transaction.LoanTransaction) {
            com.chronobank.model.transaction.LoanTransaction loanTx = (com.chronobank.model.transaction.LoanTransaction) transaction;
            if (loanTx.getLoanTransactionType() == com.chronobank.model.transaction.LoanTransaction.LoanTransactionType.DISBURSEMENT) {
                if (loanTx.getAmount() > LARGE_LOAN_AMOUNT_THRESHOLD) {
                    if (user.getReputationScore() < MIN_REPUTATION_FOR_LARGE_LOAN) {
                        System.out.println("AccessControlService: User " + user.getUsername() + " denied large loan of " + loanTx.getAmount() + 
                                           ". Reputation score " + user.getReputationScore() + " is below minimum " + MIN_REPUTATION_FOR_LARGE_LOAN + ".");
                        return false;
                    }
                }
            }
        }
        


        System.out.println("AccessControlService: User " + user.getUsername() + " is allowed to perform transaction " + transaction.getTransactionId() + ".");
        return true;
    }

    /**
     * @param user The user attempting to open an account.
     * @param accountType The type of account (e.g., "INVESTOR", "LOAN").
     * @return true if allowed, false otherwise.
     */
    public boolean canOpenAccountType(User user, String accountType) {
        if (user == null || accountType == null) {
            return false;
        }
        if (("INVESTOR".equalsIgnoreCase(accountType) || "LOAN".equalsIgnoreCase(accountType)) && user.getReputationScore() < 10) {
            System.out.println("AccessControlService: User " + user.getUsername() + " denied opening " + accountType + 
                               " account. Reputation score " + user.getReputationScore() + " is too low (min 10 required).");
            return false;
        }
        return true;
    }
}

