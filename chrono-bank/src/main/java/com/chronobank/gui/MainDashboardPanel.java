package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;

import javax.swing.*;
import java.awt.*;

public class MainDashboardPanel extends JPanel {
    private ChronoBankGuiApp mainApp;
    private BankingFacade bankingFacade;
    private User loggedInUser;

    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;

    // Panels for tabs
    private AccountsOverviewPanel accountsOverviewPanel; // Changed to the specific class
    private OperationsPanel operationsPanel; // Will be a new class for banking operations
    // Potentially more panels for loans, investments, profile etc.

    public MainDashboardPanel(ChronoBankGuiApp mainApp, BankingFacade bankingFacade, User loggedInUser) {
        this.mainApp = mainApp;
        this.bankingFacade = bankingFacade;
        this.loggedInUser = loggedInUser;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Add some spacing

        // Welcome Label
        welcomeLabel = new JLabel("Welcome, " + loggedInUser.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Tabbed Pane for different sections
        tabbedPane = new JTabbedPane();

        // Initialize and add tabs
        accountsOverviewPanel = new AccountsOverviewPanel(this, bankingFacade, loggedInUser);
        tabbedPane.addTab("Accounts Overview", accountsOverviewPanel);

        operationsPanel = new OperationsPanel(this, bankingFacade, loggedInUser);
        tabbedPane.addTab("Banking Operations", operationsPanel);
        
        // Add more tabs as needed based on chrono_bank_gui_design.md
        // e.g., Loan Management, Investment Portfolio, Profile Settings

        add(tabbedPane, BorderLayout.CENTER);

        // Logout Button (could also be in a menu bar)
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            mainApp.showPanel("LoginPanel");
        });
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    // Method to refresh data, e.g., after an operation
    public void refreshData() {
        welcomeLabel.setText("Welcome, " + loggedInUser.getUsername() + "!");
        if (accountsOverviewPanel != null) {
            accountsOverviewPanel.loadAccountData(); // Refresh account list in the overview panel
        }
        if (operationsPanel != null) {
            // operationsPanel.refreshData(); // If operations panel needs to refresh anything (e.g. account dropdowns)
        }
        System.out.println("MainDashboardPanel: Data refresh called.");
    }

    // Getter for the main app frame, useful for dialogs
    public ChronoBankGuiApp getMainAppFrame() {
        return mainApp;
    }
}

