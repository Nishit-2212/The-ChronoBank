package com.chronobank.service;

import com.chronobank.model.account.TimeAccount;
import com.chronobank.pattern.observer.AccountObserver;

public class NotificationService implements AccountObserver {

    @Override
    public void update(TimeAccount account, String messageType, Object data) {
        if (account == null || messageType == null) {
            return;
        }

        String ownerEmail = (account.getOwner() != null) ? account.getOwner().getEmail() : "[Unknown Email]";
        String accountId = account.getAccountId();

        if (account.getPreferences() != null && !account.getPreferences().getNotificationPreference(messageType)) {
            System.out.println("NotificationService: Notification type '" + messageType + "' disabled for account " + accountId);
            return;
        }

        String notificationMessage = "";

        switch (messageType) {
            case "LOW_BALANCE":
                notificationMessage = String.format(
                    "Dear User, your ChronoBank account %s has a low balance: %.2f. Please top up soon.",
                    accountId, (Double) data
                );
                break;
            case "LARGE_TRANSACTION":
                notificationMessage = String.format(
                    "Dear User, a large transaction of amount %.2f was detected on your ChronoBank account %s.",
                    (Double) data, accountId
                );
                break;
            case "LOAN_REMINDER":
                notificationMessage = String.format(
                    "Dear User, a loan repayment is due soon for your ChronoBank account %s. Details: %s",
                    accountId, data.toString()
                );
                break;
            case "STATUS_CHANGE":
                notificationMessage = String.format(
                    "Dear User, the status of your ChronoBank account %s has changed to: %s.",
                    accountId, (String) data
                );
                break;
            case "SUSPICIOUS_TRANSACTION":
                notificationMessage = String.format(
                    "Dear User, a suspicious transaction has been detected on your ChronoBank account %s. Details: %s. Please review your account activity.",
                    accountId, data.toString()
                );
                break;
            case "DEPOSIT_COMPLETED":
                notificationMessage = String.format(
                    "Dear User, a deposit of %.2f has been successfully made to your ChronoBank account %s. Current balance: %.2f.",
                    (Double) data, accountId, account.getBalance()
                );
                break;
            case "WITHDRAWAL_COMPLETED":
                notificationMessage = String.format(
                    "Dear User, a withdrawal of %.2f has been successfully made from your ChronoBank account %s. Current balance: %.2f.",
                    (Double) data, accountId, account.getBalance()
                );
                break;
            default:
                System.out.println("NotificationService: Received unhandled message type '" + messageType + "' for account " + accountId);
                return;
        }

        sendNotification(ownerEmail, "ChronoBank Alert for Account: " + accountId, notificationMessage);
    }

    public void sendNotification(String recipientEmail, String subject, String body){
        System.out.println("------- NOTIFICATION  IS  SENT ------");
        System.out.println("To: " + recipientEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("---------------------------");
    }
}

