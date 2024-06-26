package gui.controller;

import be.Event;
import gui.model.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class EMSCoordinator {
    @FXML
    private TilePane tilePane;
    @FXML
    private ImageView profilePicture;
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private StackPane profilePicturePane;
    @FXML
    private MenuItem menuArchivedEvents;
    private static EMSCoordinator instance;
    private final DisplayErrorModel displayErrorModel;
    private EventModel eventModel;
    private NavbarModel navbarModel;
    private UserModel userModel;
    private EventTicketsModel eventTicketsModel;
    private ArchivedEventModel archivedEventModel;
    private ImageModel systemIMGModel;
    private boolean isItArchivedEvent = false;
    private static Event eventBeingUpdated;
    private final HashMap<Integer, Pane> allEventBoxes = new HashMap<>(); // To store event box
    private List<Event> currentEventList = null;
    private Pane lastEventBox;
    private Task<Void> currentLoadEventsTask; // currently running task

    //TODO As hashmap to store picture so you dont have to load them each time
    private static final Image subtractIcon = new Image ("/Icons/subtract.png");
    private static final Image plusIcon = new Image ("/Icons/Plus_Icon.png");
    private final Image mainIcon = new Image("Icons/mainIcon.png");
    private boolean menuButtonVisible = false;
    @FXML
    private ImageView backgroundIMGBlur;
    private final ArrayList<Event> retiredEvent = new ArrayList<>();
    StringBuilder missingFields = new StringBuilder();

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
    public void setIsItArchivedEvent(boolean isItArchivedEvent) {
        this.isItArchivedEvent = isItArchivedEvent;
    }
    public EMSCoordinator(){
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            eventModel = EventModel.getInstance();
            navbarModel = NavbarModel.getInstance();
            archivedEventModel = ArchivedEventModel.getInstance();
            eventTicketsModel = EventTicketsModel.getInstance();
            systemIMGModel = ImageModel.getInstance();
        } catch (Exception e) {
            displayErrorModel.displayError(e);
        }

    }

    public Event getEventBeingUpdated(){
        return eventBeingUpdated;
    }
    public void setEventBeingUpdated(Event event){
        eventBeingUpdated  = event;
    }
    public void startupProgram() { // This setup op the program
        menuButtonLoggedInUser.setText(userModel.getLoggedInUser().getUserName());
        if (Boolean.TRUE.equals(isItArchivedEvent))  {
            if (currentEventList  == null)  {
                setupEvents(archivedEventModel.getObsArchivedEvents());
                currentEventList = new ArrayList<>(archivedEventModel.getObsArchivedEvents());
            } else if (!currentEventList.equals(archivedEventModel.getObsArchivedEvents())) {
                setupEvents(archivedEventModel.getObsArchivedEvents());
                currentEventList = new ArrayList<>(archivedEventModel.getObsArchivedEvents());
            }
        }
        else {
            if (currentEventList  == null)  {
                currentEventList = new ArrayList<>(eventModel.getObsEvents());
                setupEvents(eventModel.getObsEvents());
            }
            else if (!currentEventList.equals(eventModel.getObsEvents())) {
                setupEvents(eventModel.getObsEvents());
                currentEventList = new ArrayList<>(eventModel.getObsEvents());
            }
        }
        anchorPane.widthProperty().addListener((observable, oldValue, newValue) -> setupUpEventSpace(newValue.doubleValue()));
        try { //We read user have image if something go wrong we show default
            userModel.readUserProfileIMG(userModel.getLoggedInUser());
            Image profileImage = userModel.getLoggedInUser().getProfileIMG();
            if (profileImage != null) {
                navbarModel.setProfilePicture(profilePicture, profilePicturePane, profileImage);
            }
        } catch (Exception ignored) {
        }
        try { //We read user have image if something go wrong we show default
            userModel.readUserProfileIMG(userModel.getLoggedInUser());
        } catch (Exception ignored) {
        }
        setupProfilePicture(); // We set up the Profile
        setupUpEventSpace(anchorPane.getWidth());

        // Bind the fitWidth and fitHeight properties of the background image to the width and height of the AnchorPane
        backgroundIMGBlur.fitWidthProperty().bind(anchorPane.widthProperty());
        backgroundIMGBlur.fitHeightProperty().bind(anchorPane.heightProperty());

    }

    public void setupUpEventSpace(double newValue) {
        if (tilePane.getChildren().isEmpty() || tilePane == null) {
            return;
        }
        int columnWidth = 300+40; // Width of each EventBox and margin
        tilePane.setPrefColumns((int) (newValue / columnWidth)); // Set the new preferred number
        int remainderSpace = (int) Math.ceil(((newValue - (tilePane.getPrefColumns()*300))/tilePane.getPrefColumns())/2);
        for (Pane pane : allEventBoxes.values()) { //We set new insets for all event
            Insets insets = new Insets(20,remainderSpace, 20, remainderSpace);
            TilePane.setMargin(pane, insets);

        } //And then the last special box
        Insets insets = new Insets(20, remainderSpace, 20, remainderSpace);
        if (lastEventBox != null) {
            TilePane.setMargin(lastEventBox, insets);
        }
    }


    public void showRetiredEvent() {
        LocalDateTime now = LocalDateTime.now().minusDays(1); // We remove 1 days to Event there just is done still can be there
        List<Event> events = eventModel.getObsEvents();
        String time;
        retiredEvent.clear();
        for (Event event : events) {
            if (event.getEventEndDateTime() == null || event.getEventEndDateTime().isEmpty())   {
                time = event.getEventStartDateTime();
            } else { // We use end date time
                time = event.getEventEndDateTime();
            }
            LocalDateTime endDateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
            if (endDateTime.isBefore(now)) { // Take all event there is done
                retiredEvent.add(event);
                missingFields.append("\n- ").append(event.getEventName());
            }
        }

        if (!retiredEvent.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Retired Event");
            alert.setHeaderText("");
            if (retiredEvent.size() == 1) {
                alert.setContentText("This event is retired will you delete it: " + missingFields);
            } else {
                alert.setContentText("These event is retired will you delete them: " + missingFields);
            }
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(mainIcon);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                archiveAllRetiredEvent();
            }
        }
    }

    private void archiveAllRetiredEvent()   {
        for (Event event : retiredEvent) {
            try {
                archivedEventModel.archiveEvent(event);
                eventTicketsModel.deleteAllTicketsFromEvent(event);
                eventModel.deleteEvent(event);
                tilePane.getChildren().remove(allEventBoxes.get(event.getEventID()));
                currentEventList = new ArrayList<>(eventModel.getObsEvents());
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Unable to delete retired event");
            }
        }
    }

    public void profilePicture() { // Profile IMG also control dropdown
        if (menuButtonVisible) {
            menuButtonLoggedInUser.hide();
            menuButtonVisible = false;
        } else {
            menuButtonLoggedInUser.show();
            menuButtonVisible = true;
        }
    }

    public void setupProfilePicture()   {
        navbarModel.setupProfilePicture(profilePicture, profilePicturePane);
    }

    private void setupEvents(List<Event> events)  {
        tilePane.getChildren().clear();

        events.sort(Comparator.comparing(Event::getEventStartDateTime)); // Sort events by start date
        // Check if there's already a task running
        if (currentLoadEventsTask != null && currentLoadEventsTask.isRunning()) {
            currentLoadEventsTask.cancel();
        }

        // Create a new task to load events
        currentLoadEventsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Event e : events) {
                    if (isCancelled()) { // Check if the task is cancelled
                        break;
                    }
                    Platform.runLater(() -> { // Create the event box on the JavaFX Application Thread
                        Pane eventBox = createEventBox(e);
                        allEventBoxes.put(e.getEventID(), eventBox);
                        Insets insets = new Insets(20); // Set the insets for margin or padding
                        TilePane.setMargin(eventBox, insets); // Apply the insets to the eventBox
                        tilePane.getChildren().add(eventBox);
                       setupUpEventSpace(anchorPane.getWidth());
                    });

                    // Simulate loading time (if needed)
                    Thread.sleep(50); // Adjust as needed
                }
                // After all events have been loaded, add the last event box
                Platform.runLater(() -> {
                    if (!isItArchivedEvent) {
                        lastEventBox = createLastEventBox();
                        Insets insets = new Insets(20); // Set the insets for margin or padding
                        TilePane.setMargin(lastEventBox, insets); // Apply the insets to the lastEventBox
                        tilePane.getChildren().add(lastEventBox);
                        setupUpEventSpace(anchorPane.getWidth());
                    }
                });
                return null;
            }
        };
        // Start the task in a new thread
        Thread loadEventsThread = new Thread(currentLoadEventsTask);
        loadEventsThread.start();
    }

    public void addOrUpdateEventInList(Event event) { // This method change or add the Event direct instead of just add all
        // Remove the old event box from the tile pane if it exists
        Pane oldEventBox = allEventBoxes.get(event.getEventID());
        if (oldEventBox != null) {
            tilePane.getChildren().remove(oldEventBox);
            allEventBoxes.remove(event.getEventID());
        }

        // Sort the list of events based on the start date and time and get index
        eventModel.getObsEvents().sort(Comparator.comparing(Event::getEventStartDateTime));
        int newIndex = eventModel.getObsEvents().indexOf(event);

        // Create the event box for the new or updated event
        Pane eventBox = createEventBox(event);
        allEventBoxes.put(event.getEventID(), eventBox);
        Insets insets = new Insets(20);
        TilePane.setMargin(eventBox, insets);
        // Insert at the new calculated position
        tilePane.getChildren().add(newIndex, eventBox);
        currentEventList = new ArrayList<>(eventModel.getObsEvents());
    }

    public void deleteEventInList(Event event)  {
        tilePane.getChildren().remove(allEventBoxes.get(event.getEventID()));
        currentEventList = new ArrayList<>(eventModel.getObsEvents());
    }

    // Method to create an event box
    private StackPane createEventBox(Event event) {
        StackPane eventBox = new StackPane();
        VBox vBoxEntireEvent = new VBox();
        if (event.getImageID() != 0) {
            try { /// Retrieve the ImageView from the systemIMGModel
                ImageView imageView = systemIMGModel.readSystemIMG(event.getImageID());
                // Set the ImageView as the background of vBoxEntireEvent
                vBoxEntireEvent.setBackground(new Background(new BackgroundImage(
                        imageView.getImage(),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT)));
            } catch (Exception ignored) {
            }
        }
        HBox hBoxButton = new HBox();
        VBox vBoxLabels = new VBox();
        eventBox.setMinSize(300, 300);
        eventBox.setMaxSize(300, 300);
        eventBox.setOnMouseClicked(eventMouse -> {
            if (!(eventMouse.getTarget() instanceof Button)) {
                btnOpenEvent(event);
            }
        });

        Button button = new Button();
        ImageView imageView = new ImageView(subtractIcon);
        imageView.setFitWidth(10);
        imageView.setFitHeight(25);
        button.setGraphic(imageView);
        imageView.setPickOnBounds(true); // Enable picking only on the icon

        button.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Delete Event");
            alert.setContentText("You are about to delete event " + event.getEventName() + ". \nThis action cannot be undone.\n\nAre you sure you want to proceed?");
            // Set the icon for the dialog window
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(mainIcon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (isItArchivedEvent)  {
                        archivedEventModel.deleteEvent(event);
                    }
                    else {
                        archivedEventModel.archiveEvent(event);
                        eventTicketsModel.deleteAllTicketsFromEvent(event);
                        eventModel.deleteEvent(event);
                    }
                    tilePane.getChildren().remove(allEventBoxes.get(event.getEventID()));
                    setupUpEventSpace(anchorPane.getWidth());
                } catch (Exception ex) {
                    displayErrorModel.displayErrorC("Unable to delete event");
                }
            }
        });


        String eventName = (event.getEventName() != null && !event.getEventName().isEmpty()) ? event.getEventName() : "No title";
        String location = (event.getLocation() != null && !event.getLocation().isEmpty()) ? event.getLocation() : "No location";
        String dateTime = (event.getEventStartDateTime() != null && !event.getEventStartDateTime().isEmpty()) ? convertDateTime(event.getEventStartDateTime()) : "No start time and day";

        Label lblTitle = new Label(eventName);
        Label lblLocation = new Label(location);
        Label lblDatetime = new Label(dateTime);
        Label txtDesc = new Label(event.getEventNotes());

        String eventNotesString = event.getEventNotes();
        if (eventNotesString.length() > 300) {  // Use first 100 characters and append "..."
            txtDesc.setText(eventNotesString.substring(0, 300) + "...");
        } else {
            txtDesc.setText(eventNotesString);
        }
        txtDesc.setMaxWidth(270); // Set maximum width
        txtDesc.setMaxHeight(260); // Set maximum height
        txtDesc.setWrapText(true); // Set text wrapping to true
        vBoxLabels.setPadding(new Insets(5, 15,15 ,15));
        vBoxLabels.getChildren().addAll(lblTitle, lblLocation, lblDatetime, txtDesc);
        vBoxLabels.setSpacing(15.0);

        hBoxButton.getChildren().add(button);
        hBoxButton.setAlignment(Pos.TOP_RIGHT);
        vBoxEntireEvent.getChildren().addAll(hBoxButton, vBoxLabels);
        vBoxEntireEvent.setAlignment(Pos.TOP_LEFT);
        eventBox.getChildren().add(vBoxEntireEvent);

        // Set the style up for specific CSS
        eventBox.getStyleClass().add("eventBox");
        lblTitle.getStyleClass().add("title");
        lblLocation.getStyleClass().add("underTitle");
        lblDatetime.getStyleClass().add("underTitle");
        button.getStyleClass().add("buttonDynamicEvent");
        button.getStyleClass().add("eventBox");

        if (event.getImageID() != 0) { //Special CSS if there is an Image
            eventBox.getStyleClass().add("image-box");
            lblTitle.getStyleClass().add("image-box");
            lblLocation.getStyleClass().add("image-box");
            lblDatetime.getStyleClass().add("image-box");
            button.getStyleClass().add("image-box");
            button.getStyleClass().add("image-box");
            txtDesc.getStyleClass().add("image-box");
        }
        return eventBox;
    }

    private StackPane createLastEventBox() {
        StackPane eventBox = new StackPane();
        eventBox.setMinSize(300, 300);

        Button button = new Button();
        ImageView imageView = new ImageView(plusIcon);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        button.setGraphic(imageView);
        imageView.setPickOnBounds(false); // Enable picking only on the icon
        button.setPickOnBounds(false);

        button.setMaxSize(100, 100); // Set button to fill its parent
        button.setOnAction(e -> {
            // Check if the button contains an ImageView in its graphic
            if (button.getGraphic() instanceof ImageView) {
                btnCreateEvent(); // Trigger the action
            }
        });


        eventBox.getChildren().add(button);
        // Set the style for specific CSS
        eventBox.getStyleClass().add("eventBox+");
        button.getStyleClass().add("buttonNewEvent");
        return eventBox;
    }


    private String convertDateTime(String dateTimeString) { // Helper method to get the date and time in correct format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'Kl.' HH:mm");
        return dateTime.format(outputFormatter);
    }

    public void btnCreateEvent()   { // Handle when coordinators make new event
        try {
            setEventBeingUpdated(null); // To prevent you can change last updated IMG from creating window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinatorEventCreateUpdate.fxml"));
            Parent root = loader.load();
            Stage EMSCoordinatorEventCUStage = new Stage();
            EMSCoordinatorEventCUStage.setTitle("Event Manager System Create Event");
            EMSCoordinatorEventCUStage.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSCoordinatorEventCUStage.initModality(Modality.APPLICATION_MODAL);
            EMSCoordinatorEventCreator controller = loader.getController();
            EMSCoordinatorEventCUStage.setResizable(false);
            controller.setType("Create");
            controller.setEventModel(eventModel);
            controller.setEMSCoordinator(this);
            controller.startupProgram();
            EMSCoordinatorEventCUStage.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSCoordinatorEventCUStage.showAndWait();
            startupProgram();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Try to restart program");
            alert.showAndWait();
        }
    }

    public void btnOpenEvent(Event selectedEvent)   { // Handle when coordinators open event
        try {
            eventBeingUpdated = selectedEvent;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSEventInformation.fxml"));
            Parent root = loader.load();
            Stage EMSEventInformation = new Stage();
            EMSEventInformation.setTitle("Event Manager System Create Event");
            EMSEventInformation.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSEventInformation.initModality(Modality.APPLICATION_MODAL);
            EMSEventInformation controller = loader.getController();
            EMSEventInformation.setResizable(false);
            controller.setIsItArchivedEvent(isItArchivedEvent);
            controller.setEventModel(eventModel);
            controller.setEMSCoordinator(this);
            controller.setEMSCoordinatorScene(profilePicture.getScene());
            controller.startupProgram();
            EMSEventInformation.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSEventInformation.showAndWait();
            Platform.runLater(this::startupProgram);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Try to restart program");
            alert.showAndWait();
        }
    }

    @FXML
    public void openArchivedEvents() throws Exception {
        if (isItArchivedEvent)  {
            isItArchivedEvent = false;
            setupEvents(eventModel.getObsEvents());
            currentEventList = new ArrayList<>(eventModel.getObsEvents());
            menuArchivedEvents.setText("Archived Events");
        }
        else {
            isItArchivedEvent = true;
            setupEvents(archivedEventModel.getArchivedEventsToBeViewed());
            currentEventList = new ArrayList<>(archivedEventModel.getObsArchivedEvents());
            menuArchivedEvents.setText("Active Events");
        }
        setupUpEventSpace(anchorPane.getWidth());
    }
    public void openOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSProfileSetting.fxml"));
            Parent root = loader.load();
            Stage EMSProfileSettings = new Stage();
            EMSProfileSettings.setTitle("Event Manager System Setting");
            EMSProfileSettings.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSProfileSettings.initModality(Modality.APPLICATION_MODAL);
            EMSProfileSettings controller = loader.getController();
            controller.setUserModel(userModel);
            controller.setEMSCoordinator(this);
            controller.startupProgram();
            EMSProfileSettings.setResizable(false);
            EMSProfileSettings.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSProfileSettings.showAndWait();
            Platform.runLater(this::startupProgram);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Try to restart program");
            alert.showAndWait();
        }
    }

    @FXML
    private void logoutUser() throws IOException {
        Stage currentStage = (Stage) menuButtonLoggedInUser.getScene().getWindow();
        navbarModel.logoutUser(currentStage);
    }
}
