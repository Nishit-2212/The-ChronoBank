package com.chronobank.gui;

import com.chronobank.service.BankingFacade;
import com.chronobank.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterPanel extends JPanel {
    private ChronoBankGuiApp mainApp;
    private BankingFacade bankingFacade;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton backToLoginButton;

    public RegisterPanel(ChronoBankGuiApp mainApp, BankingFacade bankingFacade) {
        this.mainApp = mainApp;
        this.bankingFacade = bankingFacade;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create ChronoBank Account", SwingConstants.CENTER);
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

        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(emailLabel, gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(confirmPasswordField, gbc);

        registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);

        backToLoginButton = new JButton("Already have an account? Login");
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(backToLoginButton, gbc);

        // Action Listeners
        registerButton.addActionListener(this::performRegistration);
        backToLoginButton.addActionListener(e -> mainApp.showPanel("LoginPanel"));
    }

    private void performRegistration(ActionEvent e) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, email, and password cannot be empty.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Basic email validation (more robust validation would use regex or a library)
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return bankingFacade.registerUser(username, password, email);
            }

            @Override
            protected void done() {
                try {
                    User newUser = get();
                    if (newUser != null) {
                        JOptionPane.showMessageDialog(RegisterPanel.this, "Registration Successful! Please login.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                        mainApp.showPanel("LoginPanel");
                        // Clear fields
                        usernameField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                    } else {
                        // Facade prints specific errors to System.err, need a way to get them to UI
                        // For now, a generic message
                        JOptionPane.showMessageDialog(RegisterPanel.this, "Registration failed. Username or email may already exist, or input is invalid.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "An error occurred during registration: " + ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}

