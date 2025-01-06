package DBHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/gimatagobrero";
    private static final String USER = "root";
    //private static final String PASSWORD = "shanna05";
    private static final String PASSWORD = "Gimatag2024";
//    private static final String PASSWORD = "maclang@2023-00570";

    public static Connection connect(){
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }   catch (SQLException e){
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
