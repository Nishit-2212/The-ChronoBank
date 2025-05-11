package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.model.account.LoanAccount;
import com.chronobank.model.account.InvestorAccount;
import com.chronobank.model.common.Investment;
import com.chronobank.model.transaction.TransactionRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class AccountDetailsPanel extends JPanel {
    private ChronoBankGuiApp mainApp; // Or the parent frame/dialog
    private BankingFacade bankingFacade;
    private TimeAccount account;
    private Runnable refreshCallback; // To refresh parent view if needed

    private JTable transactionsTable;
    private DefaultTableModel transactionsTableModel;

    public AccountDetailsPanel(Frame owner, BankingFacade bankingFacade, TimeAccount account, Runnable refreshCallback) {
        // Assuming owner is the main ChronoBankGuiApp frame for dialogs
        this.mainApp = (owner instanceof ChronoBankGuiApp) ? (ChronoBankGuiApp) owner : null;
        this.bankingFacade = bankingFacade;
        this.account = account;
        this.refreshCallback = refreshCallback;
        initComponents();
        loadTransactionData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(750, 500)); // Set a preferred size for the dialog

        // Panel for Account Information
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Account Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int gridy = 0;
        addInfoRow(infoPanel, gbc, gridy++, "Account ID:", account.getAccountId());
        addInfoRow(infoPanel, gbc, gridy++, "Account Type:", account.getClass().getSimpleName());
        addInfoRow(infoPanel, gbc, gridy++, "Owner:", account.getOwner().getUsername());
        addInfoRow(infoPanel, gbc, gridy++, "Balance:", String.format("%.2f", account.getBalance()));
        addInfoRow(infoPanel, gbc, gridy++, "Status:", account.getAccountStatus().getStatusName());
        addInfoRow(infoPanel, gbc, gridy++, "Created At:", account.getCreationDate() != null ? account.getCreationDate().toString() : "N/A");

        // Specific info for account types
        if (account instanceof LoanAccount) {
            LoanAccount la = (LoanAccount) account;
            addInfoRow(infoPanel, gbc, gridy++, "Loan Amount:", String.format("%.2f", la.getLoanAmount()));
            addInfoRow(infoPanel, gbc, gridy++, "Remaining Principal:", String.format("%.2f", la.getRemainingLoanPrincipal()));
            addInfoRow(infoPanel, gbc, gridy++, "Interest Rate:", String.format("%.2f%%", la.getInterestRate() * 100));
            addInfoRow(infoPanel, gbc, gridy++, "Term (Months):", String.valueOf(la.getTermInMonths()));
        } else if (account instanceof InvestorAccount) {
            InvestorAccount ia = (InvestorAccount) account;
            addInfoRow(infoPanel, gbc, gridy++, "Configured Interest Rate:", String.format("%.2f%%", ia.getConfiguredInterestRate() * 100));
            // Display investments (simplified for now)
            if (ia.getCurrentInvestments() != null && !ia.getCurrentInvestments().isEmpty()) {
                JTextArea investmentsArea = new JTextArea(3, 30);
                investmentsArea.setEditable(false);
                for (Investment inv : ia.getCurrentInvestments()) {
                    investmentsArea.append(String.format("%s - %.2f @ %s\n", inv.getInvestmentType(), inv.getAmountInvested(), inv.getStartDate()));
                }
                gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 1; add(new JLabel("Investments:"), gbc);
                gbc.gridx = 1; gbc.gridy = gridy; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; add(new JScrollPane(investmentsArea), gbc);
                gridy++;
            }
        }
        
        gbc.gridy = gridy; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        infoPanel.add(new JPanel(), gbc); // Filler to push content up

        add(infoPanel, BorderLayout.NORTH);

        // Table for Transactions
        JPanel transactionsPanel = new JPanel(new BorderLayout());
        transactionsPanel.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        String[] columnNames = {"ID", "Type", "Amount", "Timestamp", "Status", "Description", "From Acc", "To Acc"};
        transactionsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(transactionsTableModel);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Important for many columns
        // Set column widths (approximate)
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // ID
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Type
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Amount
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Timestamp
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        transactionsTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Description
        transactionsTable.getColumnModel().getColumn(6).setPreferredWidth(120); // From Acc
        transactionsTable.getColumnModel().getColumn(7).setPreferredWidth(120); // To Acc

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        transactionsPanel.add(scrollPane, BorderLayout.CENTER);
        add(transactionsPanel, BorderLayout.CENTER);

        // Potentially add buttons for actions like "Deposit", "Withdraw", "Transfer" here
        // or these are handled from the MainDashboardPanel contextually.
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int gridy, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(value != null ? value : "N/A"), gbc);
    }

    public void loadTransactionData() {
        transactionsTableModel.setRowCount(0); // Clear existing data

        new SwingWorker<List<TransactionRecord>, Void>() {
            @Override
            protected List<TransactionRecord> doInBackground() throws Exception {
                return bankingFacade.getTransactionHistory(account.getAccountId());
            }

            @Override
            protected void done() {
                try {
                    List<TransactionRecord> records = get();
                    if (records != null) {
                        for (TransactionRecord record : records) {
                            Vector<Object> row = new Vector<>();
                            row.add(record.getTransactionId());
                            row.add(record.getTransactionType());
                            row.add(String.format("%.2f", record.getAmount()));
                            row.add(record.getLoggedAt() != null ? record.getLoggedAt().toString() : "N/A");
                            row.add(record.getStatus());
                            row.add(record.getDescription());
                            row.add(record.getFromAccountId() != null ? record.getFromAccountId() : "N/A");
                            row.add(record.getToAccountId() != null ? record.getToAccountId() : "N/A");
                            transactionsTableModel.addRow(row);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AccountDetailsPanel.this,
                            "Error loading transaction history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}

