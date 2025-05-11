package com.chronobank.pattern.decorator;

import com.chronobank.model.transaction.Transaction;

public class TimeTaxDecorator extends TransactionRuleDecorator {
    private double taxRate; 
    private double taxAmount;

    public TimeTaxDecorator(Transaction transaction, double taxRate) {
        super(transaction);
        if (taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1.");
        }
        this.taxRate = taxRate;
        this.taxAmount = 0; 
    }

    @Override
    public void execute() throws Exception {
       
        super.execute();

      
        if (wrappedTransaction.getStatus() != TransactionStatus.COMPLETED) {
            System.out.println("TimeTaxDecorator: Wrapped transaction did not complete. Tax not applied.");
            return;
        }

        
        
        this.taxAmount = wrappedTransaction.getAmount() * this.taxRate;
        
       
        System.out.println("TimeTaxDecorator: Tax of " + taxAmount + " (rate: " + taxRate*100 + "%) calculated on transaction " + wrappedTransaction.getTransactionId());
        
       
        String currentDescription = wrappedTransaction.getDescription();
        wrappedTransaction.setDescription(currentDescription + " | Taxed at " + (taxRate * 100) + "%, Tax Amount: " + taxAmount);
        
       
        // if (wrappedTransaction instanceof com.chronobank.model.transaction.TransferTransaction) {
        //     com.chronobank.model.transaction.TransferTransaction transferTx = (com.chronobank.model.transaction.TransferTransaction) wrappedTransaction;
        //     if (transferTx.getFromAccount() != null) {
        //         try {
        //             transferTx.getFromAccount().withdraw(taxAmount); // Deduct tax from sender
        //             // A central tax account would then receive this amount.
        //             System.out.println("TimeTaxDecorator: Tax of " + taxAmount + " deducted from account " + transferTx.getFromAccount().getAccountId());
        //         } catch (IllegalStateException e) {
        //             System.err.println("TimeTaxDecorator: Failed to deduct tax from account " + transferTx.getFromAccount().getAccountId() + ": " + e.getMessage());
        //             // Decide how to handle tax deduction failure: does the main transaction fail?
        //             // For now, we just log it.
        //         }
        //     }
        // }
    }

    @Override
    public void undo() throws Exception {
        
        System.out.println("TimeTaxDecorator: Attempting to undo tax for transaction " + wrappedTransaction.getTransactionId());
        
        // First, undo the wrapped transaction
        super.undo();

        if (wrappedTransaction.getStatus() == TransactionStatus.REVERTED && this.taxAmount > 0) {
            
            String currentDescription = wrappedTransaction.getDescription();
            // A bit simplistic for string replacement, but for demonstration:
            if (currentDescription.contains("Tax Amount: " + this.taxAmount)) {
                 wrappedTransaction.setDescription(currentDescription.substring(0, currentDescription.indexOf(" | Taxed at")) + " | Tax Reverted: " + this.taxAmount);
            }
            System.out.println("TimeTaxDecorator: Tax of " + this.taxAmount + " (associated with original transaction) marked as conceptually reverted.");
            this.taxAmount = 0; // Reset tax amount for this decorator instance post-undo
        }
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    @Override
    public String getTransactionDetails() {
        return super.getTransactionDetails() + "\n  -> Decorated with TimeTax (Rate: " + (taxRate * 100) + "%, Calculated Tax: " + taxAmount + ")";
    }
}

