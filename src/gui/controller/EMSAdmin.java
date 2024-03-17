package gui.controller;

import gui.model.UserModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class EMSAdmin {

    public HBox eventHBoxSection;
    private UserModel userModel;
    @FXML
    private Button btnCRUDCoordinators;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public void startupProgram() { // This setup op the program
        eventList(); // Setup dynamic event
    }


    public void eventList() // Here we create the event dynamic
    {
        HBox eventHBox = new HBox();
        eventHBox.setStyle("-fx-background-color: grey");
        eventHBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(eventHBox, Priority.ALWAYS);

        eventHBoxSection.getChildren().add(eventHBox);
    }

    public void btnCRUDCoordinators(javafx.event.ActionEvent actionEvent)   { // Handle when admin make new coordinators
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSAdminCoordinatorManager.fxml"));
            Parent root = loader.load();
            Stage EMSCoordinatorEventCUStage = new Stage();
            EMSCoordinatorEventCUStage.setTitle("Event Manager System Coordinator Manager");
            EMSCoordinatorEventCUStage.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSCoordinatorEventCUStage.initModality(Modality.APPLICATION_MODAL);
            EMSAdminCoordinatorManager controller = loader.getController();
            controller.setUserModel(userModel);
            controller.startupProgram();

            EMSCoordinatorEventCUStage.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSCoordinatorEventCUStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
            alert.showAndWait();
        }
    }

}
