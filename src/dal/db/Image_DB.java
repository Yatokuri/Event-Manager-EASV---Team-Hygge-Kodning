package dal.db;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Image_DB {

    private final myDBConnector myDBConnector;

    public Image_DB() throws IOException {
        myDBConnector = new myDBConnector(); // Initialize your connector here

    }

    public ImageView readSystemIMG(int IMGId) throws Exception {
        String sql = "SELECT IMG FROM SystemIMG WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, IMGId);
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("IMG");
                Image img = new Image(new ByteArrayInputStream(imgBytes));
                return new ImageView(img);
            }
        } catch (SQLException ex) {
            throw new Exception("Could not fetch Image", ex);
        }
        return null;
    }

    public int createSystemIMG(Image image) throws Exception {
        // Adjust SQL to remove IMGId from INSERT statement
        String sql = "INSERT INTO SystemIMG (IMG) VALUES (?)";
        // Initialize the result ID to a default value that indicates failure
        int generatedId = -1;

        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            byte[] imageData = getImageData(image);
            pStmt.setBytes(1, imageData);
            pStmt.executeUpdate();
            // Retrieve the generated key for the inserted row
            try (ResultSet rs = pStmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // Get the generated key
                    generatedId = rs.getInt(1);
                }
            }
        } catch (SQLException | IOException ex) {
            throw new Exception("Failed to upload Image", ex);
        }
        return generatedId;
    }

    public void uploadSystemIMG(int IMGId, Image newImage) throws Exception {
        String sql = "UPDATE SystemIMG SET IMG = ? WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(sql)) {
            byte[] imageData = getImageData(newImage);
            pStmt.setBytes(1, imageData);
            pStmt.setInt(2, IMGId);
            pStmt.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new Exception("Failed to replace Image", ex);
        }
    }

    public void deleteSystemIMG(int IMGId) throws Exception {
        String sql = "DELETE FROM SystemIMG WHERE IMGID = ?";
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setInt(1, IMGId);
            pStmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Failed to delete Image", ex);

        }
    }

    public int getNextIDSystemIMG() throws Exception {
        String sql = "SELECT IDENT_CURRENT('SystemIMG') + 1";
        int nextId = 0;
        try (Connection conn = myDBConnector.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new Exception("Failed in getting next image", ex);
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
