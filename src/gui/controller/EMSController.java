package gui.controller;

import be.User;
import gui.model.DisplayErrorModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXButton;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    @FXML
    private MFXTextField txtInputUsername;
    @FXML
    private MFXPasswordField txtInputPassword;
    @FXML
    private ImageView backgroundIMGLogin;
    @FXML
    private AnchorPane ancherPane;
    @FXML
    private HBox signInBox;
    @FXML
    private VBox signInBoxStuff;
    @FXML
    private MFXButton btnLogin;

    private Node[] focusNodes; // To make tab work correct
    private int currentFocusIndex;
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

        // Initialize the array of focusable nodes
        focusNodes = new Node[]{txtInputUsername, txtInputPassword, btnLogin};
        currentFocusIndex = 0;

        // Add event filter to handle Tab key press
        for (Node node : focusNodes) {
            node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabKeyPress);
        }
    }

    private void handleTabKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            event.consume();
            currentFocusIndex = (currentFocusIndex + 1) % focusNodes.length;
            focusNodes[currentFocusIndex].requestFocus();
        }
    }

    public void startupProgram() {
        // Set AnchorPane's size to match the size of primaryStage
        ancherPane.prefWidthProperty().bind(primaryStage.widthProperty());
        ancherPane.prefHeightProperty().bind(primaryStage.heightProperty());


        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            double value = ((primaryStage.getWidth() - loginBoxWidth) / 2);
            signInBox.setLayoutX(value);
            signInBoxStuff.setLayoutX(value);
        });

        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            double value = ((primaryStage.getHeight() - loginBoxHeight) / 2);
            signInBox.setLayoutY(value);
            signInBoxStuff.setLayoutY(value);
        });

        //Manipulate it to fix the login box position
        primaryStage.setWidth(primaryStage.getWidth() + 1);
        primaryStage.setHeight(primaryStage.getHeight() + 1);
        primaryStage.setWidth(primaryStage.getWidth() - 1);
        primaryStage.setHeight(primaryStage.getHeight() - 1);
    }

    private User currentUser;


    public void btnLogin(ActionEvent actionEvent) {
        String username = txtInputUsername.getText();
        String password = txtInputPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            displayErrorModel.displayErrorC("Wrong username or password");
            return;
        }

        try {
            currentUser = userModel.signIn(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorModel.displayErrorC("Wrong username or password");
            return;
        }

        if (currentUser == null) {
            displayErrorModel.displayErrorC("Wrong username or password");
            return;
        }

        userModel.setLoggedInUser(currentUser);

        if (currentUser.getUserAccessLevel() == 1) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinator.fxml"));
                Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                currentStage.setTitle("Event Manager System Coordinator");
                Parent root = loader.load();
                EMSCoordinator controller = loader.getController();
                controller.setUserModel(userModel);
                controller.startupProgram();
                currentStage.setScene(new Scene(root)); // Set the scene in the existing stage
            } catch (IOException e) {
                e.printStackTrace();
                displayErrorModel.displayErrorC("Try to restart the program");
            }
        }


        else if (currentUser.getUserAccessLevel() == 2) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSAdmin.fxml"));
                Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                currentStage.setTitle("Event Manager System Admin");
                Parent root = loader.load();
                EMSAdmin controller = loader.getController();
                controller.setUserModel(userModel);
                controller.startupProgram();
                currentStage.setScene(new Scene(root)); // Set the scene in the existing stage
            } catch (IOException e) {
                e.printStackTrace();
                displayErrorModel.displayErrorC("Try to restart the program");
            }
        }

        else {
            displayErrorModel.displayErrorC("Wrong username or password");
        }
    }

}

