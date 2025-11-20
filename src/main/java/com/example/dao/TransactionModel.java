package com.example.dao;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;

public class TransactionModel {
    private int id;
    private int userId;
    private String name;
    private Integer categoryId;
    private double amount;
    private String transactionDate;

    public TransactionModel() {
    }

    public TransactionModel(int id, int userId, String name, Integer categoryId, double amount, String transactionDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    public TransactionModel(int userId, String name, Integer categoryId, double amount, String transactionDate) {
        this.userId = userId;
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDate() {
        return this.transactionDate; 
    }

    public String getFormattedTransactionDate() {
        final DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm");
        
        if (this.transactionDate == null || this.transactionDate.trim().isEmpty()) {
            return ""; 
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(this.transactionDate, dbFormatter);
            return dateTime.format(outputFormatter);
            
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date: " + this.transactionDate);
            return this.transactionDate; 
        }
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
}
