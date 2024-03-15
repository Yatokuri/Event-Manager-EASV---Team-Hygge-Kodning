package gui.controller;

import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class EMSCoordinatorEventCreator implements Initializable {

    private EMSCoordinator emsCoordinator;

    private EventModel eventModel;

    private DisplayErrorModel displayErrorModel;

    private static be.Event eventBeingUpdated;

    @FXML
    public AnchorPane eventAnchorPane;
    @FXML
    public Label createUpdateEventLabel;
    @FXML
    public TextField eventNameTextField, eventStartTextField, eventEndTextField, locationTextField, locationGuidanceTextField;
    @FXML
    public TextArea eventNotesTextArea;
    @FXML
    private Button cancelButton, confirmButton;
    private String type;

    public EMSCoordinatorEventCreator(){
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
        eventNotesTextArea.setWrapText(true);
        eventBeingUpdated = emsCoordinator.getEventBeingUpdated();
    }

    public void setType(String type) {
        this.type = type;
    }

    public void startupProgram() {

        if (type.equals("Create"))  {
            createUpdateEventLabel.setText("Create");
        }
        else
            createUpdateEventLabel.setText("Update");

        if (createUpdateEventLabel.getText().equals("Update")){
            updateEventSetup();
        }
    }

    public void updateEventSetup(){
        eventNameTextField.setText(eventBeingUpdated.getEventName());
        eventStartTextField.setText(eventBeingUpdated.getEventStartDateTime());
        eventEndTextField.setText(eventBeingUpdated.getEventEndDateTime());
        locationTextField.setText(eventBeingUpdated.getLocation());
        locationGuidanceTextField.setText(eventBeingUpdated.getLocationGuidance());
        eventNotesTextArea.setText(eventBeingUpdated.getEventNotes());
    }

    private void createNewEvent(){
        String eventName = eventNameTextField.getText();
        String eventStartDate = eventStartTextField.getText();
        String eventEndDate = null;
        if (!eventEndTextField.getText().isEmpty())
            eventEndDate = eventEndTextField.getText();
        String location = locationTextField.getText();
        String locationGuidance = null;
        if (!locationGuidanceTextField.getText().isEmpty())
            locationGuidance = locationGuidanceTextField.getText();
        String eventNotes = eventNotesTextArea.getText();

        be.Event event = new be.Event(eventName, eventStartDate, eventEndDate, location, locationGuidance, eventNotes, -1);
        try {
            eventModel.createNewEvent(event);
            cancelButton();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Failed to create event");
        }
    }

    private void updateEvent(){
        if (eventBeingUpdated != null){
            eventBeingUpdated.setEventName(eventNameTextField.getText());
            eventBeingUpdated.setEventStartDateTime(eventStartTextField.getText());
            eventBeingUpdated.setEventEndDateTime(eventEndTextField.getText());
            eventBeingUpdated.setLocation(locationTextField.getText());
            eventBeingUpdated.setLocationGuidance(locationGuidanceTextField.getText());
            eventBeingUpdated.setEventNotes(eventNotesTextArea.getText());
            eventBeingUpdated.setEventID(eventBeingUpdated.getEventID());

            try {
                eventModel.updateEvent(eventBeingUpdated);
                //New method for refreshing event list in previous window needed here
                cancelButton();
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Event could not be updated");
            }
        }
    }

    @FXML
    private void confirmButton(ActionEvent actionEvent) {
        if (Objects.equals(createUpdateEventLabel.getText(), "Update")){
            updateEvent();
        }
        if (Objects.equals(createUpdateEventLabel.getText(), "Create")){
            createNewEvent();
        }
    }

    @FXML
    private void cancelButton() {
        Stage parent = (Stage) eventNameTextField.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
