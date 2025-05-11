package com.chronobank.model.common;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class Investment {
    private String investmentId;
    private String accountId; 
    private String investmentType;
    private double amountInvested;
    private Timestamp startDate;
    private Timestamp maturityDate;
    private double currentValue;
    private String status; 
    private Timestamp lastUpdatedAt;

    public Investment(String accountId, String investmentType, double amountInvested, double initialValue, Timestamp startDate, Timestamp maturityDate) {
        this.investmentId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.investmentType = investmentType;
        this.amountInvested = amountInvested;
        this.currentValue = initialValue; 
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.status = "ACTIVE"; 
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }
    
   
    public Investment(String investmentType, double amountInvested, Timestamp startDate) {
        this(null, investmentType, amountInvested, amountInvested, startDate, null);
    }


   
    public String getInvestmentId() {
        return investmentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getInvestmentType() {
        return investmentType;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getMaturityDate() {
        return maturityDate;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getLastUpdatedAt() {
        return lastUpdatedAt;
    }

   
    public void setInvestmentId(String investmentId) {
        this.investmentId = investmentId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setInvestmentType(String investmentType) {
        this.investmentType = investmentType;
    }

    public void setAmountInvested(double amountInvested) {
        this.amountInvested = amountInvested;
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setMaturityDate(Timestamp maturityDate) {
        this.maturityDate = maturityDate;
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setStatus(String status) {
        this.status = status;
        this.lastUpdatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Investment that = (Investment) o;
        return Objects.equals(investmentId, that.investmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(investmentId);
    }

    @Override
    public String toString() {
        return "Investment{" +
                "investmentId='" + investmentId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", investmentType='" + investmentType + '\'' +
                ", amountInvested=" + amountInvested +
                ", currentValue=" + currentValue +
                ", startDate=" + startDate +
                ", maturityDate=" + maturityDate +
                ", status='" + status + '\'' +
                ", lastUpdatedAt=" + lastUpdatedAt +
                '}';
    }
}

