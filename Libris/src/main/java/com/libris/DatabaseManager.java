package com.libris;

import io.github.cdimascio.dotenv.Dotenv; //used for .env file operations
import java.sql.Connection;	//used for database connections
import java.sql.DriverManager;	//used for database driver management
import java.sql.SQLException;	//used for database exceptions

public class DatabaseManager {
    // load the .env file
	private static final Dotenv dotenv = Dotenv.load();

    // Use the informations in the .env file privately
	private static final String URL = dotenv.get("DB_URL");
	private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {	//Method for getting connection informations
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Error: Check if MySQL works or not!");	//This message pops up if an error occurs while you connect to your database
            throw e;
        }
    }

    // Test etmek için ana metod Sonra silinecek!!!!
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("Congrulations! Libris has successfully connected to the database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}