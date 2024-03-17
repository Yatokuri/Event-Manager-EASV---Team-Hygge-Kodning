package gui.controller;

import be.Event;
import gui.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class EMSCoordinator {


    @FXML
    private Button btnCreateEvent;
    @FXML
    private Label lblLoggedInUser;
    @FXML
    private HBox eventHBoxSection;
    private static EMSCoordinator instance;
    private final DisplayErrorModel displayErrorModel;
    private EventModel eventModel;
    private TicketModel ticketModel;
    private UserModel userModel;
    private ArchivedEventModel archivedEventModel;
    private Event eventBeingUpdated;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public EMSCoordinator(){
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            eventModel = new EventModel();
            ticketModel = new TicketModel();
            archivedEventModel = new ArchivedEventModel();
        } catch (Exception e) {
            displayErrorModel.displayError(e);
        }

    }

    public Event getEventBeingUpdated(){
        return eventBeingUpdated;
    }

    public void startupProgram() { // This setup op the program
        lblLoggedInUser.setText(userModel.getLoggedInUser().getUserName());
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

    public void btnCreateEvent(javafx.event.ActionEvent actionEvent)   { // Handle when coordinators make new event
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinatorEventCreateUpdate.fxml"));
            Parent root = loader.load();
            Stage EMSCoordinatorEventCUStage = new Stage();
            EMSCoordinatorEventCUStage.setTitle("Event Manager System Create Event");
            EMSCoordinatorEventCUStage.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSCoordinatorEventCUStage.initModality(Modality.APPLICATION_MODAL);
            EMSCoordinatorEventCreator controller = loader.getController();
            controller.setType("Create");
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
