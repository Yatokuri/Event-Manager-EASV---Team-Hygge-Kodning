package gui.controller;

import gui.model.ArchivedEventModel;
import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class EMSCoordinatorEventCreator implements Initializable {

    public HBox eventStartDateTimePicker;
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
    @FXML
    public VBox timePicker, eventCreateUpdateBackgroundVbox;
    @FXML
    private Button btnConfirmTimePicker, btnCancelTimePicker;
    @FXML
    private Spinner sliderHour, sliderMinute;
    private String type;
    private final Pattern eventNamePattern = Pattern.compile("[a-zæøåA-ZÆØÅ,0-9\s*]{3,50}");
    private final Pattern eventLocationPattern = Pattern.compile("[a-zæøåA-ZÆØÅ,0-9\s*]{3,80}");
    private final Pattern eventNotesPattern = Pattern.compile("[a-zæøåA-ZÆØÅ,.0-9\s*\n*]{3,300}");
    private final Pattern dateTimePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{1}");


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
        setupSlider();
        eventNameTextField.textProperty().addListener((observable, oldValue, newValue) -> validateEventName());
        locationTextField.textProperty().addListener((observable, oldValue, newValue) -> validateEventLocation());
        eventNotesTextArea.textProperty().addListener((observable, oldValue, newValue) -> validateEventNotes());
        eventStartDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> validateEventStartDate());
        eventEndDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> validateEventEndDate());
        eventStartDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d{4}-\\d{2}-\\d{2}")) {  // Check if the new value matches the desired format (yyyy-MM-dd)
                showTimePicker("Start");
            }
        });
        eventEndDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d{4}-\\d{2}-\\d{2}")) {  // Check if the new value matches the desired format (yyyy-MM-dd)
                showTimePicker("End");
            }
        });
    }

//************************************************CUSTOM*TIMEPICKER************************************************
    private void setupSlider() {
        // Set up spinner value factories with wrapping behavior and add event handler
        setupSpinnerFactory(sliderHour, 0, 23, 12);
        setupSpinnerFactory(sliderMinute, 0, 59, 30);
        addMouseScrollHandler(sliderHour);
        addMouseScrollHandler(sliderMinute);
    }
    private void setupSpinnerFactory(Spinner<Integer> spinner, int min, int max, int initialValue) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue, 1) {
            @Override
            public void increment(int steps) {
                this.setValue(this.getValue() + steps > getMax() ? getMin() : getValue() + steps);
            }
            @Override
            public void decrement(int steps) {
                this.setValue(this.getValue() - steps < getMin() ? getMax() : getValue() - steps);
            }
        };
        spinner.setValueFactory(valueFactory);
    }

    private void showTimePicker(String pos) {
        if (Objects.equals(pos, "Start"))   {
            timePicker.setLayoutX(36);
        }
        else if (Objects.equals(pos, "End")) {
            timePicker.setLayoutX(307);
        }

        timePicker.setVisible(true);

        btnConfirmTimePicker.setOnAction(event -> {
            int hour = (int) sliderHour.getValue();
            int minute = (int) sliderMinute.getValue();
            LocalDate currentDate = null;
            // Get the current date from the eventStartDatePicker
            if (Objects.equals(pos, "Start"))   {
                currentDate = eventStartDatePicker.getValue();
            }
            else if (Objects.equals(pos, "End")) {
                currentDate = eventEndDatePicker.getValue();
            }

            // Construct a LocalDateTime object with the selected time and current date
            assert currentDate != null;
            LocalDateTime selectedDateTime = LocalDateTime.of(currentDate, LocalTime.of(hour, minute));

            // Format the LocalDateTime object into the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            String formattedDateTime = selectedDateTime.format(formatter);

            // Set the formatted datetime string to the eventStartDatePicker field
            if (Objects.equals(pos, "Start"))   {
                eventStartDatePicker.getEditor().setText(formattedDateTime);
            }
            else if (Objects.equals(pos, "End")) {
                eventEndDatePicker.getEditor().setText(formattedDateTime);
            }

            timePicker.setVisible(false);
        });

        btnCancelTimePicker.setOnAction(event -> {
            timePicker.setVisible(false);
        });


        // Method to add event filter to hide timePicker when clicking outside of it
        Scene scene = timePicker.getScene();
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node target = (Node) event.getTarget();
            if (!isNodeInsideTimePicker(event, timePicker) && !target.equals(eventCreateUpdateBackgroundVbox)) {
                timePicker.setVisible(false);
            }
        });
    }

    // Method to check if the target node is inside the time picker or its children
    private boolean isNodeInsideTimePicker(MouseEvent event, Node timePicker) {
        Bounds pickerBounds = timePicker.localToScene(timePicker.getBoundsInLocal());
        Point2D clickPoint = new Point2D(event.getSceneX(), event.getSceneY());
        return pickerBounds.contains(clickPoint);
    }

    // Method to add event filter for mouse wheel scroll events to a Spinner so, it's easy to get a time
    private void addMouseScrollHandler(Spinner<Integer> spinner) {
        spinner.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() > 0) {
                spinner.increment();
            } else {
                spinner.decrement();
            }
            event.consume();
        });
    }
