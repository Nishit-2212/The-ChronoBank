package com.chronobank.pattern.command;

import java.util.Stack;

public class CommandHistory {
    private final Stack<TransactionCommand> history = new Stack<>();
    private final Stack<TransactionCommand> redoStack = new Stack<>();

    public void executeCommand(TransactionCommand command) {
        command.execute();
        history.push(command);
        redoStack.clear(); 
    }

    public void undo() {
        if (!history.isEmpty()) {
            TransactionCommand command = history.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            TransactionCommand command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clear() {
        history.clear();
        redoStack.clear();
    }
} 