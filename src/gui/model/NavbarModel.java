package gui.model;

import be.User;
import gui.controller.EMSController;
import gui.controller.EMSCoordinator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarModel {

    private UserModel userModel;
    private final DisplayErrorModel displayErrorModel;
    private final Image defaultProfile = new Image("Icons/User_Icon.png");
    private static volatile NavbarModel instance;
    private NavbarModel() {
        displayErrorModel = new DisplayErrorModel();
        try {
            userModel = UserModel.getInstance();

        } catch (Exception e) {
            displayErrorModel.displayErrorC("Failed to setup Navbar");
        }
    }

    public static NavbarModel getInstance() {
        if (instance == null) {
            synchronized (UserModel.class) { if (instance == null) { instance = new NavbarModel(); } }
        }
        return instance;
    }

    public void setupProfilePicture(ImageView profilePicture, StackPane profilePicturePane)   {
        Image profileImage = userModel.getLoggedInUser().getProfileIMG();
        if (profileImage != null) {
            setProfilePicture(profilePicture, profilePicturePane,profileImage);
            return;
        }
        profilePicturePane.getChildren().clear();
        profilePicturePane.getChildren().addAll(profilePicture);
        profilePicture.setImage(defaultProfile);
        profilePicture.setScaleX(1);
        profilePicture.setScaleY(1);
    }

    public void setProfilePicture(ImageView profilePicture, StackPane profilePicturePane, Image img)    {
        Circle clip = new Circle(profilePicture.getFitWidth() / 2, profilePicture.getFitHeight() / 2, profilePicture.getFitWidth() / 2);
        profilePicture.setClip(clip);

        // Create a circle for the border
        Circle borderCircle = new Circle(clip.getCenterX(), clip.getCenterY(), clip.getRadius() - 19); // Adjust the radius for the border
        borderCircle.getStyleClass().add("borderCircleIMG");
        profilePicturePane.getChildren().clear();
        profilePicturePane.getChildren().addAll(borderCircle, profilePicture);
        profilePicture.setImage(img);
        profilePicture.setScaleX(0.61);
        profilePicture.setScaleY(0.61);
    }

    @FXML
    public void openArchivedEvents(User currentUser, Stage currentStage) throws Exception {
        if (currentUser.getUserAccessLevel() == 1) { //Not admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinator.fxml"));
            Parent root = loader.load();
            currentStage.setTitle("Event Manager System Coordinator");
            EMSCoordinator controller = loader.getController();
            controller.openArchivedEvents();
            controller.setUserModel(userModel);
            controller.startupProgram();
            currentStage.setScene(new Scene(root));
        }
    }

    public void openOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSProfileSetting.fxml"));
            Parent root = loader.load();
            Stage EMSProfileSettings = new Stage();
            EMSProfileSettings.setTitle("Event Manager System Setting");
            EMSProfileSettings.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSProfileSettings.initModality(Modality.APPLICATION_MODAL);
            gui.controller.EMSProfileSettings controller = loader.getController();
            controller.setUserModel(userModel);
            EMSProfileSettings.setResizable(false);
            EMSProfileSettings.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSProfileSettings.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
            alert.showAndWait();
        }
    }

    @FXML
    public void logoutUser(Stage currentStage) throws IOException {
        userModel.logOutUser();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMS.fxml"));
        currentStage.setTitle("Event Manager System");
        Parent root = loader.load();
        EMSController controller = loader.getController();
        controller.setPrimaryStage(currentStage);
        controller.startupProgram();
        currentStage.setScene(new Scene(root));
    }
}
