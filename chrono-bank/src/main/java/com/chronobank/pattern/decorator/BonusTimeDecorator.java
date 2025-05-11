package com.chronobank.pattern.decorator;

import com.chronobank.model.transaction.Transaction;

public class BonusTimeDecorator extends TransactionRuleDecorator {
    private double bonusPercentage; 
    private double bonusAmount;

    public BonusTimeDecorator(Transaction transaction, double bonusPercentage) {
        super(transaction);
        if (bonusPercentage < 0) {
            throw new IllegalArgumentException("Bonus percentage cannot be negative.");
        }
        this.bonusPercentage = bonusPercentage;
        this.bonusAmount = 0; 
    }

    @Override
    public void execute() throws Exception {
       
        super.execute();

        
        if (wrappedTransaction.getStatus() != TransactionStatus.COMPLETED) {
            System.out.println("BonusTimeDecorator: Wrapped transaction did not complete. Bonus not applied.");
            return;
        }

        
        
        this.bonusAmount = wrappedTransaction.getAmount() * this.bonusPercentage;
        
        System.out.println("BonusTimeDecorator: Bonus of " + bonusAmount + " (rate: " + bonusPercentage*100 + "%) calculated on transaction " + wrappedTransaction.getTransactionId());
        
       
        String currentDescription = wrappedTransaction.getDescription();
        wrappedTransaction.setDescription(currentDescription + " | Bonus Time at " + (bonusPercentage * 100) + "%, Bonus Amount: " + bonusAmount);
        
        
        // if (wrappedTransaction instanceof com.chronobank.model.transaction.TransferTransaction) {
        //     com.chronobank.model.transaction.TransferTransaction transferTx = (com.chronobank.model.transaction.TransferTransaction) wrappedTransaction;
        //     if (transferTx.getToAccount() != null) {
        //         try {
        //             transferTx.getToAccount().deposit(bonusAmount); // Add bonus to receiver
        //             System.out.println("BonusTimeDecorator: Bonus of " + bonusAmount + " credited to account " + transferTx.getToAccount().getAccountId());
        //         } catch (Exception e) {
        //             System.err.println("BonusTimeDecorator: Failed to credit bonus to account " + transferTx.getToAccount().getAccountId() + ": " + e.getMessage());
        //             // Decide how to handle bonus crediting failure.
        //         }
        //     }
        // }
    }

    @Override
    public void undo() throws Exception {
        // If bonus was actually credited, it should be debited during undo.
        System.out.println("BonusTimeDecorator: Attempting to undo bonus for transaction " + wrappedTransaction.getTransactionId());
        
        // First, undo the wrapped transaction
        super.undo();

        if (wrappedTransaction.getStatus() == TransactionStatus.REVERTED && this.bonusAmount > 0) {
          
            // String currentDescription = wrappedTransaction.getDescription();
            // wrappedTransaction.setDescription(currentDescription.replace(" | Bonus Time at " + (bonusPercentage * 100) + "%, Bonus Amount: " + bonusAmount, "") + " | Bonus Reverted: " + bonusAmount);
            // System.out.println("BonusTimeDecorator: Bonus of " + bonusAmount + " conceptually debited for transaction " + wrappedTransaction.getTransactionId());
            // Similar to execute, if bonus was actually moved, it needs to be moved back.
            // if (wrappedTransaction instanceof com.chronobank.model.transaction.TransferTransaction) {
            //     com.chronobank.model.transaction.TransferTransaction transferTx = (com.chronobank.model.transaction.TransferTransaction) wrappedTransaction;
            //     if (transferTx.getToAccount() != null) {
            //         try {
            //             transferTx.getToAccount().withdraw(bonusAmount); // Debit bonus from receiver
            //             System.out.println("BonusTimeDecorator: Bonus of " + bonusAmount + " debited from account " + transferTx.getToAccount().getAccountId());
            //         } catch (IllegalStateException e) {
            //             System.err.println("BonusTimeDecorator: Failed to debit bonus from account " + transferTx.getToAccount().getAccountId() + ": " + e.getMessage());
            //         }
            //     }
            // }
          
            String currentDescription = wrappedTransaction.getDescription();
            if (currentDescription.contains("Bonus Amount: " + this.bonusAmount)) {
                 wrappedTransaction.setDescription(currentDescription.substring(0, currentDescription.indexOf(" | Bonus Time at")) + " | Bonus Reverted: " + this.bonusAmount);
            }
            System.out.println("BonusTimeDecorator: Bonus of " + this.bonusAmount + " (associated with original transaction) marked as conceptually reverted.");
            this.bonusAmount = 0; // Reset bonus amount for this decorator instance post-undo
        }
    }

    public double getBonusAmount() {
        return bonusAmount;
    }

    @Override
    public String getTransactionDetails() {
        return super.getTransactionDetails() + "\n  -> Decorated with BonusTime (Rate: " + (bonusPercentage * 100) + "%, Calculated Bonus: " + bonusAmount + ")";
    }
}

