package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.account.LoanAccount;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class LoanOperationsGuiPanel extends JPanel {
    private OperationsPanel parentOperationsPanel; // To access shared components or main app frame
    private BankingFacade bankingFacade;
    private User loggedInUser;

    private JComboBox<OperationsPanel.AccountSelectionItem> loanAccountComboBox;
    private JComboBox<OperationsPanel.AccountSelectionItem> targetAccountForDisbursementComboBox;
    private JComboBox<OperationsPanel.AccountSelectionItem> sourceAccountForRepaymentComboBox;
    private JTextField disbursementAmountField;
    private JTextField repaymentAmountField;

    public LoanOperationsGuiPanel(OperationsPanel parentOperationsPanel, BankingFacade bankingFacade, User loggedInUser) {
        this.parentOperationsPanel = parentOperationsPanel;
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        initComponents();
        loadUserLoanAccounts();
        loadUserAllAccountsForComboBoxes();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridy = 0;

        // Loan Account Selection
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Select Loan Account:"), gbc);
        loanAccountComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = gridy++; add(loanAccountComboBox, gbc);

        // --- Disbursement Section ---
        JPanel disbursementPanel = new JPanel(new GridBagLayout());
        disbursementPanel.setBorder(BorderFactory.createTitledBorder("Disburse Loan"));
        GridBagConstraints gbcDisburse = new GridBagConstraints();
        gbcDisburse.insets = new Insets(5,5,5,5);
        gbcDisburse.fill = GridBagConstraints.HORIZONTAL;
        int dGridy = 0;

        gbcDisburse.gridx = 0; gbcDisburse.gridy = dGridy; disbursementPanel.add(new JLabel("Target Account for Disbursement:"), gbcDisburse);
        targetAccountForDisbursementComboBox = new JComboBox<>();
        gbcDisburse.gridx = 1; gbcDisburse.gridy = dGridy++; disbursementPanel.add(targetAccountForDisbursementComboBox, gbcDisburse);

        gbcDisburse.gridx = 0; gbcDisburse.gridy = dGridy; disbursementPanel.add(new JLabel("Disbursement Amount:"), gbcDisburse);
        disbursementAmountField = new JTextField(10);
        gbcDisburse.gridx = 1; gbcDisburse.gridy = dGridy++; disbursementPanel.add(disbursementAmountField, gbcDisburse);

        JButton disburseButton = new JButton("Disburse Loan Funds");
        gbcDisburse.gridx = 0; gbcDisburse.gridy = dGridy; gbcDisburse.gridwidth = 2; gbcDisburse.anchor = GridBagConstraints.CENTER; disbursementPanel.add(disburseButton, gbcDisburse);
        disburseButton.addActionListener(this::performDisbursement);

        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.weightx = 1.0; add(disbursementPanel, gbc);
        gridy++;

        // --- Repayment Section ---
        JPanel repaymentPanel = new JPanel(new GridBagLayout());
        repaymentPanel.setBorder(BorderFactory.createTitledBorder("Make Loan Repayment"));
        GridBagConstraints gbcRepay = new GridBagConstraints();
        gbcRepay.insets = new Insets(5,5,5,5);
        gbcRepay.fill = GridBagConstraints.HORIZONTAL;
        int rGridy = 0;

        gbcRepay.gridx = 0; gbcRepay.gridy = rGridy; repaymentPanel.add(new JLabel("Source Account for Repayment:"), gbcRepay);
        sourceAccountForRepaymentComboBox = new JComboBox<>();
        gbcRepay.gridx = 1; gbcRepay.gridy = rGridy++; repaymentPanel.add(sourceAccountForRepaymentComboBox, gbcRepay);

        gbcRepay.gridx = 0; gbcRepay.gridy = rGridy; repaymentPanel.add(new JLabel("Repayment Amount:"), gbcRepay);
        repaymentAmountField = new JTextField(10);
        gbcRepay.gridx = 1; gbcRepay.gridy = rGridy++; repaymentPanel.add(repaymentAmountField, gbcRepay);

        JButton repayButton = new JButton("Make Repayment");
        gbcRepay.gridx = 0; gbcRepay.gridy = rGridy; gbcRepay.gridwidth = 2; gbcRepay.anchor = GridBagConstraints.CENTER; repaymentPanel.add(repayButton, gbcRepay);
        repayButton.addActionListener(this::performRepayment);

        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.weightx = 1.0; add(repaymentPanel, gbc);
        gridy++;
        
        // Add a filler panel to push content to the top
        gbc.gridy = gridy; gbc.weighty = 1.0; add(new JPanel(), gbc);
    }

    private void loadUserLoanAccounts() {
        new SwingWorker<List<TimeAccount>, Void>() {
            @Override
            protected List<TimeAccount> doInBackground() throws Exception {
                return bankingFacade.findAccountsByUserId(loggedInUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<TimeAccount> accounts = get();
                    loanAccountComboBox.removeAllItems();
                    if (accounts != null) {
                        for (TimeAccount acc : accounts) {
                            if (acc instanceof LoanAccount) {
                                loanAccountComboBox.addItem(new OperationsPanel.AccountSelectionItem(acc));
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Error loading loan accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    
    private void loadUserAllAccountsForComboBoxes() {
        new SwingWorker<List<TimeAccount>, Void>() {
            @Override
            protected List<TimeAccount> doInBackground() throws Exception {
                return bankingFacade.findAccountsByUserId(loggedInUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<TimeAccount> accounts = get();
                    targetAccountForDisbursementComboBox.removeAllItems();
                    sourceAccountForRepaymentComboBox.removeAllItems();
                    if (accounts != null) {
                        for (TimeAccount acc : accounts) {
                            // Loan accounts typically cannot be targets for disbursement or sources for repayment from themselves
                            if (!(acc instanceof LoanAccount)) {
                                targetAccountForDisbursementComboBox.addItem(new OperationsPanel.AccountSelectionItem(acc));
                            }
                            sourceAccountForRepaymentComboBox.addItem(new OperationsPanel.AccountSelectionItem(acc));
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Error loading accounts for dropdowns: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performDisbursement(java.awt.event.ActionEvent e) {
        OperationsPanel.AccountSelectionItem selectedLoanItem = (OperationsPanel.AccountSelectionItem) loanAccountComboBox.getSelectedItem();
        OperationsPanel.AccountSelectionItem selectedTargetItem = (OperationsPanel.AccountSelectionItem) targetAccountForDisbursementComboBox.getSelectedItem();

        if (selectedLoanItem == null || selectedTargetItem == null) {
            JOptionPane.showMessageDialog(this, "Please select both a loan account and a target account.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LoanAccount loanAccount = (LoanAccount) selectedLoanItem.getAccount();
        TimeAccount targetAccount = selectedTargetItem.getAccount();
        double amount;
        try {
            amount = Double.parseDouble(disbursementAmountField.getText());
            if (amount <= 0) throw new NumberFormatException("Amount must be positive.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid disbursement amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankingFacade.disburseLoan(loanAccount, targetAccount, amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Loan disbursement successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        disbursementAmountField.setText("");
                        parentOperationsPanel.getMainDashboardPanel().refreshData(); // Refresh main dashboard
                        loadUserLoanAccounts(); // Refresh loan account details if balance changed
                        loadUserAllAccountsForComboBoxes(); // Refresh target account balance
                    } else {
                        JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Loan disbursement failed.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Error during loan disbursement: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performRepayment(java.awt.event.ActionEvent e) {
        OperationsPanel.AccountSelectionItem selectedLoanItem = (OperationsPanel.AccountSelectionItem) loanAccountComboBox.getSelectedItem();
        OperationsPanel.AccountSelectionItem selectedSourceItem = (OperationsPanel.AccountSelectionItem) sourceAccountForRepaymentComboBox.getSelectedItem();

        if (selectedLoanItem == null || selectedSourceItem == null) {
            JOptionPane.showMessageDialog(this, "Please select both a loan account and a source account for repayment.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LoanAccount loanAccount = (LoanAccount) selectedLoanItem.getAccount();
        TimeAccount sourceAccount = selectedSourceItem.getAccount();
        double amount;
        try {
            amount = Double.parseDouble(repaymentAmountField.getText());
            if (amount <= 0) throw new NumberFormatException("Amount must be positive.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid repayment amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankingFacade.makeLoanRepayment(loanAccount, sourceAccount, amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Loan repayment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        repaymentAmountField.setText("");
                        parentOperationsPanel.getMainDashboardPanel().refreshData(); // Refresh main dashboard
                        loadUserLoanAccounts(); // Refresh loan account details
                        loadUserAllAccountsForComboBoxes(); // Refresh source account balance
                    } else {
                        JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Loan repayment failed.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoanOperationsGuiPanel.this, "Error during loan repayment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    
    // Call this if the list of accounts in the main app changes
    public void refreshAccountDropdowns() {
        loadUserLoanAccounts();
        loadUserAllAccountsForComboBoxes();
    }
}

