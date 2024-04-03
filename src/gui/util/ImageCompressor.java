package gui.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCompressor { // System to compress IMG

    public static Image compressImageTo500KB(Image fxImage) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);

            byte[] pngBytes = compressPNGWithResizing(bufferedImage);
            if (pngBytes.length <= 500 * 1024) {
                return new Image(new ByteArrayInputStream(pngBytes));
            }

        // If unable to compress below 500KB or unsupported format, return the original image
        return fxImage;
    }


    private static byte[] compressPNGWithResizing(BufferedImage image) throws IOException {
        double scale = 1.0; // Start with the original size
        byte[] compressedImage;
        ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();

        do {
            tempOutputStream.reset(); // Clear the stream for the next attempt
            BufferedImage resizedImage = resizeImage(image, scale);
            ImageIO.write(resizedImage, "png", tempOutputStream);
            compressedImage = tempOutputStream.toByteArray();

            scale -= 0.1; // Reduce size by 10% for the next iteration if necessary
        } while (compressedImage.length > 500 * 1024 && scale > 0.1);

        return compressedImage;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, double scale) {
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resizedImage;
    }
}