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
import java.util.ArrayList;

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
    @FXML
    private Button eventButton1,eventButton2,eventButton3,eventButton4,eventButton5,eventButton6,eventButton7,eventButton8,eventButton9,eventButton10,eventButton11,eventButton12;


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
        hideButton();
    }

    public void hideButton()
    {

        ArrayList<Button> Buttons = new ArrayList<>();
        Buttons.add(eventButton1);
        Buttons.add(eventButton2);
        Buttons.add(eventButton3);
        Buttons.add(eventButton4);
        Buttons.add(eventButton5);
        Buttons.add(eventButton6);
        Buttons.add(eventButton7);
        Buttons.add(eventButton8);
        Buttons.add(eventButton9);
        Buttons.add(eventButton10);
        Buttons.add(eventButton11);
        Buttons.add(eventButton12);

        for(Button b : Buttons)
        {
            if (b.getText().isEmpty())
            {
                b.setOpacity(0);
            }
        }
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


    @FXML
    private void eventButton(javafx.event.ActionEvent actionEvent) {
    }
}
