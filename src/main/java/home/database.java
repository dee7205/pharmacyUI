package home;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    private static final String URL = "jdbc:mysql://localhost:3306/gimatagObrero";
    private static final String USER = "root";
    private static final String PASSWORD = "Gimatag2024";  // Adjust password if needed

    public static Connection getConnection() throws SQLException {
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Error connecting to the database.", e);
        }
    }
}
