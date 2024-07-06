import javax.print.DocFlavor;
import java.net.URI;
import java.security.spec.ECField;
import java.sql.*;

public class DataBaseManager {
    private static final String DB_URL = "jdbc::mysql://localhost:3306/user_db";
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

    public int getUserId(User user) throws Exception {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getInt("id");
            }
            throw new Exception("User does not exist in database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
        //TODO: add Network Exception
    }

    public boolean isUserLoggedIn(User user) {
        int userId;
        try {
            userId = getUserId(user);
        } catch (Exception e) {
            return false;
        }
        String query = "SELECT * FROM user_status WHERE id = ?";
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getBoolean("is_logged_in");
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean logInUser(User user) throws Exception { // User not found Exception
        return setLogInStatus(user, true);
    }

    public boolean logOutUser(User user) throws Exception { // User not found Exception
        return setLogInStatus(user, false);
    }

    public boolean setLogInStatus(User user, boolean logInStatus) throws Exception {
        int userId;
        try {
            userId = getUserId(user);
        } catch (Exception e) {
            throw new Exception("user does not exist in database");
        }
        String query = "UPDATE user_status SET is_logged_in = ? WHERE user_id = ?";
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setBoolean(1, logInStatus);
            pstmt.setInt(2, userId);

            int rowsUpdatedNumber = pstmt.executeUpdate();
            return rowsUpdatedNumber > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            int rowsInsertedNumber = pstmt.executeUpdate();
            assert rowsInsertedNumber > 0;
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next()) {
                int generatedId = rs.getInt(1);
                return initializeUserStatus(generatedId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doesUsernameExist(String username) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean initializeUserStatus(int generatedId) {
        String query = "INSERT INTO user_status (user_id, id_logged_in) VALUES (?, ?)";
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, generatedId);
            pstmt.setBoolean(2, false);

            int rowsInsertedNumber = pstmt.executeUpdate();
            return rowsInsertedNumber > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
