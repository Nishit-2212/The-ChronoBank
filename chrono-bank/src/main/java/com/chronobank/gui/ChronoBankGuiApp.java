package com.chronobank.gui;

import com.chronobank.service.BankingFacade;

import javax.swing.*;
import java.awt.*;

public class ChronoBankGuiApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private MainDashboardPanel mainDashboardPanel;
    private BankingFacade bankingFacade;

    public ChronoBankGuiApp() {
        this.bankingFacade = new BankingFacade(); // Initialize the facade

        setTitle("ChronoBank System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize panels
        loginPanel = new LoginPanel(this, bankingFacade);
        registerPanel = new RegisterPanel(this, bankingFacade);
        // MainDashboardPanel will be initialized after successful login, passing the User object

        // Add panels to the CardLayout
        mainPanel.add(loginPanel, "LoginPanel");
        mainPanel.add(registerPanel, "RegisterPanel");
        // mainDashboardPanel will be added dynamically

        add(mainPanel);

        // Show LoginPanel initially
        cardLayout.show(mainPanel, "LoginPanel");
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public void showMainDashboard(com.chronobank.model.user.User loggedInUser) {
        if (mainDashboardPanel != null) {
            mainPanel.remove(mainDashboardPanel); // Remove old instance if any (e.g., re-login)
        }
        mainDashboardPanel = new MainDashboardPanel(this, bankingFacade, loggedInUser);
        mainPanel.add(mainDashboardPanel, "MainDashboardPanel");
        cardLayout.show(mainPanel, "MainDashboardPanel");
    }

    public static void main(String[] args) {
        // Ensure UI updates are on the EDT
        SwingUtilities.invokeLater(() -> {
            ChronoBankGuiApp app = new ChronoBankGuiApp();
            app.setVisible(true);
        });
    }
}

