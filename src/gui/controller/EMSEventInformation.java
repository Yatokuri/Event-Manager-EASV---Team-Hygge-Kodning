package gui.controller;

import gui.model.ArchivedEventModel;
import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import gui.model.EventTicketsModel;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class EMSEventInformation implements Initializable {
    public Label eventNameLabel, eventStartTimeLabel, eventEndTimeLabel, eventLocationLabel;
    public TextArea eventNotesTextArea;
    public Button deleteButton, updateButton, backButton, ticketButton;
    public EMSCoordinator emsCoordinator;
    public EMSAdmin emsAdmin;
    public DisplayErrorModel displayErrorModel;
    public EventModel eventModel;
    public ArchivedEventModel archivedEventModel;
    private EventTicketsModel eventTicketsModel;
    public be.Event eventBeingUpdated;
    public Scene emsCoordinatorScene;
    private boolean isItArchivedEvent = false;
    public EMSEventInformation(){
        displayErrorModel = new DisplayErrorModel();
    }

    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) { this.emsCoordinator = emsCoordinator; }
    public void setEMSCoordinatorScene(Scene emsCoordinatorScene) { this.emsCoordinatorScene = emsCoordinatorScene; }
    public void setIsItArchivedEvent(boolean isItArchivedEvent) {this.isItArchivedEvent = isItArchivedEvent; }
    public void setEMSAdmin(EMSAdmin emsAdmin) { this.emsAdmin = emsAdmin; }
    private final Image mainIcon = new Image ("/Icons/mainIcon.png");
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void startupProgram() throws Exception { // This setup program
        archivedEventModel = ArchivedEventModel.getInstance();
        eventTicketsModel = EventTicketsModel.getInstance();
        if (emsCoordinator != null) {
            eventBeingUpdated = emsCoordinator.getEventBeingUpdated();
        }
        if (emsAdmin != null)   {
            eventBeingUpdated = emsAdmin.getEventBeingUpdated();
        } //Admin cannot update, so we remove the button or if is archivedEvent
        if (isItArchivedEvent || emsAdmin != null)  {
            updateButton.setManaged(false);
            updateButton.setVisible(false);
            ticketButton.setManaged(false);
            ticketButton.setVisible(false);
        }
        setupEventInformation();
    }
    public void setupEventInformation() {
        eventNameLabel.setText(eventBeingUpdated.getEventName());
        eventStartTimeLabel.setText(eventBeingUpdated.getEventStartDateTime());
        if (eventBeingUpdated.getEventEndDateTime() != null && !eventBeingUpdated.getEventEndDateTime().isEmpty() && !Objects.equals(eventBeingUpdated.getEventEndDateTime(), eventBeingUpdated.getEventStartDateTime())){
            eventEndTimeLabel.setText(eventBeingUpdated.getEventEndDateTime());
        }
        else
            eventEndTimeLabel.setText(null);
        eventLocationLabel.setText(eventBeingUpdated.getLocation());
        eventNotesTextArea.setWrapText(true);
        eventNotesTextArea.setText(eventBeingUpdated.getEventNotes());
    }

    public void ticketButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSTicketMain.fxml"));
            Parent root = loader.load();
            Stage EMSTicketMain = new Stage();
            EMSTicketMain.setTitle("Ticket Manager System");
            EMSTicketMain.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSTicketMain.setMaximized(false);
            EMSTicketMain controller = loader.getController();
            controller.setEMSCoordinator(emsCoordinator);
            controller.startupProgram();
            EMSTicketMain.setMaximized(true);
            EMSTicketMain.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSTicketMain.show();
            // Close emsCoordinator and event information windows
            Stage emsCoordinatorStage = (Stage) emsCoordinatorScene.getWindow();
            emsCoordinatorStage.close();
            Stage eventInformationStage = (Stage) eventNameLabel.getScene().getWindow();
            eventInformationStage.close();
            controller.setEMSCoordinatorStage(emsCoordinatorStage); // Pass the emsCoordinator stage
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Ticket Window");
            alert.showAndWait();
        }
    }

    public void deleteButton() {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Delete Event");
            alert.setContentText("You are about to delete event " + eventBeingUpdated.getEventName() + ". \nThis action cannot be undone.\n\nAre you sure you want to proceed?");
            // Set the icon for the dialog window
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(mainIcon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (isItArchivedEvent)  {
                        archivedEventModel.deleteEvent(eventBeingUpdated);
                    }
                    else {
                        archivedEventModel.archiveEvent(eventBeingUpdated);
                        eventTicketsModel.deleteAllTicketsFromEvent(eventBeingUpdated);
                        eventModel.deleteEvent(eventBeingUpdated);
                    }
                    emsCoordinator.startupProgram(); // Refresh UI
                } catch (Exception e) {
                    displayErrorModel.displayErrorC("Unable to delete event");
                }
                backButton();
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
            EMSCoordinatorEventCUStage.setResizable(false);
            controller.setType("Update");
            controller.setEventModel(eventModel);
            controller.setArchivedEventModel(archivedEventModel);
            controller.setEMSCoordinator(emsCoordinator);
            controller.startupProgram();
            // Set event handler for window hiding event, so we can update the event
            EMSCoordinatorEventCUStage.setOnHiding(event -> setupEventInformation());
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