//************************************************STARTUP************************************************

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
            public LocalDate fromString(String string) {
                if (string == null || string.length() < 10) {return null;}
                String datePart = string.substring(0, 10);
                return LocalDate.parse(datePart, startDateFormatter);
            }
        });
        eventEndDatePicker.setConverter(new StringConverter<>(){
            final DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date){
                return (date != null) ? endDateFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.length() < 10) {return null;}
                String datePart = string.substring(0, 10);
                return LocalDate.parse(datePart, endDateFormatter);
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
        if (!eventStartDatePicker.getEditor().getText().isEmpty() && Pattern.matches(String.valueOf(dateTimePattern), eventStartDatePicker.getEditor().getText()))
            eventStartDate = eventStartDatePicker.getEditor().getText();
        else{
            displayErrorModel.displayErrorC("Missing Event start Date & Time");
            return;
        }
        String eventEndDate;
        if (eventEndDatePicker.getEditor().getText().isEmpty() || !eventEndDatePicker.getEditor().getText().isEmpty() && Pattern.matches(String.valueOf(dateTimePattern), eventEndDatePicker.getEditor().getText()))
            eventEndDate = eventStartDatePicker.getEditor().getText();
        else {
            displayErrorModel.displayErrorC("Missing Event End Date & Time");
            return;
        }
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
            if (!eventStartDatePicker.getEditor().getText().isEmpty() && Pattern.matches(String.valueOf(dateTimePattern), eventStartDatePicker.getEditor().getText()))
                eventBeingUpdated.setEventStartDateTime(eventStartDatePicker.getEditor().getText());
            else {
                displayErrorModel.displayErrorC("Missing Event Start Date & Time");
                return;
            }
            if (eventEndDatePicker.getEditor().getText().isEmpty() || eventEndDatePicker.getEditor().getText() == null) {
                eventBeingUpdated.setEventEndDateTime(eventEndDatePicker.getEditor().getText());
            }
            if (!eventEndDatePicker.getEditor().getText().isEmpty() && eventEndDatePicker.getEditor().getText() != null && Pattern.matches(String.valueOf(dateTimePattern), eventEndDatePicker.getEditor().getText())) //
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
            locationTextField.setStyle("-fx-border-color: null");
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
    public void validateEventStartDate() {
        if (!eventStartDatePicker.getEditor().getText().isEmpty()) {
            if (Pattern.matches(dateTimePattern.pattern(), eventStartDatePicker.getEditor().getText())) {
                eventStartDatePicker.setStyle("-fx-border-color: green;");
            } else {
                eventStartDatePicker.setStyle("-fx-border-color: red;");
            }
        } else {
            eventStartDatePicker.setStyle(null); // Reset border color
        }
    }

    public void validateEventEndDate() {
        if (!eventEndDatePicker.getEditor().getText().isEmpty()) {
            if (Pattern.matches(dateTimePattern.pattern(), eventEndDatePicker.getEditor().getText())) {
                eventEndDatePicker.setStyle("-fx-border-color: green;");
            } else {
                eventEndDatePicker.setStyle("-fx-border-color: red;");
            }
        } else {
            eventEndDatePicker.setStyle(null); // Reset border color
        }
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
