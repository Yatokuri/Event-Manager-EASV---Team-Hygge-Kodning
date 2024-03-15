package gui.controller;

import be.Event;
import gui.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class EMSCoordinator {

    private static EMSCoordinator instance;
    private final DisplayErrorModel displayErrorModel;
    @FXML
    private Button btnCreateEvent;
    private EventModel eventModel;
    private TicketModel ticketModel;
    private UserModel userModel;
    private ArchivedEventModel archivedEventModel;
    private Event eventBeingUpdated;


    public EMSCoordinator(){
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            eventModel = new EventModel();
            ticketModel = new TicketModel();
            userModel = new UserModel();
            archivedEventModel = new ArchivedEventModel();
        } catch (Exception e) {
            displayErrorModel.displayError(e);
        }

    }

    public Event getEventBeingUpdated(){
        return eventBeingUpdated;
    }

    public void btnCreateEvent(javafx.event.ActionEvent actionEvent)   {
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

            // Set the scene in the existing stage
            EMSCoordinatorEventCUStage.setScene(new Scene(root));

            EMSCoordinatorEventCUStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
            alert.showAndWait();
        }
    }
}
