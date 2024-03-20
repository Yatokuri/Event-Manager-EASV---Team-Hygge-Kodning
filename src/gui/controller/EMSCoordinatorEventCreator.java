package gui.controller;

import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

public class EMSCoordinatorEventCreator implements Initializable {

    private EMSCoordinator emsCoordinator;

    private EventModel eventModel;

    private final DisplayErrorModel displayErrorModel;

    private be.Event eventBeingUpdated;

    @FXML
    public AnchorPane eventAnchorPane;
    @FXML
    public Label createUpdateEventLabel;
    @FXML
    public TextField eventNameTextField, locationTextField, locationGuidanceTextField;
    @FXML
    public DatePicker eventStartDatePicker, eventEndDatePicker;
    @FXML
    public TextArea eventNotesTextArea;
    private String type;


    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSCoordinatorEventCreator(){
        displayErrorModel = new DisplayErrorModel();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setType(String type) {
        this.type = type;
    }

    public void startupProgram() { //This setup program

        eventNotesTextArea.setWrapText(true);
        eventBeingUpdated = emsCoordinator.getEventBeingUpdated();

        if (type.equals("Create"))  {
            createUpdateEventLabel.setText("Create");
        }
        else
            createUpdateEventLabel.setText("Update");

        if (createUpdateEventLabel.getText().equals("Update")){
            updateEventSetup();
        }



        eventStartDatePicker.setConverter(new StringConverter<>(){
            final DateTimeFormatter startDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date){
                return (date != null) ? startDateFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string){
                return (string != null && !string.isEmpty())
                        ? LocalDate.parse(string, startDateFormatter) : null;
            }
        });
        eventEndDatePicker.setConverter(new StringConverter<>(){
            final DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date){
                return (date != null) ? endDateFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string){
                return (string != null && !string.isEmpty())
                        ? LocalDate.parse(string, endDateFormatter) : null;
            }
        });
    }

    public void updateEventSetup(){
        eventNameTextField.setText(eventBeingUpdated.getEventName());
        eventStartDatePicker.getEditor().setText(eventBeingUpdated.getEventStartDateTime());
        eventEndDatePicker.getEditor().setText(eventBeingUpdated.getEventEndDateTime());
        locationTextField.setText(eventBeingUpdated.getLocation());
        locationGuidanceTextField.setText(eventBeingUpdated.getLocationGuidance());
        eventNotesTextArea.setText(eventBeingUpdated.getEventNotes());
    }

    private void createNewEvent(){
        String eventName = eventNameTextField.getText();
        String eventStartDate = eventStartDatePicker.getEditor().getText();
        String eventEndDate = null;
        if (!eventEndDatePicker.getEditor().getText().isEmpty())
            eventEndDate = eventEndDatePicker.getEditor().getText();
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
            eventBeingUpdated.setEventStartDateTime(eventStartDatePicker.getEditor().getText());
            eventBeingUpdated.setEventEndDateTime(eventEndDatePicker.getEditor().getText());
            eventBeingUpdated.setLocation(locationTextField.getText());
            eventBeingUpdated.setLocationGuidance(locationGuidanceTextField.getText());
            eventBeingUpdated.setEventNotes(eventNotesTextArea.getText());
            eventBeingUpdated.setEventID(eventBeingUpdated.getEventID());

            try {
                eventModel.updateEvent(eventBeingUpdated);
                emsCoordinator.startupProgram(); // Refresh UI
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
