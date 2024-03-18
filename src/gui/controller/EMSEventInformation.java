package gui.controller;

import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EMSEventInformation implements Initializable {
    public Label eventNameLabel, eventStartTimeLabel, eventEndTimeLabel, eventLocationLabel;
    public TextArea eventNotesTextArea;
    public Button deleteButton, updateButton, backButton;
    public EMSCoordinator emsCoordinator;
    public DisplayErrorModel displayErrorModel;
    public EventModel eventModel;
    public be.Event eventBeingUpdated;


    public EMSEventInformation(){
        displayErrorModel = new DisplayErrorModel();
        try {
            eventModel = new EventModel();
            emsCoordinator = new EMSCoordinator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventBeingUpdated = emsCoordinator.getEventBeingUpdated();

        eventNameLabel.setText(eventBeingUpdated.getEventName());
        eventStartTimeLabel.setText(eventBeingUpdated.getEventStartDateTime());
        if (!eventBeingUpdated.getEventEndDateTime().isEmpty() || eventBeingUpdated.getEventEndDateTime() != null){
            eventEndTimeLabel.setText(eventBeingUpdated.getEventEndDateTime());
        }
        eventLocationLabel.setText(eventBeingUpdated.getLocation());
        eventNotesTextArea.setWrapText(true);
        eventNotesTextArea.setText(eventBeingUpdated.getEventNotes());
    }


    public void deleteButton() {
        try {
            eventModel.deleteEvent(eventBeingUpdated);
            backButton();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Unable to delete event");
        }
    }

    public void updateButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinatorEventCreateUpdate.fxml"));
            Parent root = loader.load();
            Stage EMSCoordinatorEventCUStage = new Stage();
            EMSCoordinatorEventCUStage.setTitle("Event Manager System Create Event");
            EMSCoordinatorEventCUStage.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSCoordinatorEventCUStage.initModality(Modality.APPLICATION_MODAL);
            EMSCoordinatorEventCreator controller = loader.getController();
            controller.setType("Update");
            controller.startupProgram();

            EMSCoordinatorEventCUStage.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSCoordinatorEventCUStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Update Window");
            alert.showAndWait();
        }
    }

    public void backButton() {
        Stage parent = (Stage) eventNameLabel.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
