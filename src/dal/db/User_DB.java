package dal.db;

import be.User;
import bll.util.crytographic.BCrypt;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User_DB {

    private final myDBConnector myDBConnector;

    private static ArrayList<be.User> allUser;

    public User_DB() throws Exception {
        myDBConnector = new myDBConnector();
        allUser = new ArrayList<>();
    }

//***********************************LOGIN*************************************
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
                    return generateUser(rs);  // Passwords match, return the user object
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new Exception("Wrong username or password", ex);
        }
    }


//********************************CRUD*USER***********************************
    public User createUser(User newUser) throws Exception {
        String sql = "INSERT INTO dbo.Users (Username, Password, userAccessLevel) VALUES (?, ?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, newUser.getUserName());
            pStmt.setString(2, newUser.getPassword());
            pStmt.setInt(3, newUser.getUserAccessLevel());
            pStmt.executeUpdate();

            User user = new User(newUser.getUserName(), newUser.getPassword(), newUser.getUserAccessLevel());

            allUser.add(user);
            return user;
        } catch (SQLException ex) {
            throw new Exception("Could not create user in database", ex);
        }
    }

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
            throw new Exception("Could not get users from database", ex);
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
            throw new Exception("Could not remove user from database", ex);
        }
    }

//***************************CRUD*PROFILE*IMAGE*******************************
    public void createUserProfileIMG(User selectedUser) throws Exception {
        String sqlInsert = "INSERT INTO dbo.UsersProfileIMG (Username, IMG) VALUES (?, ?)";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
            insertStmt.setString(1, selectedUser.getUserName());
            byte[] imageData = getImageData(selectedUser.getProfileIMG());
            insertStmt.setBytes(2, imageData);
            insertStmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Error creating user profile image", ex);
        }
    }

    public void readUserProfileIMG(User selectedUser) throws Exception {
        String sql = "SELECT IMG FROM dbo.UsersProfileIMG WHERE Username = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedUser.getUserName());
            try (ResultSet rs = stmt.executeQuery()) {
                // Check if the result set has any rows (indicating that the user has a profile image)
                if (rs.next()) {
                    // User has a profile image, retrieve and set it
                    byte[] imageData = rs.getBytes("IMG");
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                    Image profileImage = new Image(inputStream);
                    selectedUser.setProfileIMG(profileImage);
                }
            } catch (SQLException ex) {
                throw new Exception("Error loading user profile Image", ex);
            }
        }
    }

    public void updateUserProfileIMG(User selectedUser) throws Exception {
        String sqlUpdate = "UPDATE dbo.UsersProfileIMG SET IMG = ? WHERE Username = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
            byte[] imageData = getImageData(selectedUser.getProfileIMG());
            updateStmt.setBytes(1, imageData);
            updateStmt.setString(2, selectedUser.getUserName());
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("User not found or does not have a profile image.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error updating user profile image", ex);
        }
    }

    public void deleteUserProfileIMG(User selectedUser) throws Exception {
        String sql = "DELETE FROM dbo.UsersProfileIMG WHERE Username = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedUser.getUserName());
            stmt.executeUpdate();
            allUser.remove(selectedUser);
        } catch (SQLException ex) {
            throw new Exception("Could not delete user profile picture", ex);
        }
    }

//***************************CRUD*PROFILE*IMAGE*******************************
    private byte[] getImageData(Image image) throws IOException { //Helper method to get image data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    private User generateUser(ResultSet rs) throws SQLException {  // Helper method to generate a User object from the ResultSet
        return new User(rs.getString("Username"), rs.getString("Password"), rs.getInt("userAccessLevel"));
    }

}













