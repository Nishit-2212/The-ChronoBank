package com.chronobank.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/chronobank"; 
    private static final String DB_USER = "postgres"; 
    private static final String DB_PASSWORD = "123"; 

    private static Connection connection = null;

    // this is a private constructor to prevent instantiation from other classes
    private DatabaseConnector() {}

    /**
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                System.out.println("attempting to connect to postgreSQL database: " + DB_URL);
                System.out.println("Using User: " + DB_USER );
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("PostgreSQL Database is Successful Connected !");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC Driver not found. Include it in your library path.");
                e.printStackTrace();
                throw new SQLException("JDBC Driver not found", e);
            } catch (SQLException e) {
                System.err.println("Database Connection Failed! Check console for errors.");
                e.printStackTrace();
                throw e; 
            }
        }
        return connection;
    }

   
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database Connection Closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

