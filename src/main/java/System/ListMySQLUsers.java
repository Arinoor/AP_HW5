package System;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListMySQLUsers {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_db";
    private static final String USER = "root";
    private static final String PASS = "lmqsy.Arinoor_8053";

    public static void main(String[] args) {
        String query = "SELECT user, host FROM mysql.user";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String user = rs.getString("user");
                String host = rs.getString("host");
                System.out.println("System.User: " + user + ", Host: " + host);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
