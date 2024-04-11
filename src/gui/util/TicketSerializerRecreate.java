package gui.util;


import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class TicketSerializerRecreate {
    //We use abbreviation in the JSON, so it take less space
    public static String serializeTicketAreaToJson(Pane ticketArea) {
        JSONArray jsonArray = new JSONArray();
        for (javafx.scene.Node node : ticketArea.getChildren()) {
            JSONObject jsonObject = new JSONObject();
            if (node instanceof Label lbl) { // We make custom type to save space
                Boolean isEventName = (Boolean) lbl.getProperties().get("isEventName");
                Boolean isEventStartDateTime = (Boolean) lbl.getProperties().get("isEventStartDateTime");
                Boolean isEventEndDateTime = (Boolean) lbl.getProperties().get("isEventEndDateTime");
                Boolean isEventNotes = (Boolean) lbl.getProperties().get("isEventNotes");
                Boolean isEventLocationGuide = (Boolean) lbl.getProperties().get("isEventLocationGuide");
                Boolean isEventLocation = (Boolean) lbl.getProperties().get("isEventLocation");

                if (Boolean.TRUE.equals(isEventName)) {
                    jsonObject.put("ty", "ET");
                }
                else if (Boolean.TRUE.equals(isEventStartDateTime)) {
                    jsonObject.put("ty", "ES");
                }
                else if (Boolean.TRUE.equals(isEventEndDateTime)) {
                    jsonObject.put("ty", "EE");
                }
                else if (Boolean.TRUE.equals(isEventNotes)) {
                    jsonObject.put("ty", "EN");
                }
                else if (Boolean.TRUE.equals(isEventLocationGuide)) {
                    jsonObject.put("ty", "EG");
                }
                else if (Boolean.TRUE.equals(isEventLocation)) {
                    jsonObject.put("ty", "EL");
                }
                else {
                    jsonObject.put("ty", "Lbl");
                }
            } else if (node instanceof ImageView imageView) {
                Image image = imageView.getImage();
                // Assume `imageView` is the ImageView instance you're checking
                Boolean isQRCode = (Boolean) imageView.getProperties().get("isQRCode");
                Boolean isBarcode = (Boolean) imageView.getProperties().get("isBarcode");

                if (Boolean.TRUE.equals(isQRCode)) {
                    jsonObject.put("ty", "QR");
                } else if (Boolean.TRUE.equals(isBarcode)) {
                    jsonObject.put("ty", "BC");
                }
                else {
                    jsonObject.put("ty", "Img");
                }
            } else {
                jsonObject.put("ty", node.getClass().getSimpleName());
            }
            jsonObject.put("lX", String.format(Locale.US,  "%.2f", Math.floor(node.getLayoutX())));
            jsonObject.put("lY", String.format(Locale.US, "%.2f", Math.floor(node.getLayoutY())));

            // Handle Text nodes
            if (node instanceof Label labelNode) {
                jsonObject.put("t", labelNode.getText());
                jsonObject.put("fF", labelNode.getFont().getFamily());
                // Round the fontSize value to two decimal places save space
                double roundedFontSize = Math.round(labelNode.getFont().getSize() * 100.0) / 100.0;
                jsonObject.put("fS", roundedFontSize);
                jsonObject.put("fB", labelNode.getFont().getStyle().contains("Bold") ? "t" : "f");
                jsonObject.put("fI", labelNode.getFont().getStyle().contains("Italic") ? "t" : "f");
                jsonObject.put("fU", labelNode.isUnderline() ? "t" : "f");
                jsonObject.put("r", labelNode.getRotate());
                // Define your ColorPicker control
                // Serialize color as hex string
                Color color = (Color) labelNode.getTextFill();
                String colorHex = String.format("#%02X%02X%02X",
                        (int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255));
                jsonObject.put("c", colorHex);
            }
            else if (node instanceof ImageView imageView) {
                double originalWidth = Math.round(imageView.getImage().getWidth() * 100.0) / 100.0;
                double originalHeight = Math.round(imageView.getImage().getHeight() * 100.0) / 100.0;
                double scaleX = imageView.getFitWidth() / originalWidth;
                double scaleY = imageView.getFitHeight() / originalHeight;
                double scale = Math.min(scaleX, scaleY);
                scale = Math.round(scale * 100.0) / 100.0; // Round scale to two decimal places
                double formattedWidth = Math.round(originalWidth * 100.0) / 100.0;
                double formattedHeight = Math.round(originalHeight * 100.0) / 100.0;
                jsonObject.put("sc", scale);
                jsonObject.put("oW", formattedWidth);
                jsonObject.put("oH", formattedHeight);
                jsonObject.put("rI", imageView.getRotate());
                jsonObject.put("id", imageView.getId()); // The ID the image in database
            }
            // Add the JSONObject to the JSONArray
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    public static Pane cloneTicketAreaFromJson(String json) {
        Pane ticketAreaClone = new Pane();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String type = jsonObject.getString("ty");
            double layoutX = jsonObject.getDouble("lX");
            double layoutY = jsonObject.getDouble("lY");

            javafx.scene.Node node;
            switch (type) {

                case "Lbl", "ET", "ES", "EE", "EN", "EG", "EL":
                    Label text = new Label();
                    text.setText(jsonObject.optString("t", ""));
                    Color color = Color.web(jsonObject.optString("c", "#000000")); // Default to black if not specified
                    text.setFont(Font.font(
                            jsonObject.optString("fF", "System"),
                            jsonObject.optString("fB", "f").equals("t") ? FontWeight.BOLD : FontWeight.NORMAL,
                            jsonObject.optString("fI", "f").equals("t") ? FontPosture.ITALIC : FontPosture.REGULAR,
                            jsonObject.optDouble("fS", 12)
                    ));
                    if (jsonObject.optString("fU", "f").equals("t")) {
                        text.setUnderline(true);
                    }

                    if (type.equals("ET"))   {
                        text.getProperties().put("isEventName", true);
                    }
                    if (type.equals("ES"))   {
                        text.getProperties().put("isEventStartDateTime", true);
                    }
                    if (type.equals("EE"))   {
                        text.getProperties().put("isEventEndDateTime", true);
                    }
                    if (type.equals("EN"))   {
                        text.getProperties().put("isEventNotes", true);
                    }
                    if (type.equals("EG"))   {
                        text.getProperties().put("isEventLocationGuide", true);
                    }
                    if (type.equals("EL"))   {
                        text.getProperties().put("isEventLocation", true);
                    }

                    text.setTextFill(color);
                    text.setRotate(jsonObject.optDouble("r", 0));
                    text.setWrapText(true);
                    text.setMaxWidth(250); //TODO More Dynamic
                    node = text;
                    if (text.getText().startsWith("%f%")){ // To insert ticket buyer name direct
                        text.getProperties().put("isAFName", true);
                    }
                    if (text.getText().startsWith("%l%")){ // To insert ticket buyer last name direct
                        text.getProperties().put("isALName", true);
                    }
                    if (text.getText().startsWith("%fl%")){ // To insert ticket buyer full name direct
                        text.getProperties().put("isAFLName", true);
                    }
                    break;
                case "Img", "BC", "QR":
                    ImageView imageView = new ImageView();
                    double fitWidth = jsonObject.optDouble("fW", 100); // Default width if not specified
                    double fitHeight = jsonObject.optDouble("fH", 100); // Default height if not specified
                    double originalWidth = jsonObject.optDouble("oW", 100);
                    double originalHeight = jsonObject.optDouble("oH",100);
                    double scale = jsonObject.getDouble("sc");
                    imageView.setFitWidth(fitWidth);
                    imageView.setFitHeight(fitHeight);
                    imageView.setFitWidth(originalWidth * scale);
                    imageView.setFitHeight(originalHeight * scale);
                    imageView.setRotate(jsonObject.optDouble("rI", 0)); // Default rotation if not specified
                    imageView.setId(jsonObject.optString("id", "")); // The ID of the image in the database
                    node = imageView;
                if (type.equals("QR"))   {
                    imageView.getProperties().put("isQRCode", true);
                }
                if (type.equals("BC")){
                    imageView.getProperties().put("isBarcode", true);
                }
                    break;
                default:
                    node = null;
            }
            if (node != null) {
                node.setLayoutX(layoutX);
                node.setLayoutY(layoutY);
                ticketAreaClone.getChildren().add(node);
            }
        }
        return ticketAreaClone;
    }
}
