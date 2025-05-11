package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class LoginPanel extends JPanel {
    private ChronoBankGuiApp mainApp;
    private BankingFacade bankingFacade;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginPanel(ChronoBankGuiApp mainApp, BankingFacade bankingFacade) {
        this.mainApp = mainApp;
        this.bankingFacade = bankingFacade;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ChronoBank Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.1;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0;

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        registerButton = new JButton("Don't have an account? Register");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(registerButton, gbc);

        // Action Listeners
        loginButton.addActionListener(this::performLogin);
        registerButton.addActionListener(e -> mainApp.showPanel("RegisterPanel"));
    }

    private void performLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use SwingWorker for backend operation
        new SwingWorker<Optional<User>, Void>() {
            @Override
            protected Optional<User> doInBackground() throws Exception {
                return bankingFacade.loginUser(username, password);
            }

            @Override
            protected void done() {
                try {
                    Optional<User> userOptional = get();
                    if (userOptional.isPresent()) {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Login Successful! Welcome " + userOptional.get().getUsername(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                        mainApp.showMainDashboard(userOptional.get());
                        // Clear fields after successful login
                        usernameField.setText("");
                        passwordField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "An error occurred during login: " + ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}

