package com.example.dao;

public class DashboardData {
    public double spendLimit;
    public double currentSpend;
    public int limitPeriod;
    public String currencyCode;

    public DashboardData(double spendLimit, double currentSpend, int limitPeriod, String currencyCode) {
        this.spendLimit = spendLimit;
        this.currentSpend = currentSpend;
        this.limitPeriod = limitPeriod;
        this.currencyCode = currencyCode;
    }
}
