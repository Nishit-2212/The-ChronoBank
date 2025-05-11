package com.chronobank.pattern.observer;

import com.chronobank.model.account.TimeAccount;


public interface AccountObserver {
    /**
     * @param account the account that triggered the notification.
     * @param messageType a string indicating the type of event (example:- "LOW_BALANCE", "LARGE_TRANSACTION", "STATUS_CHANGE").
     * @param data optional data related to the event (e.g., transaction amount, new status).
     */
    void update(TimeAccount account, String messageType, Object data);
}

