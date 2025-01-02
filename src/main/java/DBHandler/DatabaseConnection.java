package DBHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/gimatagObrero";
    private static final String USER = "GiMaTag";
    private static final String PASSWORD = "DBMS_LE";

    public static Connection connect(){
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }   catch (SQLException e){
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}
