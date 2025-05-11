package com.chronobank.model.account;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountPreferences {
    private final double transactionLimitPerDay;
    private final Map<String, Boolean> notificationPreferences;
    private final String preferredInterestRateType; 

    
    private AccountPreferences(Builder builder) {
        this.transactionLimitPerDay = builder.transactionLimitPerDay;
        this.notificationPreferences = builder.notificationPreferences;
        this.preferredInterestRateType = builder.preferredInterestRateType;
    }

    
    public double getTransactionLimitPerDay() {
        return transactionLimitPerDay;
    }

    public Map<String, Boolean> getNotificationPreferences() {
        return new HashMap<>(notificationPreferences);
    }

    public boolean getNotificationPreference(String type) {
        return notificationPreferences.getOrDefault(type, false);
    }

    public String getPreferredInterestRateType() {
        return preferredInterestRateType;
    }

    @Override
    public String toString() {
        return "AccountPreferences{" +
                "transactionLimitPerDay=" + transactionLimitPerDay +
                ", notificationPreferences=" + notificationPreferences +
                ", preferredInterestRateType='" + preferredInterestRateType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountPreferences that = (AccountPreferences) o;
        return Double.compare(that.transactionLimitPerDay, transactionLimitPerDay) == 0 &&
                Objects.equals(notificationPreferences, that.notificationPreferences) &&
                Objects.equals(preferredInterestRateType, that.preferredInterestRateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionLimitPerDay, notificationPreferences, preferredInterestRateType);
    }

    // Static Builder class
    public static class Builder {
        private double transactionLimitPerDay = 5000.0; // Default value
        private Map<String, Boolean> notificationPreferences = new HashMap<>();
        private String preferredInterestRateType = "FIXED"; // Default value

        public Builder() {
            // Initialize with some default notification preferences if needed
            notificationPreferences.put("LOW_BALANCE", true);
            notificationPreferences.put("LARGE_TRANSACTION", true);
            notificationPreferences.put("LOAN_REMINDER", true);
        }

        public Builder withTransactionLimit(double limit) {
            this.transactionLimitPerDay = limit;
            return this;
        }

        public Builder withNotificationPreference(String type, boolean enabled) {
            this.notificationPreferences.put(type, enabled);
            return this;
        }

        public Builder withPreferredInterestRateType(String rateType) {
            this.preferredInterestRateType = rateType;
            return this;
        }

        public AccountPreferences build() {
            return new AccountPreferences(this);
        }
    }
}

