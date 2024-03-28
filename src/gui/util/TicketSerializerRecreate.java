package gui.util;


import javafx.scene.control.Label;
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
            if (node instanceof Label) { // We make custom type to save space
                jsonObject.put("ty", "Lbl");
            } else if (node instanceof ImageView) {
                jsonObject.put("ty", "Img");
            }
            else {
                jsonObject.put("ty", node.getClass().getSimpleName());
            }
            jsonObject.put("lX", node.getLayoutX());
            jsonObject.put("lY", node.getLayoutY());

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
                // Round the fontSize value to two decimal places save space
                double fitWidth = Double.parseDouble(String.format(Locale.US, "%.2f", imageView.getFitWidth()));
                double fitHeight = Double.parseDouble(String.format(Locale.US, "%.2f", imageView.getFitHeight()));
                jsonObject.put("fW", fitWidth);
                jsonObject.put("fH", fitHeight);
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
                case "Lbl":
                    Label text = new Label();
                    text.setText(jsonObject.optString("t", ""));
                    Color color = Color.web(jsonObject.optString("c", "#000000")); // Default to black if not specified
                    text.setFont(Font.font(
                            jsonObject.optString("fF", "Arial"),
                            jsonObject.optString("fB", "f").equals("t") ? FontWeight.BOLD : FontWeight.NORMAL,
                            jsonObject.optString("fI", "f").equals("t") ? FontPosture.ITALIC : FontPosture.REGULAR,
                            jsonObject.optDouble("fS", 12)
                    ));
                    if (jsonObject.optString("fU", "f").equals("t")) {
                        text.setUnderline(true);
                    }
                    text.setTextFill(color);
                    text.setRotate(jsonObject.optDouble("r", 0));
                    node = text;
                    break;
                case "Img":
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(jsonObject.optDouble("fW", 100)); // Default width if not specified
                    imageView.setFitHeight(jsonObject.optDouble("fH", 100)); // Default height if not specified
                    imageView.setRotate(jsonObject.optDouble("rI", 0)); // Default rotation if not specified
                    imageView.setId(jsonObject.optString("id", "")); // The ID the image in database
                    node = imageView;
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
