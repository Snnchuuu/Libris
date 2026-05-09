package com.libris;

import io.github.cdimascio.dotenv.Dotenv; //used for .env file operations
import java.sql.Connection;	//used for database connections
import java.sql.DriverManager;	//used for database driver management
import java.sql.SQLException;	//used for database exceptions

public class DatabaseManager {
    private static String URL = "jdbc:mysql://localhost:3306/libris_db";
    private static String USER = "root";
    private static String PASSWORD = "";

    static {
        try {
            // .env dosyasını yüklemeye çalış
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            
            // Eğer dosya varsa ve içindeki değerler boş değilse onları kullan
            if (dotenv.get("DB_URL") != null) URL = dotenv.get("DB_URL");
            if (dotenv.get("DB_USER") != null) USER = dotenv.get("DB_USER");
            if (dotenv.get("DB_PASSWORD") != null) PASSWORD = dotenv.get("DB_PASSWORD");
            
        } catch (Exception e) {
            // Dosya yoksa hiçbir şey yapma, yukarıdaki varsayılanlarla devam et
            System.out.println("Bilgi: .env dosyası bulunamadı, varsayılan ayarlarla bağlanılıyor.");
        }
    }

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
