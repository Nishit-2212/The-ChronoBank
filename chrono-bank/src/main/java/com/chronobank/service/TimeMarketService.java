package com.chronobank.service;

import java.util.Random;

public class TimeMarketService {
    private static TimeMarketService instance;
    private Random random = new Random();

    private TimeMarketService() {
    }

    public static synchronized TimeMarketService getInstance() {
        if (instance == null) {
            instance = new TimeMarketService();
        }
        return instance;
    }

    /**
     * @return A double representing the adjustment factor.
     */
    public double getCurrentInterestRateAdjustment() {
        return (random.nextDouble() * 0.002) - 0.001;
    }

    /**
     * @return A double representing a base annual interest rate.
     */
    public double getBaseMarketRate() {
        return 0.03 + random.nextDouble() * 0.04;
    }

}

