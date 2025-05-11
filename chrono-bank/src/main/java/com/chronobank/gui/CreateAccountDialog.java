package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;
import com.chronobank.model.account.AccountPreferences;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.service.AccountFactory; // Correct import

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class CreateAccountDialog extends JDialog {
    private BankingFacade bankingFacade;
    private User loggedInUser;
    private Runnable refreshCallback; // To refresh the accounts list after creation

    private JComboBox<AccountFactory.AccountType> accountTypeComboBox;
    private JTextField initialDepositField;
    private JTextField investorInterestRateField;
    private JTextField loanAmountField;
    private JTextField loanInterestRateField;
    private JTextField loanTermField;

    private JLabel investorInterestRateLabel;
    private JLabel loanAmountLabel;
    private JLabel loanInterestRateLabel;
    private JLabel loanTermLabel;

    public CreateAccountDialog(Frame owner, BankingFacade bankingFacade, User loggedInUser, Runnable refreshCallback) {
        super(owner, "Create New Account", true);
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        this.refreshCallback = refreshCallback;
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int gridy = 0;

        // Account Type
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(new JLabel("Account Type:"), gbc);
        accountTypeComboBox = new JComboBox<>(AccountFactory.AccountType.values());
        gbc.gridx = 1;
        add(accountTypeComboBox, gbc);
        gridy++;

        // Initial Deposit
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(new JLabel("Initial Deposit:"), gbc);
        initialDepositField = new JTextField("0.00", 10);
        gbc.gridx = 1;
        add(initialDepositField, gbc);
        gridy++;

        // Investor Fields (initially hidden)
        investorInterestRateLabel = new JLabel("Investor Interest Rate (%):");
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(investorInterestRateLabel, gbc);
        investorInterestRateField = new JTextField(5);
        gbc.gridx = 1;
        add(investorInterestRateField, gbc);
        gridy++;

        // Loan Fields (initially hidden)
        loanAmountLabel = new JLabel("Loan Amount:");
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(loanAmountLabel, gbc);
        loanAmountField = new JTextField(10);
        gbc.gridx = 1;
        add(loanAmountField, gbc);
        gridy++;

        loanInterestRateLabel = new JLabel("Loan Interest Rate (%):");
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(loanInterestRateLabel, gbc);
        loanInterestRateField = new JTextField(5);
        gbc.gridx = 1;
        add(loanInterestRateField, gbc);
        gridy++;

        loanTermLabel = new JLabel("Loan Term (Months):");
        gbc.gridx = 0;
        gbc.gridy = gridy;
        add(loanTermLabel, gbc);
        loanTermField = new JTextField(5);
        gbc.gridx = 1;
        add(loanTermField, gbc);
        gridy++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Action Listeners
        accountTypeComboBox.addActionListener(this::onAccountTypeChange);
        createButton.addActionListener(this::performCreateAccount);
        cancelButton.addActionListener(e -> dispose());

        // Initial visibility based on default selection
        onAccountTypeChange(null); 
    }

    private void onAccountTypeChange(ActionEvent e) {
        AccountFactory.AccountType selectedType = (AccountFactory.AccountType) accountTypeComboBox.getSelectedItem();
        boolean isInvestor = selectedType == AccountFactory.AccountType.INVESTOR;
        boolean isLoan = selectedType == AccountFactory.AccountType.LOAN;

        investorInterestRateLabel.setVisible(isInvestor);
        investorInterestRateField.setVisible(isInvestor);

        loanAmountLabel.setVisible(isLoan);
        loanAmountField.setVisible(isLoan);
        loanInterestRateLabel.setVisible(isLoan);
        loanInterestRateField.setVisible(isLoan);
        loanTermLabel.setVisible(isLoan);
        loanTermField.setVisible(isLoan);

        if (isLoan) {
            initialDepositField.setText("0.00"); // Loans typically don't have an initial deposit from user
            initialDepositField.setEnabled(false);
        } else {
            initialDepositField.setEnabled(true);
        }
        pack(); // Adjust dialog size
    }

    private void performCreateAccount(ActionEvent e) {
        AccountFactory.AccountType selectedType = (AccountFactory.AccountType) accountTypeComboBox.getSelectedItem();
        double initialDeposit;
        try {
            initialDeposit = Double.parseDouble(initialDepositField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid initial deposit amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AccountPreferences prefs = new AccountPreferences.Builder().build(); // Default preferences
        Object[] additionalParams = {};

        try {
            if (selectedType == AccountFactory.AccountType.INVESTOR) {
                double rate = Double.parseDouble(investorInterestRateField.getText()) / 100.0;
                additionalParams = new Object[]{rate};
            } else if (selectedType == AccountFactory.AccountType.LOAN) {
                double loanAmount = Double.parseDouble(loanAmountField.getText());
                double loanRate = Double.parseDouble(loanInterestRateField.getText()) / 100.0;
                int term = Integer.parseInt(loanTermField.getText());
                // For simplicity, not including LoanRepaymentStrategy selection in this dialog yet
                additionalParams = new Object[]{loanAmount, loanRate, term, null};
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid numeric input for account specific fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] finalAdditionalParams = additionalParams; // For use in SwingWorker
        new SwingWorker<TimeAccount, Void>() {
            @Override
            protected TimeAccount doInBackground() throws Exception {
                return bankingFacade.createAccount(loggedInUser, selectedType, prefs, initialDeposit, finalAdditionalParams);
            }

            @Override
            protected void done() {
                try {
                    TimeAccount newAccount = get();
                    if (newAccount != null) {
                        JOptionPane.showMessageDialog(CreateAccountDialog.this,
                                "Account created successfully: " + newAccount.getAccountId(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        if (refreshCallback != null) {
                            refreshCallback.run();
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(CreateAccountDialog.this,
                                "Failed to create account. Check console for details from facade.",
                                "Creation Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CreateAccountDialog.this,
                            "Error during account creation: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}

