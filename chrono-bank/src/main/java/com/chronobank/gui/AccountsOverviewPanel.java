package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;
import com.chronobank.model.account.TimeAccount;
import com.chronobank.service.AccountFactory; // Ensure this is the correct import

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional; // Added import for Optional
import java.util.Vector;

public class AccountsOverviewPanel extends JPanel {
    private MainDashboardPanel mainDashboardPanel; // Parent for callbacks or shared data access
    private BankingFacade bankingFacade;
    private User loggedInUser;

    private JTable accountsTable;
    private DefaultTableModel accountsTableModel;
    private JButton createAccountButton;
    private JButton viewAccountDetailsButton;
    private JButton refreshButton;

    public AccountsOverviewPanel(MainDashboardPanel mainDashboardPanel, BankingFacade bankingFacade, User loggedInUser) {
        this.mainDashboardPanel = mainDashboardPanel;
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        initComponents();
        loadAccountData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));

        // Table for accounts
        String[] columnNames = {"Account ID", "Type", "Balance", "Status"};
        accountsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        accountsTable = new JTable(accountsTableModel);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(accountsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createAccountButton = new JButton("Create New Account");
        viewAccountDetailsButton = new JButton("View Details/Transactions");
        refreshButton = new JButton("Refresh List");

        buttonPanel.add(createAccountButton);
        buttonPanel.add(viewAccountDetailsButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        createAccountButton.addActionListener(this::showCreateAccountDialog);
        viewAccountDetailsButton.addActionListener(this::showAccountDetails);
        refreshButton.addActionListener(e -> loadAccountData());
    }

    public void loadAccountData() {
        // Clear existing data
        accountsTableModel.setRowCount(0);

        new SwingWorker<List<TimeAccount>, Void>() {
            @Override
            protected List<TimeAccount> doInBackground() throws Exception {
                return bankingFacade.findAccountsByUserId(loggedInUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<TimeAccount> accounts = get();
                    if (accounts != null) {
                        for (TimeAccount account : accounts) {
                            Vector<Object> row = new Vector<>();
                            row.add(account.getAccountId());
                            row.add(account.getClass().getSimpleName()); // Or a more user-friendly type
                            row.add(String.format("%.2f", account.getBalance()));
                            row.add(account.getAccountStatus().getStatusName());
                            accountsTableModel.addRow(row);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AccountsOverviewPanel.this,
                            "Error loading accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void showCreateAccountDialog(ActionEvent e) {
        CreateAccountDialog dialog = new CreateAccountDialog(mainAppFrame(), bankingFacade, loggedInUser, this::loadAccountData);
        dialog.setVisible(true);
    }

    private void showAccountDetails(ActionEvent e) {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String accountId = (String) accountsTableModel.getValueAt(selectedRow, 0);
            
            new SwingWorker<Optional<TimeAccount>, Void>(){
                @Override
                protected Optional<TimeAccount> doInBackground() throws Exception {
                    return bankingFacade.findAccountById(accountId);
                }

                @Override
                protected void done() {
                    try {
                        Optional<TimeAccount> accountOpt = get();
                        if(accountOpt.isPresent()){
                            AccountDetailsPanel detailsPanel = new AccountDetailsPanel(mainAppFrame(), bankingFacade, accountOpt.get(), AccountsOverviewPanel.this::loadAccountData);
                            JDialog detailsDialog = new JDialog(mainAppFrame(), "Account Details: " + accountId, true);
                            detailsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                            detailsDialog.setContentPane(detailsPanel);
                            detailsDialog.pack();
                            detailsDialog.setLocationRelativeTo(mainAppFrame());
                            detailsDialog.setVisible(true);
                        } else {
                             JOptionPane.showMessageDialog(AccountsOverviewPanel.this, "Could not load details for account: " + accountId, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AccountsOverviewPanel.this, "Error loading account details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();

        } else {
            JOptionPane.showMessageDialog(this, "Please select an account to view details.", "No Account Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private ChronoBankGuiApp mainAppFrame() {
        Component parent = this.getParent();
        while (parent != null && !(parent instanceof ChronoBankGuiApp)) {
            parent = parent.getParent();
        }
        return (ChronoBankGuiApp) parent;
    }
}

