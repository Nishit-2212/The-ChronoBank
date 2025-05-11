package com.chronobank.pattern.strategy;

public interface LoanRepaymentStrategy {
    /**
     * @param principal The outstanding principal amount of the loan.
     * @param interestRate The annual interest rate (e.g., 0.05 for 5%).
     * @param termPeriods The number of periods remaining or total for the loan (e.g., months).
     * @return The calculated repayment amount for the current period.
     */
    double calculateRepayment(double principal, double interestRate, int termPeriods);

    /**
     * @return A string representing the name of the repayment strategy.
     */
    String getStrategyName();

    /**
     * @param principal The initial principal amount of the loan.
     * @param interestRate The annual interest rate.
     * @param totalTermPeriods The total number of periods for the loan.
     * @return The total interest paid.
     */
}

