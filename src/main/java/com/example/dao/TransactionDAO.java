package com.example.dao;

import com.example.dao.TransactionModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    private final String dbUrl;
    
    public TransactionDAO(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found.", e);
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.dbUrl);
    }

    public void init() {
        System.out.println("Initializing TransactionDAO...");
        
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "user_id INTEGER NOT NULL, " +
                     "name TEXT NOT NULL, " +
                     "category_id INTEGER, " +
                     "amount REAL NOT NULL, " +
                     "transaction_date TEXT NOT NULL, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL" +
                     ")";
        
        try (Connection conn = getConnection();
              Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("SQLite table 'transactions' ensured to exist.");
            
        } catch (SQLException e) {
            System.err.println("Error creating database table in TransactionDAO: " + e.getMessage());
            throw new RuntimeException("Failed to create 'transactions' table.", e);
        }
    }

    public List<TransactionModel> getAllTransactionsByUser(int userId) {
        List<TransactionModel> transactions = new ArrayList<>();
        String sql = "SELECT id, user_id, name, category_id, amount, transaction_date FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    Integer categoryId = (Integer) rs.getObject("category_id");
                    double amount = rs.getDouble("amount");
                    String transactionDate = rs.getString("transaction_date");

                    transactions.add(new TransactionModel(id, userId, name, categoryId, amount, transactionDate));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all transactions for user " + userId + ": " + e.getMessage());
        }
        return transactions;
    }

    public boolean saveNewTransaction(TransactionModel transaction) {
        String transactionDate = transaction.getTransactionDate();
        
        String sql = "INSERT INTO transactions(user_id, name, category_id, amount, transaction_date) VALUES(?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transaction.getUserId());
            stmt.setString(2, transaction.getName());
            
            Integer categoryId = transaction.getCategoryId();
            
            if (categoryId != null) {
                stmt.setInt(3, categoryId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.setDouble(4, transaction.getAmount());
            stmt.setString(5, transactionDate);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving new transaction: " + e.getMessage());
            return false;
        }
    }


    private double calculateTotalSpending(int userId, String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) * -1 FROM transactions WHERE user_id = ?";
        
        if (startDate != null && endDate != null) {
            sql += " AND DATE(transaction_date) BETWEEN ? AND ?"; 
        } else if (startDate != null) {
            sql += " AND DATE(transaction_date) >= ?";
        } else if (endDate != null) {
            sql += " AND DATE(transaction_date) <= ?";
        }
        
        int paramIndex = 1;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(paramIndex++, userId);

            if (startDate != null && endDate != null) {
                stmt.setString(paramIndex++, startDate);
                stmt.setString(paramIndex++, endDate);
            } else if (startDate != null) {
                stmt.setString(paramIndex++, startDate);
            } else if (endDate != null) {
                stmt.setString(paramIndex++, endDate);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total spending for user " + userId + ": " + e.getMessage());
        }
        return 0.0;
    }

    public double calculateSpendingToday(int userId) {
        String today = LocalDate.now().toString(); 
        return calculateTotalSpending(userId, today, today);
    }

    public double calculateSpendingThisMonth(int userId) {
        LocalDate today = LocalDate.now();
        
        String startOfMonth = today.withDayOfMonth(1).toString();
        
        String endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).toString(); 
        
        return calculateTotalSpending(userId, startOfMonth, endOfMonth);
    }

    public double calculateSpendingThisYear(int userId) {
        LocalDate today = LocalDate.now();
        
        String startOfYear = today.withDayOfYear(1).toString(); 
        
        String endOfYear = today.withMonth(12).withDayOfMonth(31).toString(); 
        
        return calculateTotalSpending(userId, startOfYear, endOfYear);
    }

    public double calculateSpendingAllTime(int userId) {
        return calculateTotalSpending(userId, null, null); 
    }
}
