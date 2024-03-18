package dal.db;

import be.User;
import bll.util.crytographic.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User_DB {

    private final myDBConnector myDBConnector;

    private static ArrayList<be.User> allUser;

    public User_DB() throws Exception {
        myDBConnector = new myDBConnector();
        allUser= new ArrayList<>();
    }

    //Login Part
    public User checkUserBCrypt(String username, String password) throws Exception {
        String query = "SELECT * FROM dbo.Users WHERE Username = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // Check if the user exists
            if (rs.next()) {
                String hashedPassword = rs.getString("Password");
                // Use BCrypt to verify the password
                if (BCrypt.checkpw(password, hashedPassword)) {


                    // Passwords match, return the user object
                    //   return generateUser(rs);

                    return generateUser(rs);
                }
            }
            return null;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get user from database", ex);
        }
    }



    //User Part
    public List<User> getAllUsers() throws Exception {

        try (Connection conn = myDBConnector.getConnection();
            Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM dbo.Users;";
            ResultSet rs = stmt.executeQuery(sql);
            // Loop through rows from the database result set
            while (rs.next()) {
                allUser.add(generateUser(rs));
            }
            return allUser;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get users from database", ex);
        }
    }



    public User createUser(User newUser) throws Exception {
        String sql = "INSERT INTO dbo.Users (Username, Password, userAccessLevel) VALUES (?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUser.getUserName());
            pstmt.setString(2, newUser.getPassword());
            pstmt.setInt(3, newUser.getUserAccessLevel());
            pstmt.executeUpdate();

            User user = new User (newUser.getUserName(), newUser.getPassword(), newUser.getUserAccessLevel());

            allUser.add(user);
            return  user;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not create user in database", ex);
        }
    }

    public void updateUser(User user) throws Exception {
        String sql = "UPDATE dbo.Users SET Password = ?, userAccessLevel = ? WHERE Username = ?";

        try (Connection conn = myDBConnector.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getPassword());
            stmt.setInt(2, user.getUserAccessLevel());
            stmt.setString(3, user.getUserName());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not update User", ex);
        }
    }

    public void removeUser(User user) throws Exception {
        String sql = "DELETE FROM dbo.Users WHERE Username = ?";
        try (Connection conn = myDBConnector.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserName());
            stmt.executeUpdate();
            allUser.remove(user);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not remove user from database", ex);
        }
    }

    // Helper method to generate a User object from the ResultSet
    private User generateUser(ResultSet rs) throws SQLException {
        // Implement this method based on your User class structure
        return new User(rs.getString("Username"), rs.getString("Password"), rs.getInt("userAccessLevel"));
    }


}












