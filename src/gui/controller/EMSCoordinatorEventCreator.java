package gui.controller;

import gui.model.ArchivedEventModel;
import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import javafx.css.PseudoClass;
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
    private final Pattern dateTimePattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}");


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
        eventNameTextField.textProperty().addListener((observable, oldValue, newValue) -> validateTextFields(eventNameTextField, eventNamePattern));
        locationTextField.textProperty().addListener((observable, oldValue, newValue) -> validateTextFields(locationTextField, eventLocationPattern));
        eventNotesTextArea.textProperty().addListener((observable, oldValue, newValue) -> validateTextAreas(eventNotesTextArea, eventNotesPattern));
        eventStartDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> validateTextFields(eventStartDatePicker.getEditor(), dateTimePattern));
        eventEndDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> validateTextFields(eventEndDatePicker.getEditor(), dateTimePattern));
        eventStartDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d{2}-\\d{2}-\\d{4}")) {  // Check if the new value matches the desired format (yyyy-MM-dd)
                showTimePicker("Start");
            }
        });
        eventEndDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d{2}-\\d{2}-\\d{4}")) {  // Check if the new value matches the desired format (yyyy-MM-dd)
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
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

        btnCancelTimePicker.setOnAction(event -> timePicker.setVisible(false));


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
            final DateTimeFormatter startDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
            final DateTimeFormatter endDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
        eventStartDatePicker.getEditor().setText(timeDateConverter(eventBeingUpdated.getEventStartDateTime()));
        eventEndDatePicker.getEditor().setText(timeDateConverter(eventBeingUpdated.getEventEndDateTime()));
        locationTextField.setText(eventBeingUpdated.getLocation());
        locationGuidanceTextField.setText(eventBeingUpdated.getLocationGuidance());
        eventNotesTextArea.setText(eventBeingUpdated.getEventNotes());
    }


    public String timeDateConverter( String timeDate)   { // Format LocalDateTime object to desired format
        LocalDateTime dateTime = LocalDateTime.parse(timeDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String validateTextField(TextField textField, Pattern pattern, String fieldName) {
        String fieldValue = textField.getText().trim();
        if (!fieldValue.isEmpty() && pattern.matcher(fieldValue).matches()) {
            return fieldValue;
        } else {
            missingFields.append("\n- ").append(fieldName);
            return null;
        }
    }

    private String validateTextArea(TextArea textarea, Pattern pattern) {
        String fieldValue = textarea.getText().trim();
        if (!fieldValue.isEmpty() && pattern.matcher(fieldValue).matches()) {
            return fieldValue;
        } else {
            missingFields.append("\n- ").append("Event Notes");
            return null;
        }
    }

    private String validateDateTimeField(DatePicker datePicker, Pattern pattern, String fieldName, DateTimeFormatter inputFormatter, DateTimeFormatter outputFormatter) {
        String fieldValue = datePicker.getEditor().getText().trim();
        if (!fieldValue.isEmpty() && pattern.matcher(fieldValue).matches()) {
            LocalDateTime dateTime = LocalDateTime.parse(fieldValue, inputFormatter);
            return dateTime.format(outputFormatter);
        } else {
            if (fieldValue.isEmpty() && !pattern.matcher(fieldValue).matches() && datePicker == eventStartDatePicker) {
                missingFields.append("\n- ").append(fieldName);
                return null;
            }
            if (!pattern.matcher(fieldValue).matches() && datePicker == eventStartDatePicker)   {
                missingFields.append("\n- ").append(fieldName);
                missingFields.append(" (Syntax Error)");
                return null;
            }
            if (!pattern.matcher(fieldValue).matches() && !eventEndDatePicker.getEditor().getText().isEmpty())   {
                missingFields.append("\n- ").append(fieldName);
                missingFields.append(" (Syntax Error)");
                return null;
            }
            if (datePicker == eventEndDatePicker)   { // EndDate is not Required, so it will not be added to the list only when text inside is broke
                return null;
            }
            missingFields.append("\n- ").append(fieldName);
            return null;
        }
    }

    StringBuilder missingFields = new StringBuilder();
    private void createOrUpdateEvent(boolean isNewEvent) {
        missingFields.delete(0, missingFields.length()); // We clear to get new fresh errors - // Here we check every field and add error message if required
        String eventName = validateTextField(eventNameTextField, eventNamePattern, "Event Name");
        String eventStartDate = validateDateTimeField(eventStartDatePicker, dateTimePattern, "Event Start Date & Time", DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        String eventEndDate = validateDateTimeField(eventEndDatePicker, dateTimePattern, "Event End Date & Time", DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        String location = validateTextField(locationTextField, eventLocationPattern, "Location");
        String locationGuidance = locationGuidanceTextField.getText().trim();
        String eventNotes = validateTextArea(eventNotesTextArea, eventNotesPattern);

        if (!missingFields.isEmpty()) {
            displayErrorModel.displayErrorCT("Please fill in the following fields: " + missingFields, "Missing Fields");
            return;
        }

        if (eventStartDate != null && !eventStartDate.isEmpty() && eventEndDate != null && !eventEndDate.isEmpty() && missingFields.isEmpty()) {
        LocalDateTime startDateTime = LocalDateTime.parse(eventStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        LocalDateTime endDateTime = LocalDateTime.parse(eventEndDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        if (endDateTime.isBefore(startDateTime)) {
            displayErrorModel.displayErrorC("End date must be after start date");
            return;
        }
    }
        try {  // Create event object
            if (isNewEvent) {
                be.Event event = new be.Event(eventName, eventStartDate, eventEndDate, location, locationGuidance, eventNotes, -1);
                eventModel.createNewEvent(event);
            } else { // Perform update action
                eventBeingUpdated.setEventName(eventName);
                eventBeingUpdated.setEventStartDateTime(eventStartDate);
                eventBeingUpdated.setEventEndDateTime(eventEndDate);
                eventBeingUpdated.setLocation(location);
                eventBeingUpdated.setLocationGuidance(locationGuidance);
                eventBeingUpdated.setEventNotes(eventNotes);
                eventModel.updateEvent(eventBeingUpdated);
            }
            emsCoordinator.startupProgram(); // Refresh UI
            cancelButton();
        } catch (Exception e) {
            displayErrorModel.displayErrorC(isNewEvent ? "Failed to create event" : "Event could not be updated");
        }
    }

    public void validateTextFields(TextField textField, Pattern pattern) {
        if (!textField.getText().isEmpty() && pattern.matcher(textField.getText()).matches()) {
            textField.getStyleClass().removeAll("textFieldInvalid", "textFieldNormal");
            textField.getStyleClass().add("textFieldValid");
        } else if (textField.getText().isEmpty()) {
            textField.getStyleClass().removeAll("textFieldValid", "textFieldInvalid");
            textField.getStyleClass().add("textFieldNormal");
        } else {
            textField.getStyleClass().removeAll("textFieldValid", "textFieldNormal");
            textField.getStyleClass().add("textFieldInvalid");
        }
    }
    public void validateTextAreas(TextArea txtArea, Pattern pattern) {
        if (!txtArea.getText().isEmpty() && pattern.matcher(txtArea.getText()).matches()) {
            txtArea.getStyleClass().removeAll("textFieldInvalid", "textFieldNormal");
            txtArea.getStyleClass().add("textFieldValid");
        } else if (txtArea.getText().isEmpty()) {
            txtArea.getStyleClass().removeAll("textFieldValid", "textFieldInvalid");
            txtArea.getStyleClass().add("textFieldNormal");
        } else {
            txtArea.getStyleClass().removeAll("textFieldValid", "textFieldNormal");
            txtArea.getStyleClass().add("textFieldInvalid");
        }
    }

    @FXML
    private void confirmButton() {
        if (Objects.equals(createUpdateEventLabel.getText(), "Update")){
            createOrUpdateEvent(false);
        }
        if (Objects.equals(createUpdateEventLabel.getText(), "Create")){
            createOrUpdateEvent(true);
        }
    }

    @FXML
    private void cancelButton() {
        Stage parent = (Stage) eventNameTextField.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
