package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;
import com.chronobank.model.account.TimeAccount;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public class OperationsPanel extends JPanel {
    private MainDashboardPanel mainDashboardPanel;
    private BankingFacade bankingFacade;
    private User loggedInUser;

    // Fields for Transfer Panel (example, can be encapsulated further)
    private JComboBox<AccountSelectionItem> sourceAccountComboBoxTransfer;
    private JTextField externalTargetAccountIdFieldTransfer;
    private JTextField amountFieldTransfer;
    private JTextField descriptionFieldTransfer;

    public OperationsPanel(MainDashboardPanel mainDashboardPanel, BankingFacade bankingFacade, User loggedInUser) {
        this.mainDashboardPanel = mainDashboardPanel;
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JTabbedPane operationsTabs = new JTabbedPane();

        operationsTabs.addTab("Deposit", createDepositPanel());
        operationsTabs.addTab("Withdraw", createWithdrawPanel());
        operationsTabs.addTab("Transfer", createTransferPanel());
        operationsTabs.addTab("Loan Operations", new LoanOperationsGuiPanel(this, bankingFacade, loggedInUser));
        operationsTabs.addTab("Investment Operations", new InvestmentOperationsGuiPanel(this, bankingFacade, loggedInUser));

        add(operationsTabs, BorderLayout.CENTER);
    }

    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridy = 0;

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Account to Deposit Into:"), gbc);
        JComboBox<AccountSelectionItem> depositAccountCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(depositAccountCombo, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Amount:"), gbc);
        JTextField depositAmountField = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(depositAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Description (Optional):"), gbc);
        JTextField depositDescriptionField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(depositDescriptionField, gbc);

        JButton depositButton = new JButton("Perform Deposit");
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; panel.add(depositButton, gbc);

        populateAccountComboBox(depositAccountCombo);
        depositButton.addActionListener(e -> performDeposit(depositAccountCombo, depositAmountField, depositDescriptionField));
        return panel;
    }

    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridy = 0;

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Account to Withdraw From:"), gbc);
        JComboBox<AccountSelectionItem> withdrawAccountCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(withdrawAccountCombo, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Amount:"), gbc);
        JTextField withdrawAmountField = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(withdrawAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Description (Optional):"), gbc);
        JTextField withdrawDescriptionField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(withdrawDescriptionField, gbc);

        JButton withdrawButton = new JButton("Perform Withdrawal");
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; panel.add(withdrawButton, gbc);

        populateAccountComboBox(withdrawAccountCombo);
        withdrawButton.addActionListener(e -> performWithdrawal(withdrawAccountCombo, withdrawAmountField, withdrawDescriptionField));
        return panel;
    }

    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridy = 0;

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Source Account:"), gbc);
        sourceAccountComboBoxTransfer = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(sourceAccountComboBoxTransfer, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Target Account ID:"), gbc);
        externalTargetAccountIdFieldTransfer = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(externalTargetAccountIdFieldTransfer, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Amount:"), gbc);
        amountFieldTransfer = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(amountFieldTransfer, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; panel.add(new JLabel("Description (Optional):"), gbc);
        descriptionFieldTransfer = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = gridy++; panel.add(descriptionFieldTransfer, gbc);

        JButton transferButton = new JButton("Perform Transfer");
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; panel.add(transferButton, gbc);

        populateAccountComboBox(sourceAccountComboBoxTransfer);
        transferButton.addActionListener(this::performTransfer);
        return panel;
    }

    private void populateAccountComboBox(JComboBox<AccountSelectionItem> comboBox) {
        new SwingWorker<List<TimeAccount>, Void>() {
            @Override
            protected List<TimeAccount> doInBackground() throws Exception {
                return bankingFacade.findAccountsByUserId(loggedInUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<TimeAccount> accounts = get();
                    comboBox.removeAllItems();
                    if (accounts != null) {
                        for (TimeAccount acc : accounts) {
                            comboBox.addItem(new AccountSelectionItem(acc));
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OperationsPanel.this, "Error loading accounts for dropdown: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performDeposit(JComboBox<AccountSelectionItem> accountCombo, JTextField amountField, JTextField descriptionField) {
        AccountSelectionItem selectedItem = (AccountSelectionItem) accountCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an account.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TimeAccount selectedAccount = selectedItem.getAccount();
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) throw new NumberFormatException("Amount must be positive");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid deposit amount: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String description = descriptionField.getText();

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankingFacade.performDeposit(selectedAccount, amount, description);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(OperationsPanel.this, "Deposit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        amountField.setText("");
                        descriptionField.setText("");
                        mainDashboardPanel.refreshData();
                    } else {
                        JOptionPane.showMessageDialog(OperationsPanel.this, "Deposit failed. See console for details.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OperationsPanel.this, "Error during deposit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performWithdrawal(JComboBox<AccountSelectionItem> accountCombo, JTextField amountField, JTextField descriptionField) {
        AccountSelectionItem selectedItem = (AccountSelectionItem) accountCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an account.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TimeAccount selectedAccount = selectedItem.getAccount();
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
             if (amount <= 0) throw new NumberFormatException("Amount must be positive");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid withdrawal amount: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String description = descriptionField.getText();

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankingFacade.performWithdrawal(selectedAccount, amount, description);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(OperationsPanel.this, "Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        amountField.setText("");
                        descriptionField.setText("");
                        mainDashboardPanel.refreshData();
                    } else {
                        JOptionPane.showMessageDialog(OperationsPanel.this, "Withdrawal failed. Insufficient funds or other error.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OperationsPanel.this, "Error during withdrawal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performTransfer(ActionEvent e) {
        AccountSelectionItem sourceItem = (AccountSelectionItem) sourceAccountComboBoxTransfer.getSelectedItem();
        if (sourceItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a source account.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TimeAccount sourceAccount = sourceItem.getAccount();
        String targetAccountId = externalTargetAccountIdFieldTransfer.getText();
        if (targetAccountId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a target account ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountFieldTransfer.getText());
            if (amount <= 0) throw new NumberFormatException("Amount must be positive");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid transfer amount: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String description = descriptionFieldTransfer.getText();

        new SwingWorker<Boolean, String>() { // Changed to return String for error messages
            @Override
            protected Boolean doInBackground() throws Exception {
                Optional<TimeAccount> targetAccountOpt = bankingFacade.findAccountById(targetAccountId);
                if (!targetAccountOpt.isPresent()) {
                    publish("Target account not found.");
                    return false;
                }
                if (sourceAccount.getAccountId().equals(targetAccountOpt.get().getAccountId())){
                     publish("Source and target accounts cannot be the same.");
                    return false;
                }
                return bankingFacade.performTransfer(sourceAccount, targetAccountOpt.get(), amount, description);
            }

            @Override
            protected void process(List<String> chunks) {
                // Display error messages published from doInBackground
                for (String message : chunks) {
                    JOptionPane.showMessageDialog(OperationsPanel.this, message, "Transfer Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            protected void done() {
                try {
                    // Check if an error message was already shown by process()
                    // This logic is a bit tricky because `get()` might throw an exception if `doInBackground` threw one,
                    // or it might return false if `doInBackground` returned false.
                    // The `publish` mechanism is for specific, known error conditions from `doInBackground`.
                    boolean operationSucceeded = get(); // This might throw if doInBackground threw an unhandled exception.

                    if (operationSucceeded) {
                        JOptionPane.showMessageDialog(OperationsPanel.this, "Transfer successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        externalTargetAccountIdFieldTransfer.setText("");
                        amountFieldTransfer.setText("");
                        descriptionFieldTransfer.setText("");
                        mainDashboardPanel.refreshData();
                    } else {
                        // If `get()` returned false, and no message was published, show a generic failure.
                        // This path is taken if `doInBackground` returned `false` without `publish`ing.
                        // We need to ensure we don't show a double error if `publish` was used.
                        // A simple way is to check if the input fields are still populated, implying no specific error was handled by `process`.
                        if (!externalTargetAccountIdFieldTransfer.getText().isEmpty()) { 
                             // This condition is not robust enough to prevent double messages.
                             // A better way is to have doInBackground return a more complex Result object.
                             // For now, we risk a generic message if specific ones weren't published.
                             // JOptionPane.showMessageDialog(OperationsPanel.this, "Transfer failed. Please check details.", "Transfer Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    // This catches exceptions from `get()` or other issues in `done()`.
                    JOptionPane.showMessageDialog(OperationsPanel.this, "Error during transfer: " + ex.getMessage(), "Transfer Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Getter for MainDashboardPanel to allow child panels to call refreshData
    public MainDashboardPanel getMainDashboardPanel() {
        return mainDashboardPanel;
    }

    // Helper class for JComboBox items to store TimeAccount object
    // Made static and public to be accessible by LoanOperationsGuiPanel and InvestmentOperationsGuiPanel
    public static class AccountSelectionItem {
        private TimeAccount account;

        public AccountSelectionItem(TimeAccount account) {
            this.account = account;
        }

        public TimeAccount getAccount() {
            return account;
        }

        @Override
        public String toString() {
            return account.getAccountId() + " (" + account.getClass().getSimpleName() + ") - Bal: " + String.format("%.2f", account.getBalance());
        }
    }
}

