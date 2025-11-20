package com.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CategoryDAO {

    private final String dbUrl;

    public CategoryDAO(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found.", e);
        }
    }

    public void init() {
        System.out.println("Initializing CategoryDAO...");
        createCategoriesTable();
        System.out.println("CategoryDAO initialization complete.");
    }

    private void createCategoriesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS categories (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "user_id INTEGER NOT NULL," +
                     "name TEXT NOT NULL," +
                     "spend_limit_per_day REAL DEFAULT 0," +
                     "spend_limit_period_settings INTEGER DEFAULT 1," +
                     "current_category_spending REAL DEFAULT 0," +
                     "alert_enabled INTEGER DEFAULT 1," +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ");";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("Table 'categories' created or already exists.");

        } catch (SQLException e) {
            System.err.println("Error creating 'categories' table: " + e.getMessage());
            throw new RuntimeException("Failed to create 'categories' table.", e);
        }
    }
}
