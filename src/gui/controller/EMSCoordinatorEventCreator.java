package gui.controller;

import gui.model.ArchivedEventModel;
import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class EMSCoordinatorEventCreator implements Initializable {

    private EMSCoordinator emsCoordinator;

    private EventModel eventModel;

    private ArchivedEventModel archivedEventModel;

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
    private final Pattern eventNamePattern = Pattern.compile("[a-zæøåA-ZÆØÅ0-9\s*]{3,50}");
    private final Pattern eventLocationPattern = Pattern.compile("[a-zæøåA-ZÆØÅ0-9\s*]{3,80}");
    private final Pattern eventNotesPattern = Pattern.compile("[a-zæøåA-ZÆØÅ0-9\s*\n*]{3,300}");
    private final Pattern eventStartDatePattern = Pattern.compile("[0-9]{4}+-[0-9]{2}+-[0-9]{2}+\s[0-9]{2}+:[0-9]{2}+:[0-9]{2}");


    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }

    public void setArchivedEventModel(ArchivedEventModel archivedEventModel) { this.archivedEventModel = archivedEventModel;}


    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSCoordinatorEventCreator(){
        displayErrorModel = new DisplayErrorModel();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventNameTextField.textProperty().addListener((observable, oldValue, newValue) -> validateEventName());
        locationTextField.textProperty().addListener((observable, oldValue, newValue) -> validateEventLocation());
        eventNotesTextArea.textProperty().addListener((observable, oldValue, newValue) -> validateEventNotes());
        eventStartDatePicker.accessibleTextProperty().addListener((observable, oldValue, newValue) -> validateEventStartDate());
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
            final DateTimeFormatter startDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

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
            final DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

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

        String eventName;
        if (!eventNameTextField.getText().isEmpty() && Pattern.matches(String.valueOf(eventNamePattern), eventNameTextField.getText()))
            eventName = eventNameTextField.getText();
        else {
            displayErrorModel.displayErrorC("Missing Event Name");
            return;
        }
        String eventStartDate;
        if (!eventStartDatePicker.getEditor().getText().isEmpty() && Pattern.matches(String.valueOf(eventStartDatePattern), eventStartDatePicker.getEditor().getText()))
            eventStartDate = eventStartDatePicker.getEditor().getText();
        else{
            displayErrorModel.displayErrorC("Missing Event start Date");
            return;
        }
        String eventEndDate = null;
        if (!eventEndDatePicker.getEditor().getText().isEmpty())
            eventEndDate = eventEndDatePicker.getEditor().getText();
        String location;
        if (!locationTextField.getText().isEmpty() && Pattern.matches(String.valueOf(eventLocationPattern), locationTextField.getText()))
            location = locationTextField.getText();
        else {
            displayErrorModel.displayErrorC("Location for Event Missing");
            return;
        }
        String locationGuidance = null;
        if (!locationGuidanceTextField.getText().isEmpty())
            locationGuidance = locationGuidanceTextField.getText();
        String eventNotes;
        if (!eventNotesTextArea.getText().isEmpty() && Pattern.matches(String.valueOf(eventNotesPattern), eventNotesTextArea.getText()))
            eventNotes = eventNotesTextArea.getText();
        else {
            displayErrorModel.displayErrorC("Missing notes for Event");
            return;
        }
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
            if (!eventNameTextField.getText().isEmpty() && Pattern.matches(String.valueOf(eventNamePattern), eventNameTextField.getText()))
                eventBeingUpdated.setEventName(eventNameTextField.getText());
            else {
                displayErrorModel.displayErrorC("Missing Event Name");
                return;
            }
            if (!eventStartDatePicker.getEditor().getText().isEmpty())
                eventBeingUpdated.setEventStartDateTime(eventStartDatePicker.getEditor().getText());
            else {
                displayErrorModel.displayErrorC("Missing Event Start Date & Time");
                return;
            }
            eventBeingUpdated.setEventEndDateTime(eventEndDatePicker.getEditor().getText());
            if (!locationTextField.getText().isEmpty() && Pattern.matches(String.valueOf(eventLocationPattern), locationTextField.getText()))
                eventBeingUpdated.setLocation(locationTextField.getText());
            else {
                displayErrorModel.displayErrorC("Missing Event Location");
                return;
            }
            eventBeingUpdated.setLocationGuidance(locationGuidanceTextField.getText());
            if (!eventNotesTextArea.getText().isEmpty() && Pattern.matches(String.valueOf(eventNotesPattern), eventNotesTextArea.getText()))
                eventBeingUpdated.setEventNotes(eventNotesTextArea.getText());
            else {
                displayErrorModel.displayErrorC("Missing Event Notes");
                return;
            }
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

    public void validateEventName(){
        if (!eventNameTextField.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(eventNamePattern),eventNameTextField.getText())){
                eventNameTextField.setStyle("-fx-border-color: green;");
            }
            else {
                eventNameTextField.setStyle("-fx-border-color: red;");
            }
        }
        else
            eventNameTextField.setStyle("-fx-border-color: null");
    }

    public void validateEventLocation(){
        if (!locationTextField.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(eventLocationPattern),locationTextField.getText())){
                locationTextField.setStyle("-fx-border-color: green;");
            }
            else {
                locationTextField.setStyle("-fx-border-color: red;");
            }
        }
        else
            locationTextField.setStyle("-fx-background-color: null");
    }

    public void validateEventNotes(){
        if (!eventNotesTextArea.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(eventNotesPattern),eventNotesTextArea.getText())){
                eventNotesTextArea.setStyle("-fx-border-color: green;");
            }
            else {
                eventNotesTextArea.setStyle("-fx-border-color: red;");
            }
        }
        else
            eventNotesTextArea.setStyle("-fx-border-color: null");
    }
    public void validateEventStartDate(){
        if (!eventStartDatePicker.getEditor().getText().isEmpty()){
            if (Pattern.matches(String.valueOf(eventNotesPattern),eventNotesTextArea.getText())){
                eventStartDatePicker.setStyle("-fx-border-color: green;");
            }
            else {
                eventStartDatePicker.setStyle("-fx-border-color: red;");
            }
        }
        else
            eventStartDatePicker.setStyle("-fx-border-color: null");
    }
    @FXML
    private void confirmButton() {
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
