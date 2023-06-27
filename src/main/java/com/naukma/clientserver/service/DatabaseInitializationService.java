package com.naukma.clientserver.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializationService {

    public Connection createConnection(String dbName) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            createDbScheme(connection);
        } catch(ClassNotFoundException e) {
            System.out.println("JDBC Driver not found");
            e.printStackTrace();
            System.exit(0);
        } catch (SQLException e) {
            System.out.println("Invalid SQL query");
            e.printStackTrace();
        }
        return connection;
    }

    public void createDbScheme(Connection connection) {
        String sqlCreateGoodGroup = "CREATE TABLE IF NOT EXISTS good_group " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "description TEXT NOT NULL)";

        String sqlCreateGood = "CREATE TABLE IF NOT EXISTS good " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "description TEXT NOT NULL, " +
                "producer TEXT NOT NULL, " +
                "quantity INTEGER CHECK (quantity >= 0) NULL, " +
                "price REAL CHECK (price >= 0) NOT NULL, " +
                "groupId INTEGER NOT NULL, " +
                "FOREIGN KEY(groupId) REFERENCES good_group(id) ON DELETE CASCADE)";

        String sqlCreateUser = "CREATE TABLE IF NOT EXISTS user " +
                "(login TEXT UNIQUE NOT NULL PRIMARY KEY, " +
                "password TEXT NOT NULL)";

        try (Statement stmt = connection.createStatement()) {
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.executeUpdate(sqlCreateGoodGroup);
            stmt.executeUpdate(sqlCreateGood);
            stmt.executeUpdate(sqlCreateUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
