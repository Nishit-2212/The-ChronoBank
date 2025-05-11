package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.account.InvestorAccount;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InvestmentOperationsGuiPanel extends JPanel {
    private OperationsPanel parentOperationsPanel;
    private BankingFacade bankingFacade;
    private User loggedInUser;

    private JComboBox<OperationsPanel.AccountSelectionItem> investorAccountComboBox;
    private JTextField investmentTypeField;
    private JTextField investmentAmountField;

    public InvestmentOperationsGuiPanel(OperationsPanel parentOperationsPanel, BankingFacade bankingFacade, User loggedInUser) {
        this.parentOperationsPanel = parentOperationsPanel;
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        initComponents();
        loadUserInvestorAccounts();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridy = 0;

        // Investor Account Selection
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Select Investor Account:"), gbc);
        investorAccountComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = gridy++; add(investorAccountComboBox, gbc);

        // Investment Type
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Investment Type/Identifier:"), gbc);
        investmentTypeField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = gridy++; add(investmentTypeField, gbc);

        // Investment Amount
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Investment Amount:"), gbc);
        investmentAmountField = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = gridy++; add(investmentAmountField, gbc);

        // Make Investment Button
        JButton makeInvestmentButton = new JButton("Make Investment");
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; add(makeInvestmentButton, gbc);
        makeInvestmentButton.addActionListener(this::performInvestment);
        
        // Add a filler panel to push content to the top
        gbc.gridy = gridy + 1; gbc.weighty = 1.0; add(new JPanel(), gbc);
    }

    private void loadUserInvestorAccounts() {
        new SwingWorker<List<TimeAccount>, Void>() {
            @Override
            protected List<TimeAccount> doInBackground() throws Exception {
                return bankingFacade.findAccountsByUserId(loggedInUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<TimeAccount> accounts = get();
                    investorAccountComboBox.removeAllItems();
                    if (accounts != null) {
                        for (TimeAccount acc : accounts) {
                            if (acc instanceof InvestorAccount) {
                                investorAccountComboBox.addItem(new OperationsPanel.AccountSelectionItem(acc));
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InvestmentOperationsGuiPanel.this, "Error loading investor accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void performInvestment(ActionEvent e) {
        OperationsPanel.AccountSelectionItem selectedItem = (OperationsPanel.AccountSelectionItem) investorAccountComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an investor account.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        InvestorAccount investorAccount = (InvestorAccount) selectedItem.getAccount();
        String investmentType = investmentTypeField.getText();
        if (investmentType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Investment type cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(investmentAmountField.getText());
            if (amount <= 0) throw new NumberFormatException("Amount must be positive");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid investment amount: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bankingFacade.makeInvestment(investorAccount, investmentType, amount);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(InvestmentOperationsGuiPanel.this, "Investment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        investmentTypeField.setText("");
                        investmentAmountField.setText("");
                        // Refresh data in the main dashboard, particularly the account overview
                        parentOperationsPanel.getMainDashboardPanel().refreshData();
                        loadUserInvestorAccounts(); // Refresh this panel's dropdown if needed, though balance change is main thing
                    } else {
                        JOptionPane.showMessageDialog(InvestmentOperationsGuiPanel.this, "Investment failed. Insufficient funds or other error.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(InvestmentOperationsGuiPanel.this, "Error during investment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Call this if the list of accounts in the main app changes
    public void refreshAccountDropdowns() {
        loadUserInvestorAccounts();
    }
}

