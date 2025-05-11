package com.chronobank.pattern.command;

import com.chronobank.model.account.TimeAccount;
import com.chronobank.service.BankingFacade;

public class TransferCommand implements TransactionCommand {
    private final BankingFacade facade;
    private final TimeAccount fromAccount;
    private final TimeAccount toAccount;
    private final double amount;
    private final String description;
    private boolean executed = false;

    public TransferCommand(BankingFacade facade, TimeAccount fromAccount, TimeAccount toAccount, double amount, String description) {
        this.facade = facade;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.description = description;
    }

    @Override
    public void execute() {
        if (!executed) {
            executed = facade.performTransfer(fromAccount, toAccount, amount, description);
        }
    }

    @Override
    public void undo() {
        if (executed) {
           
            facade.performTransfer(toAccount, fromAccount, amount, "Undo: " + description);
            executed = false;
        }
    }

    @Override
    public String getDescription() {
        return "Transfer " + amount + " from " + fromAccount.getAccountId() + " to " + toAccount.getAccountId();
    }
} 