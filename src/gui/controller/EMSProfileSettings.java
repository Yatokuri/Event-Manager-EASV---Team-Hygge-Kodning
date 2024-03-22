package gui.controller;

import be.User;
import gui.model.DisplayErrorModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class EMSProfileSettings implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView settingSectionProfileIMG;
    @FXML
    private Button onChangePicture, onDeletePicture, backButton, btnConfirmNewProfileIMG, btnUploadNewProfileIMG, btnConfirmNewUser, btnCancelNewUser;
    @FXML
    private VBox settingSectionLeft, settingSectionNewIMGLeft, animatedNewPasswordBox;
    @FXML
    private HBox mainSection;
    @FXML
    private Pane imagePane;
    @FXML
    private TextField txtUsername;
    @FXML
    private MFXPasswordField txtInputOldPassword, txtInputNewPassword,  txtInputNewPasswordConfirm;


    public EMSCoordinator emsCoordinator;
    public EMSAdmin emsAdmin;
    public DisplayErrorModel displayErrorModel;
    public UserModel userModel;

    private Image currentProfilePicture, originalProfilePicture;
    private User currentUser;
    public FileChooser fileChooserDialog;



    public EMSProfileSettings(){
        displayErrorModel = new DisplayErrorModel();
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    private Circle pictureCapture;
    private final Image defaultProfileIMG = new Image("Icons/User_Icon.png");
    public void setEMSAdmin(EMSAdmin emsAdmin) {
        this.emsAdmin = emsAdmin;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void startupProgram() { // This setup program
        currentUser = userModel.getLoggedInUser();
        txtUsername.setText(currentUser.getUserName());
        settingSectionNewIMGLeft.setManaged(false);
        settingSectionProfileIMG.setImage(defaultProfileIMG);
        setProfilePicture(defaultProfileIMG);
        currentProfilePicture = defaultProfileIMG;
        originalProfilePicture = defaultProfileIMG;

        if (currentUser.getProfileIMG() != null)   { //If user have a picture set it
            currentProfilePicture = currentUser.getProfileIMG();
            originalProfilePicture = currentProfilePicture;
            settingSectionProfileIMG.setImage(currentProfilePicture);
            setProfilePicture(currentProfilePicture);
        }
    }


    public void setProfilePicture(Image img) { // This setup the Image
        settingSectionProfileIMG.setFitWidth(250);
        settingSectionProfileIMG.setFitHeight(250);
        settingSectionProfileIMG.setPreserveRatio(false);
        settingSectionProfileIMG.setImage(img);

        if (img != defaultProfileIMG) { //  Default picture dont need border
            Circle clip = new Circle(settingSectionProfileIMG.getFitWidth() / 2, settingSectionProfileIMG.getFitHeight() / 2,
                    settingSectionProfileIMG.getFitWidth() / 2);
            settingSectionProfileIMG.setClip(clip); // Create the circular clip so we get a round picture

            Circle borderCircle = new Circle(clip.getCenterX(), clip.getCenterY(), clip.getRadius() + 1);
            borderCircle.getStyleClass().add("borderCircleIMG");  // Create a circle, so we can make a border around the picture

            // Add the border circle behind the clip
            imagePane.getChildren().clear();
            imagePane.getChildren().addAll(borderCircle, settingSectionProfileIMG);
        }
    }


    public void onChangePicture() { //Here we open Image changing window, so we close a lot and something new
        if ("Reset picture".equals(onChangePicture.getText())) {
            setProfilePicture(originalProfilePicture != null ? originalProfilePicture : currentProfilePicture);
        } else {
            settingSectionLeft.setVisible(false);
            settingSectionLeft.setManaged(false);
            onDeletePicture.setVisible(true);
            settingSectionNewIMGLeft.setManaged(true);
            settingSectionNewIMGLeft.setVisible(true);
            btnConfirmNewProfileIMG.setVisible(true);
            btnUploadNewProfileIMG.setVisible(true);
            onChangePicture.setText("Reset picture");
        }
        setupResizableAndDraggableSystem(); // Assuming it's needed here regardless
    }




    public void onDeletePicture() {
        try {
            currentUser.setProfileIMG(null);
            userModel.deleteUserProfileIMG(currentUser);
            currentProfilePicture = defaultProfileIMG;
            setProfilePicture(defaultProfileIMG);


        } catch (Exception e) {
            displayErrorModel.displayErrorC("Profile picture could be be deleted");
        }
    }

    public void setupResizableAndDraggableSystem()  {
        // Create a circle with 50px radius. Adjust the center to be over the image.
        pictureCapture = new Circle(50);
        // Assuming the image is centered in the Pane, adjust these values as needed.
        pictureCapture.setCenterX(settingSectionProfileIMG.getFitWidth() / 2);
        pictureCapture.setCenterY(settingSectionProfileIMG.getFitHeight() / 2);
        pictureCapture.getStyleClass().add("pictureCaptureIMG");  // Create a circle, so we can make a border around the picture
        pictureCapture.setFill(Color.TRANSPARENT);
        pictureCapture.setStroke(Color.ORANGERED);
        pictureCapture.setStrokeWidth(5);
        // Clear any existing clips or circles from previous operations.
        imagePane.getChildren().removeIf(node -> node instanceof Circle);

        // Add the circle to the pane to make it appear over the image.
        imagePane.getChildren().add(pictureCapture);

        // Make the circle draggable
        makeResizableAndDraggable(pictureCapture, settingSectionProfileIMG);
    }


    public void btnUploadNewProfileIMG() {
        fileChooserDialog = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooserDialog.getExtensionFilters().add(extFilter);

        // Set the owner window for the file chooser dialog
        Stage ownerStage = (Stage) settingSectionLeft.getScene().getWindow();

        // Show the file chooser dialog
        File selectedFile = fileChooserDialog.showOpenDialog(ownerStage);

        if (selectedFile != null) {
            settingSectionProfileIMG.setClip(null);
            currentProfilePicture = new Image(selectedFile.toURI().toString());

            double fitWidth = 400; // Your specified fit width
            double fitHeight = 250; // Your specified fit height

            // Calculate the scaled width and height while preserving the aspect ratio
            double scaledWidth = currentProfilePicture.getWidth();
            double scaledHeight = currentProfilePicture.getHeight();

            if (scaledWidth > fitWidth) {
                scaledHeight *= fitWidth / scaledWidth;
                scaledWidth = fitWidth;
            }

            if (scaledHeight > fitHeight) {
                scaledWidth *= fitHeight / scaledHeight;
                scaledHeight = fitHeight;
            }

            // Set the scaled width and height to the image view
            settingSectionProfileIMG.setFitWidth(scaledWidth);
            settingSectionProfileIMG.setFitHeight(scaledHeight);

            // Set the image and reset circle
            settingSectionProfileIMG.setImage(currentProfilePicture);
            setupResizableAndDraggableSystem();
        }
    }



    public void btnConfirmNewProfileIMG() { // Here we capture the picture inside the circle
        if (currentProfilePicture  == defaultProfileIMG)    { // If you confirm with default picture
            pictureCapture.setVisible(false);
            backButton();
            return;
        }

        double radius = pictureCapture.getRadius();
        double centerX = pictureCapture.getCenterX();
        double centerY = pictureCapture.getCenterY();

        // Calculate the bounds for the snapshot
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT); // Ensure the background is transparent

        // Define the viewport to capture the area within the circle
        Rectangle2D viewport = new Rectangle2D(centerX - radius, centerY - radius, radius * 2, radius * 2);
        parameters.setViewport(viewport);

        // Take a snapshot of the ImageView within the defined viewport
        Image capturedImage = settingSectionProfileIMG.snapshot(parameters, null);

        // Set the capturedImage image as the new profile image
        settingSectionProfileIMG.setImage(capturedImage);
        currentProfilePicture = capturedImage;
        originalProfilePicture = capturedImage;
        //Here we update the database depends on they already have an IMG or new
        if (currentUser.getProfileIMG() != null)    {
            try {
                currentUser.setProfileIMG(currentProfilePicture);
                userModel.updateUserProfileIMG(currentUser);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Profile picture could not be updated");
            }
        }
        else {
            try {
                currentUser.setProfileIMG(currentProfilePicture);
                userModel.createUserProfileIMG(currentUser);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Profile picture could not be created");
            }
        }
        setProfilePicture(capturedImage);
        pictureCapture.setVisible(false);
        backButton();
    }



    private void makeResizableAndDraggable(Circle circle, ImageView imageView) { // Source Stackoverflow
        final double[] initialMousePos = new double[2];
        final double[] initialCirclePos = new double[2];
        final double[] initialCircleRadius = new double[1];
        final boolean[] isResizing = {false};

        circle.setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) { // Check if right mouse button is pressed
                isResizing[0] = true;
                initialMousePos[0] = e.getSceneX();
                initialMousePos[1] = e.getSceneY();
                initialCircleRadius[0] = circle.getRadius();
            } else {
                initialMousePos[0] = e.getSceneX();
                initialMousePos[1] = e.getSceneY();
                initialCirclePos[0] = circle.getCenterX();
                initialCirclePos[1] = circle.getCenterY();
            }
        });

        circle.setOnMouseDragged(e -> {
            if (isResizing[0]) {
                double deltaX = e.getSceneX() - initialMousePos[0];
                double deltaY = e.getSceneY() - initialMousePos[1];

                double newRadius = initialCircleRadius[0] + (deltaX + deltaY) / 2;
                double radius = Math.min(newRadius, Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);

                circle.setRadius(radius);
            } else {
                double deltaX = e.getSceneX() - initialMousePos[0];
                double deltaY = e.getSceneY() - initialMousePos[1];

                double newX = initialCirclePos[0] + deltaX;
                double newY = initialCirclePos[1] + deltaY;

                // Constrain the circle within the bounds of the imageView
                double radius = circle.getRadius();
                double maxX = imageView.getLayoutX() + imageView.getFitWidth() - radius;
                double maxY = imageView.getLayoutY() + imageView.getFitHeight() - radius;
                double minX = imageView.getLayoutX() + radius;
                double minY = imageView.getLayoutY() + radius;

                // Apply constraints
                newX = Math.min(Math.max(newX, minX), maxX);
                newY = Math.min(Math.max(newY, minY), maxY);

                // Update circle position
                circle.setCenterX(newX);
                circle.setCenterY(newY);
            }
        });

        circle.setOnMouseReleased(e -> {
            isResizing[0] = false;
        });
    }

    public void btnConfirmNewUser() { // This method is used to change user password.
        String oldPasswordInput = txtInputOldPassword.getText();
        String newPasswordInput = txtInputNewPassword.getText();
        String newConfirmPasswordInput = txtInputNewPasswordConfirm.getText();

        if (oldPasswordInput.isEmpty() || newPasswordInput.isEmpty() || newConfirmPasswordInput.isEmpty()) {
            displayErrorModel.displayErrorC("Not all field filled");
            return;
        }

        if (!Objects.equals(newPasswordInput, newConfirmPasswordInput)) {
            displayErrorModel.displayErrorC("Not identical passwords");
            return;
        }

        try { //Check old password is correct
            if (userModel.signIn(userModel.getLoggedInUser().getUserName(), oldPasswordInput) == null) {
                displayErrorModel.displayErrorC("Wrong password");
                return;
            }

        } catch (Exception ignored) {
        }

        try {
            User updatedUser = userModel.getLoggedInUser();
            updatedUser.setPassword(newPasswordInput);
            userModel.updateUser(updatedUser);
            disableResetPassword();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Password could not be updated");
        }
    }

    public void btnCancelNewUser() {
        disableResetPassword();
    }


    public void enableResetPassword() { // Here the reset password get showed up with animation
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.80), animatedNewPasswordBox);
        transition.setByX(-(imagePane.getWidth()+125));
        transition.play();
        animatedNewPasswordBox.setVisible(true);
        animatedNewPasswordBox.setLayoutX(mainSection.getWidth()+0);
        animatedNewPasswordBox.setLayoutY(mainSection.getHeight()-(imagePane.getHeight()/2));
    }

    public void disableResetPassword() { // Here the reset password disappear up with animation
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.50), animatedNewPasswordBox);
        transition.setByX(imagePane.getWidth()+125);
        transition.setOnFinished(event -> {
            txtInputNewPassword.clear();
            txtInputNewPasswordConfirm.clear();
            txtInputOldPassword.clear();
            animatedNewPasswordBox.setVisible(false);
            animatedNewPasswordBox.setLayoutX(mainSection.getWidth()+0);
            animatedNewPasswordBox.setLayoutY(mainSection.getHeight()-(imagePane.getHeight()/2));
        });
        transition.play();
    }

    public void onNewPassword() {
        if (!animatedNewPasswordBox.isVisible()) {
            enableResetPassword();
            return;
        }
        disableResetPassword();
    }

    public void backButton() { // When user press back
        if (!settingSectionLeft.isVisible())    { //If user is in profile picture mode go back to setting waiting
            settingSectionLeft.setVisible(true);
            onDeletePicture.setVisible(false);
            btnConfirmNewProfileIMG.setVisible(false);
            btnUploadNewProfileIMG.setVisible(false);
            settingSectionLeft.setManaged(true);
            settingSectionNewIMGLeft.setVisible(false);
            settingSectionNewIMGLeft.setManaged(false);
            pictureCapture.setVisible(false);
            onChangePicture.setText("Change picture");
            setProfilePicture(currentProfilePicture);
            return;
        } // If not go out of setting window

        if (emsAdmin != null)   {
            emsAdmin.setupProfilePicture();
        }
        else if (emsCoordinator != null) {
            emsCoordinator.setupProfilePicture();
        }

        Stage parent = (Stage) settingSectionLeft.getScene().getWindow(); // Close window
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
