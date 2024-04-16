package gui.controller;

import gui.model.ArchivedEventModel;
import gui.model.DisplayErrorModel;
import gui.model.EventModel;
import gui.model.ImageModel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.awt.image.BufferedImage;
import java.io.File;
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
    private ImageModel imageModel;

    private ArchivedEventModel archivedEventModel;

    private final DisplayErrorModel displayErrorModel;

    private be.Event eventBeingUpdated;

    @FXML
    private Pane imagePane;
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
    public VBox timePicker, eventCreateUpdateBackgroundVbox, eventCreateUpdateBackgroundVboxIMG;
    @FXML
    private Button btnConfirmTimePicker, btnCancelTimePicker, imageButton, confirmButton, btnDeleteImage, cancelButton;
    @FXML
    private Spinner<Integer> sliderHour, sliderMinute;
    @FXML
    private ImageView eventImage;
    private String type;
    private final Pattern eventNamePattern = Pattern.compile("[a-zæøåA-ZÆØÅ,0-9\s*]{3,50}");
    private final Pattern eventLocationPattern = Pattern.compile("[a-zæøåA-ZÆØÅ,0-9\s*]{3,80}");
    private final Pattern eventNotesPattern = Pattern.compile("[a-zæøåA-ZÆØÅ,.0-9\s*\n]{3,300}");
    private final Pattern dateTimePattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}");
    private Rectangle pictureCapture; // When we capture IMG
    private boolean imageMode = false;
    private Image currentEventPicture, originalEventPicture;
    private final Image defaultProfileIMG = new Image("Icons/EventDefault_Icon.png");

    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }

    public void setArchivedEventModel(ArchivedEventModel archivedEventModel) { this.archivedEventModel = archivedEventModel;}


    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSCoordinatorEventCreator() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        imageModel = ImageModel.getInstance();
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
        setupSpinnerFactory(sliderHour, 23, 12);
        setupSpinnerFactory(sliderMinute, 59, 30);
        addMouseScrollHandler(sliderHour);
        addMouseScrollHandler(sliderMinute);
    }
    private void setupSpinnerFactory(Spinner<Integer> spinner, int max, int initialValue) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, initialValue, 1) {
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
            LocalDateTime selectedDateTime = getLocalDateTime(pos);

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

    private LocalDateTime getLocalDateTime(String pos) {
        int hour = sliderHour.getValue();
        int minute = sliderMinute.getValue();
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
        return LocalDateTime.of(currentDate, LocalTime.of(hour, minute));
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
        if (eventBeingUpdated.getEventEndDateTime() != null) {
            eventEndDatePicker.getEditor().setText(timeDateConverter(eventBeingUpdated.getEventEndDateTime()));
        }
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
        if (imageMode)  { //Image mode use all this button to confirm
            confirmNewEventIMG();
            return;
        }
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

        int eventImageID = 0;
        if (currentEventPicture != null) {
            try {
                imageModel = ImageModel.getInstance();
                eventImageID = imageModel.getNextIDSystemIMG();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        try {  // Create event object
            if (isNewEvent) {
                be.Event event = new be.Event(eventName, eventStartDate, eventEndDate, location, locationGuidance, eventNotes, -1, eventImageID);
                imageModel.createSystemIMG(currentEventPicture);
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
            emsCoordinator.setIsItArchivedEvent(false);
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
        if (imageMode)  {
            eventCreateUpdateBackgroundVbox.setVisible(true);
            eventCreateUpdateBackgroundVboxIMG.setVisible(false);
            btnDeleteImage.setVisible(false);
            imageButton.setText("Image");
            confirmButton.setText("Confirm");
            cancelButton.setText("Cancel");
            imageMode = false;
            return;
        }
        Stage parent = (Stage) eventNameTextField.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

//************************************************IMAGE***********************************************

    public void setEventPicture(Image img) { // This setup the Image
        eventImage.setFitWidth(300);
        eventImage.setFitHeight(300);
        eventImage.setPreserveRatio(false);
        eventImage.setImage(img);

        if (img != defaultProfileIMG) { // Default picture doesn't need a border
            double width = eventImage.getFitWidth();
            double height = eventImage.getFitHeight();

            Rectangle clip = new Rectangle(width, height); // Create a rectangular clip
            eventImage.setClip(clip); // Set the rectangular clip

            Rectangle borderRectangle = new Rectangle(width, height); // Create a rectangle for the border
            // Add the border rectangle behind the clip
            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(borderRectangle, eventImage);
            imagePane.getChildren().clear();
            imagePane.getChildren().add(stackPane);
        }

    }

    public void imageButton() throws Exception { //Here we open Image changing window, so we close a lot and something new
        if (eventBeingUpdated == null)  {
            displayErrorModel.displayErrorC("You can first add image after event, \nis created due to internal policies.");
            return;
        }

        if (imageMode)  {
            uploadImage();
            return;
        }
        eventCreateUpdateBackgroundVbox.setVisible(false);
        eventCreateUpdateBackgroundVboxIMG.setVisible(true);
        btnDeleteImage.setVisible(true);
        imageButton.setText("Upload IMG");
        confirmButton.setText("Confirm IMG");
        cancelButton.setText("Cancel IMG");
        imageMode = true;

        if (eventBeingUpdated.getImageID() != 0)    {
            currentEventPicture = imageModel.readSystemIMG(eventBeingUpdated.getImageID()).getImage();
        }
        else {
            currentEventPicture = defaultProfileIMG;
        }
        originalEventPicture = currentEventPicture;
        eventImage.setImage(currentEventPicture);
        setEventPicture(currentEventPicture);
        setupResizableAndDraggableSystem();
    }


    public void confirmNewEventIMG() {
        if (currentEventPicture == defaultProfileIMG) {
            pictureCapture.setVisible(false);
            try {
                imageModel.deleteSystemIMG(eventBeingUpdated.getImageID());
                eventBeingUpdated.setImageID(0);
                eventModel.updateEvent(eventBeingUpdated);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Problem delete Event image");
            }
            cancelButton();
            return;
        }

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        // Define the viewport to capture the area within the rectangle
        Rectangle2D viewport = new Rectangle2D(
                pictureCapture.getBoundsInParent().getMinX(),
                pictureCapture.getBoundsInParent().getMinY(),
                pictureCapture.getWidth(),
                pictureCapture.getHeight());
        parameters.setViewport(viewport);

        // Take a snapshot of the ImageView within the defined viewport
        Image capturedImage = eventImage.snapshot(parameters, null);

        // Resize the captured image to 300x300
        Image resizedImage = resizeImage(capturedImage, 300, 300);

        // Set the resizedImage as the new profile image
        eventImage.setImage(resizedImage);
        currentEventPicture = resizedImage;
        originalEventPicture = resizedImage;
        setEventPicture(resizedImage);
        pictureCapture.setVisible(false);

        // Save the resized image to the database
        try {
            saveImageToDatabase(resizedImage);
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Failed to save new event Image");
        }
        cancelButton();
    }

    // Method to resize the image to the specified width and height
    private Image resizeImage(Image image, int width, int height) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        BufferedImage resizedBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Image scaledImage = bufferedImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        resizedBufferedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
        return SwingFXUtils.toFXImage(resizedBufferedImage, null);
    }

    // Method to save the image to the database
    private void saveImageToDatabase(Image image) throws Exception {
        if (eventBeingUpdated.getImageID() != 0 && currentEventPicture != null) {
            imageModel.updateSystemIMG(eventBeingUpdated.getImageID(), image);
        } else { //If first time with Image we create a new otherwise we update it
            eventBeingUpdated.setImageID(imageModel.createSystemIMG(image));
            eventModel.updateEvent(eventBeingUpdated);
        }
    }

    public void btnDeleteImage() {
        currentEventPicture = defaultProfileIMG;
        setEventPicture(defaultProfileIMG);
    }

    public void setupResizableAndDraggableSystem() {
        // Create a rectangle with 50px radius. Adjust the center to be over the image.
        pictureCapture = new Rectangle(50, 50);
        // Assuming the image is centered in the Pane, adjust these values as needed.
        pictureCapture.setLayoutX((eventImage.getFitWidth() -50) / 2);
        pictureCapture.setLayoutY((eventImage.getFitHeight() -50) / 2);
        pictureCapture.getStyleClass().add("pictureCaptureIMG");  // Create a rectangle, so we can make a border around the picture
        pictureCapture.setFill(Color.TRANSPARENT);
        pictureCapture.setStroke(Color.ORANGERED);
        pictureCapture.setStrokeWidth(5);
        // Clear any existing clips or rectangles from previous operations.
        imagePane.getChildren().removeIf(node -> node instanceof Rectangle);

        // Add the rectangle to the pane to make it appear over the image.
        imagePane.getChildren().add(pictureCapture);

        // Make the rectangle draggable
        makeResizableAndDraggable(pictureCapture, eventImage);
    }


    public void uploadImage() {
        FileChooser fileChooserDialog = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooserDialog.getExtensionFilters().add(extFilter);

        // Set the owner window for the file chooser dialog
        Stage ownerStage = (Stage) eventCreateUpdateBackgroundVboxIMG.getScene().getWindow();

        // Show the file chooser dialog
        File selectedFile = fileChooserDialog.showOpenDialog(ownerStage);

        if (selectedFile != null) {
            eventImage.setClip(null);
            currentEventPicture = new Image(selectedFile.toURI().toString());

            double fitWidth = 550; // Your specified fit width
            double fitHeight = 400; // Your specified fit height

            // Calculate the scaled width and height while preserving the aspect ratio
            double scaledWidth = currentEventPicture.getWidth();
            double scaledHeight = currentEventPicture.getHeight();

            if (scaledWidth > fitWidth) {
                scaledHeight *= fitWidth / scaledWidth;
                scaledWidth = fitWidth;
            }

            if (scaledHeight > fitHeight) {
                scaledWidth *= fitHeight / scaledHeight;
                scaledHeight = fitHeight;
            }

            // Set the scaled width and height to the image view
            eventImage.setFitWidth(scaledWidth);
            eventImage.setFitHeight(scaledHeight);

            // Set the image and reset rectangle
            eventImage.setImage(null);
            eventImage.setImage(currentEventPicture);
            setupResizableAndDraggableSystem();
        }
    }

    private void makeResizableAndDraggable(Rectangle rectangle, ImageView imageView) {
        final double[] initialMousePos = new double[2];
        final double[] initialRectanglePos = new double[2];
        final double[] initialRectangleWidth = new double[1];
        final double[] initialRectangleHeight = new double[1];
        final boolean[] isResizing = {false};

        rectangle.setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) { // Check if right mouse button is pressed
                isResizing[0] = true;
                initialMousePos[0] = e.getSceneX();
                initialMousePos[1] = e.getSceneY();
                initialRectangleWidth[0] = rectangle.getWidth();
                initialRectangleHeight[0] = rectangle.getHeight();
            } else {
                initialMousePos[0] = e.getSceneX();
                initialMousePos[1] = e.getSceneY();
                initialRectanglePos[0] = rectangle.getX();
                initialRectanglePos[1] = rectangle.getY();

            }
        });

        rectangle.setOnMouseDragged(e -> {
            if (isResizing[0]) {
                double deltaX = e.getSceneX() - initialMousePos[0];
                double deltaY = e.getSceneY() - initialMousePos[1];

                double newWidth = initialRectangleWidth[0] + deltaX;
                double newHeight = initialRectangleHeight[0] + deltaY;

                // Maintain the aspect ratio
                double aspectRatio = initialRectangleWidth[0] / initialRectangleHeight[0];

                // Adjust width or height based on the change in the other dimension
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    newHeight = newWidth / aspectRatio;
                } else {
                    newWidth = newHeight * aspectRatio;
                }

                // Constrain the size within the bounds of the imageView
                double maxWidth = imageView.getBoundsInParent().getWidth() - initialRectanglePos[0];
                double maxHeight = imageView.getBoundsInParent().getHeight() - initialRectanglePos[1];

                double width = Math.min(newWidth, maxWidth);
                double height = Math.min(newHeight, maxHeight);

                rectangle.setWidth(width);
                rectangle.setHeight(height);
            } else {
                double newX = e.getSceneX() - initialMousePos[0] + initialRectanglePos[0];
                double newY = e.getSceneY() - initialMousePos[1] + initialRectanglePos[1];

                // Calculate the scaling factors dynamically
                double referenceWidth = 300.0; // Define your reference width here
                double referenceHeight = 300.0; // Define your reference height here

                double widthRatio = imageView.getFitWidth() / referenceWidth;
                double heightRatio = imageView.getFitHeight() / referenceHeight;

                // Adjust the position of the rectangle to stay within the bounds of the imageView
                newX = Math.max(-(imageView.getFitWidth()/2)+25, Math.min(newX, (imageView.getFitWidth()/(1.5*widthRatio) - rectangle.getWidth())));
                newY = Math.max(-(imageView.getFitHeight()/2)+25, Math.min(newY, (imageView.getFitHeight()/(1.5*heightRatio) - rectangle.getHeight())));


                // Update rectangle position
                rectangle.setX(newX);
                rectangle.setY(newY);
            }
        });

        rectangle.setOnMouseReleased(e -> isResizing[0] = false);
    }

}