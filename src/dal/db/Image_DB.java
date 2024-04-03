package dal.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

public class Image_DB {

    private final myDBConnector myDBConnector;

    public Image_DB() throws IOException {
        myDBConnector = new myDBConnector(); // Initialize your connector here
    }

    public ImageView readSystemIMG(int IMGID) {
        String sql = "SELECT IMG FROM SystemIMG WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, IMGID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("IMG");
                Image img = new Image(new ByteArrayInputStream(imgBytes));
                return new ImageView(img);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int createSystemIMG(Image image) {
        // Adjust SQL to remove IMGID from INSERT statement
        String sql = "INSERT INTO SystemIMG (IMG) VALUES (?)";
        // Initialize the result ID to a default value that indicates failure
        int generatedId = -1;

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            byte[] imageData = getImageData(image);
            pstmt.setBytes(1, imageData);
            pstmt.executeUpdate();
            // Retrieve the generated key for the inserted row
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // Get the generated key
                    generatedId = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public void uploadSystemIMG(int IMGID, Image newImage) {
        String sql = "UPDATE SystemIMG SET IMG = ? WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            byte[] imageData = getImageData(newImage);
            pstmt.setBytes(1, imageData);
            pstmt.setInt(2, IMGID);
            pstmt.executeUpdate();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteSystemIMG(int IMGID) {
        String sql = "DELETE FROM SystemIMG WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, IMGID);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getNextIDSystemIMG() {
        String sql = "SELECT IDENT_CURRENT('SystemIMG') + 1";
        int nextId = 0;
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return nextId;
    }

    //Helper method to get image data
    private byte[] getImageData(Image image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputStream);
        return outputStream.toByteArray();
    }
}
