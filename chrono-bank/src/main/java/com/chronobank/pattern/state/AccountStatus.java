package com.chronobank.pattern.state;

import com.chronobank.model.account.TimeAccount;
import java.io.Serializable;

public interface AccountStatus extends Serializable {
    void handleDeposit(TimeAccount account, double amount);
    void handleWithdrawal(TimeAccount account, double amount) throws IllegalStateException;
    String getStatusName();

}

