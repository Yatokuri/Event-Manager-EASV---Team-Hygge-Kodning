package gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EMSController implements Initializable {

    @FXML
    private ImageView backgroundIMGLogin;
    @FXML
    private AnchorPane ancherPane;

    @FXML
    private HBox signInBox;

    private final Image backgroundIMG = new Image("/icons/LoginBackground2.png");

    private Stage primaryStage; // Store a reference to the stage


    public EMSController(Stage primaryStage) {
        this.primaryStage = primaryStage;

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the background image to fill the AnchorPane
        backgroundIMGLogin.setImage(backgroundIMG);

        // Bind the fitWidth and fitHeight properties of the background image to the width and height of the AnchorPane
        backgroundIMGLogin.fitWidthProperty().bind(ancherPane.widthProperty());
        backgroundIMGLogin.fitHeightProperty().bind(ancherPane.heightProperty());

        // Set AnchorPane's size to match the size of primaryStage
        ancherPane.prefWidthProperty().bind(primaryStage.widthProperty());
        ancherPane.prefHeightProperty().bind(primaryStage.heightProperty());
        BoxBlur boxblur = new BoxBlur(signInBox.getWidth(), signInBox.getHeight(), 3);
        GaussianBlur gaussianBlur = new GaussianBlur();
        //gaussianBlur.setRadius(10.5);

        //signInBox.setEffect(gaussianBlur);
        signInBox.setEffect(boxblur);

    }

}
