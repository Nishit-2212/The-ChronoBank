package com.chronobank.pattern.strategy;

import com.chronobank.service.TimeMarketService; 

public class DynamicInterestRepaymentStrategy implements LoanRepaymentStrategy {
    private static final String STRATEGY_NAME = "DYNAMIC_INTEREST_REPAYMENT";
    private TimeMarketService timeMarketService; 

    public DynamicInterestRepaymentStrategy(TimeMarketService timeMarketService) {
        this.timeMarketService = timeMarketService;
    }
    
    public DynamicInterestRepaymentStrategy() {
        this.timeMarketService = TimeMarketService.getInstance(); // Example if singleton
    }

   
    @Override
    public double calculateRepayment(double principal, double baseAnnualInterestRate, int termPeriods) {
       
        double marketRateAdjustment = timeMarketService.getCurrentInterestRateAdjustment(); 
        double currentPeriodicInterestRate = (baseAnnualInterestRate / 12) + marketRateAdjustment; 

        if (currentPeriodicInterestRate <= 0) {
            if (termPeriods <= 0) return principal;
            return Math.round((principal / termPeriods) * 100.0) / 100.0;
        }
        if (termPeriods <= 0) {
            return principal; // Full amount due
        }

        double i = currentPeriodicInterestRate;
        double n = termPeriods;

        double monthlyPayment = principal * (i * Math.pow(1 + i, n)) / (Math.pow(1 + i, n) - 1);
        

        return Math.round(monthlyPayment * 100.0) / 100.0;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}

