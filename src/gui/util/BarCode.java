package gui.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gui.model.DisplayErrorModel;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class BarCode {

    DisplayErrorModel displayErrorModel;

    public BarCode(){
        displayErrorModel = new DisplayErrorModel();
    }
    public static BufferedImage generateCode128BarcodeImage(UUID uuid, int width, int height) {
        String barcodeText = String.valueOf(uuid);

        Map<EncodeHintType, Object> hints;
        hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "");

        BitMatrix bitMatrix = new Code128Writer().encode(barcodeText, BarcodeFormat.CODE_128, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);

    }
    public static BufferedImage generateQRCodeImage(String data, int width, int height) throws Exception{
        String charset = "UTF-8";
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.MARGIN, 1); // Set margin to 1 module
        BitMatrix bitMatrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}
