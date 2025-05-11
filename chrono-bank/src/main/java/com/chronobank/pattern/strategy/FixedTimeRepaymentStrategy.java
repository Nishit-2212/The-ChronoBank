package com.chronobank.pattern.strategy;

public class FixedTimeRepaymentStrategy implements LoanRepaymentStrategy {
    private static final String STRATEGY_NAME = "FIXED_TIME_REPAYMENT";

    
    @Override
    public double calculateRepayment(double principal, double periodicInterestRate, int termPeriods) {
        if (periodicInterestRate <= 0) {
            if (termPeriods <=0) return principal; 
            return principal / termPeriods;
        }
        if (termPeriods <= 0) {
            return principal;
        }

        double i = periodicInterestRate;
        double n = termPeriods;

        double monthlyPayment = principal * (i * Math.pow(1 + i, n)) / (Math.pow(1 + i, n) - 1);
        
       
        return Math.round(monthlyPayment * 100.0) / 100.0;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    
}

