package System;

import java.sql.*;
import Exception.*;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_db";
    private static final String USER = "root";
    private static final String PASS = "lmqsy.Arinoor_8053";

    public DataBaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            return null;
        }
    }

    public int getUserId(User user) throws UserNotRegisteredException, SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        ResultSet rs = pstmt.executeQuery();

        if(rs.next()) {
            return rs.getInt("id");
        }
        throw new UserNotRegisteredException("Username or Password is incorrect");
    }

    public boolean logInUser(User user) throws DatabaseException, UserNotRegisteredException, SQLException {
        if(getLogInStatus(user) == true) {
            throw new LogInException("User is already logged in");
        }
        return setLogInStatus(user, true);
    }

    public boolean logOutUser(User user) throws DatabaseException, UserNotRegisteredException, SQLException {
        if(getLogInStatus(user) == false) {
            throw new LogOutException("User is already logged out");
        }
        return setLogInStatus(user, false);
    }


    public boolean getLogInStatus(User user) throws DatabaseException, UserNotRegisteredException, SQLException{
        int userId = getUserId(user);
        String query = "SELECT is_logged_in FROM user_status WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        if(rs.next()) {
            return rs.getBoolean("is_logged_in");
        }
        throw new DatabaseException("User is registered but does not exist in user_status database");
    }

    public boolean setLogInStatus(User user, boolean logInStatus) throws DatabaseException, UserNotRegisteredException, SQLException {
        int userId = getUserId(user);
        String query = "UPDATE user_status SET is_logged_in = ? WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, logInStatus);
        pstmt.setInt(2, userId);

        int rowsUpdatedNumber = pstmt.executeUpdate();
        if(rowsUpdatedNumber > 0)
            return true;
        throw new DatabaseException("User is registered but does not exist in user_status database");

    }

    public boolean registerUser(User user) throws NotAvailableUsernameException, DatabaseException, SQLException {
        checkUsernameAvailable(user.getUsername());
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());

        int rowsInsertedNumber = pstmt.executeUpdate();
        if(rowsInsertedNumber == 0)
            throw new DatabaseException("unexpected database behavior");
        ResultSet rs = pstmt.getGeneratedKeys();
        if(rs.next()) {
            int generatedId = rs.getInt(1);
            return initializeUserStatus(generatedId);
        }
        throw new DatabaseException("unexpected database behavior");
    }

    public void checkUsernameAvailable(String username) throws NotAvailableUsernameException, DatabaseException, SQLException{
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();

        if(!rs.next())
            throw new DatabaseException("unexpected database behavior");
        if(rs.getBoolean(1)) {
            throw new NotAvailableUsernameException("username is not available");
        }

    }

    private boolean initializeUserStatus(int generatedId) throws DatabaseException, SQLException{
        String query = "INSERT INTO user_status (user_id, is_logged_in) VALUES (?, ?)";
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, generatedId);
        pstmt.setBoolean(2, false);

        int rowsInsertedNumber = pstmt.executeUpdate();
        if(rowsInsertedNumber == 0) {
            throw new DatabaseException("unexpected database behavior");
        }
        return true;
    }

    public static void main(String[] args) {
        DataBaseManager ds = new DataBaseManager();
        Connection conn = ds.getConnection();
        if(conn == null) {
            System.out.println("Null");
        }
    }

}
