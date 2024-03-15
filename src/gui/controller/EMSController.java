package gui.controller;

import be.User;
import gui.model.DisplayErrorModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EMSController implements Initializable {

    public MFXTextField txtInputUsername;
    public MFXPasswordField txtInputPassword;
    @FXML
    private ImageView backgroundIMGLogin;
    @FXML
    private AnchorPane ancherPane;
    @FXML
    private HBox signInBox;
    @FXML
    private VBox signInBoxStuff;

    private final Image backgroundIMG = new Image("/icons/LoginBackground2.png");

    private Stage primaryStage; // Store a reference to the stage

    private final DisplayErrorModel displayErrorModel;
    private UserModel userModel;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public EMSController() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            displayErrorModel.displayError(e);
        }
    }

    private final int loginBoxWidth = 500;
    private final int loginBoxHeight = 400;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the background image to fill the AnchorPane
        backgroundIMGLogin.setImage(backgroundIMG);

        // Bind the fitWidth and fitHeight properties of the background image to the width and height of the AnchorPane
        backgroundIMGLogin.fitWidthProperty().bind(ancherPane.widthProperty());
        backgroundIMGLogin.fitHeightProperty().bind(ancherPane.heightProperty());

        BoxBlur boxblur = new BoxBlur(signInBox.getWidth(), signInBox.getHeight(), 3);
        signInBox.setEffect(boxblur);
    }

    public void startupProgram() {
        primaryStage.setWidth(primaryStage.getWidth());

        // Set AnchorPane's size to match the size of primaryStage
        ancherPane.prefWidthProperty().bind(primaryStage.widthProperty());
        ancherPane.prefHeightProperty().bind(primaryStage.heightProperty());


        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            double value = ((primaryStage.getWidth()-loginBoxWidth)/2);
            signInBox.setLayoutX(value);
            signInBoxStuff.setLayoutX(value);
        });

        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            double value = ((primaryStage.getHeight()-loginBoxHeight)/2);
            signInBox.setLayoutY(value);
            signInBoxStuff.setLayoutY(value);
        });
    }

    private User currentUser;

    public void btnLogin(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinator.fxml"));
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setTitle("Event Manager System Admin");

            EMSAdmin controller = loader.getController();
          //  controller.setUserModel(userModel);
            //controller.startupProgram();

            Parent root = loader.load();
            // Set the scene in the existing stage
            currentStage.setScene(new Scene(root));
            currentStage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
            alert.showAndWait();
        }
    }


  /* public void btnLogin(ActionEvent actionEvent)  {
        String username = txtInputUsername.getText();
        String password = txtInputPassword.getText();


       if (username.isEmpty() || password.isEmpty()){
           displayErrorModel.displayErrorC("Wrong username or password");
           return;
       }

       try {
       currentUser = userModel.signIn(username, password);
       } catch (Exception e) {
           e.printStackTrace();
           Alert alert = new Alert(Alert.AlertType.ERROR, "Problems");
           alert.showAndWait();
       }

       System.out.println("eaeae");

        if (currentUser.getUserAccessLevel() == 2){

            userModel.setLoggedInUser(currentUser);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSAdmin.fxml"));
                Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                currentStage.setTitle("Event Manager System Admin");
Parent root = loader.load();
                EMSAdmin controller = loader.getController();
                controller.setUserModel(userModel);
                controller.startupProgram();

                Parent root = loader.load();
                // Set the scene in the existing stage
                currentStage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
                alert.showAndWait();
            }
        }
        else {
           displayErrorModel.displayErrorC("Wrong username or password");
       }
    } */
}