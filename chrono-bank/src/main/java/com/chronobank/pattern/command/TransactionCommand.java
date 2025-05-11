package com.chronobank.pattern.command;
 
public interface TransactionCommand {
    void execute();
    void undo();
    String getDescription();
} 