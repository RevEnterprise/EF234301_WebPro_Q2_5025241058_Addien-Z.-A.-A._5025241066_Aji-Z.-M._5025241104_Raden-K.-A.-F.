package com.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private String dbUrl;

    public void init(String dbDirPath) {
        this.dbUrl = "jdbc:sqlite:" + dbDirPath + "database.sqlite";
        System.out.println("UserDAO SQLite DB URL set to: " + this.dbUrl);
        
        try {
            Class.forName("org.sqlite.JDBC"); 
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath.");
            throw new RuntimeException("Missing SQLite Driver!", e); 
        }
        
        try (Connection conn = getConnection()) {
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "overall_spend_limit_per_day REAL DEFAULT 0.0, " +
                "overall_spend_limit_period_setting INTEGER DEFAULT 1, " +
                "current_overall_spending REAL DEFAULT 0.0, " +
                "currency_setting TEXT DEFAULT 'RP'" +
                ")")
            ) {
                stmt.executeUpdate();
                System.out.println("SQLite table 'users' ensured to exist with all spending and currency attributes.");
            }
            
            insertSampleUsers(); 
            
        } catch (SQLException e) {
            System.err.println("Error creating database tables in UserDAO: " + e.getMessage());
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.dbUrl);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    private void insertSampleUsers() {
        if (getUserCount() == 0) {
            System.out.println("Inserting sample users...");
            registerUser("admin", "adminpass");
            registerUser("testuser", "password123");
            registerUser("guest", "guest");
        }
    }

    private int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        return 0;
    }


    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); 
            
            return stmt.executeUpdate() > 0; 
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public int validateUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Login validation error: " + e.getMessage());
        }
        return 0; 
    }

    public boolean updateUsername(String oldUsername, String newUsername) {
        String sql = "UPDATE users SET username=? WHERE username=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, oldUsername);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE username=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public void updateOverallSpendLimit(int userId, double newLimit) throws SQLException {
        String sql = "UPDATE users SET overall_spend_limit_per_day = ? WHERE id = ?";
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newLimit);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        }
    }

    public void updateSpendPeriodSetting(int userId, int newPeriodSetting) throws SQLException {
        String sql = "UPDATE users SET overall_spend_limit_period_setting = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newPeriodSetting);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        }
    }

    public void incrementCurrentOverallSpending(int userId, double amountToSpend) throws SQLException {
        String sql = "UPDATE users SET current_overall_spending = current_overall_spending + ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amountToSpend);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        }
    }

    public void updateCurrencySetting(int userId, String newCurrencySetting) throws SQLException {
        String sql = "UPDATE users SET currency_setting = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCurrencySetting);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        }
    }

    public DashboardData getDashboardDataById(int userId) throws SQLException {
        String sql = "SELECT overall_spend_limit_per_day, current_overall_spending, " +
                    "overall_spend_limit_period_setting, currency_setting " +
                    "FROM users WHERE id = ?";
        
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double spendLimit = rs.getDouble("overall_spend_limit_per_day");
                    double currentSpend = rs.getDouble("current_overall_spending");
                    int limitPeriod = rs.getInt("overall_spend_limit_period_setting");
                    String currencyCode = rs.getString("currency_setting");
                    
                    return new DashboardData(spendLimit, currentSpend, limitPeriod, currencyCode);
                }
            }
        }
        return null;
    }


}
