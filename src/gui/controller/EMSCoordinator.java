package gui.controller;

import be.Event;
import gui.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Circle;
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
    private Button btnCreateEvent;
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private StackPane profilePicturePane;
    private static EMSCoordinator instance;
    private final DisplayErrorModel displayErrorModel;
    private EventModel eventModel;
    private TicketModel ticketModel;
    private UserModel userModel;
    private EventTicketsModel eventTicketsModel;
    private ArchivedEventModel archivedEventModel;
    private static Event eventBeingUpdated;
    private HashMap<Integer, Pane> allEventBoxes = new HashMap<>(); // To store event box

    //TODO As hashmap to store picture so you dont have to load them each time
    private static final Image subtractIcon = new Image ("/Icons/subtract.png");
    private static final Image plusIcon = new Image ("/Icons/plus.png");
    private final Image mainIcon = new Image("Icons/mainIcon.png");
    private final Image defaultProfile = new Image("Icons/User_Icon.png");
    private boolean menuButtonVisible = false;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public EMSCoordinator(){
        instance = this;
        displayErrorModel = new DisplayErrorModel();
        try {
            eventModel = EventModel.getInstance();
            ticketModel = TicketModel.getInstance();
            archivedEventModel = ArchivedEventModel.getInstance();
            eventTicketsModel = EventTicketsModel.getInstance();
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
        setupEvents();  // Setup dynamic event
        anchorPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            double width = newValue.doubleValue(); // Get the new width of the AnchorPane
            int columnWidth = 300+40; // Width of each EventBox and margin
            tilePane.setPrefColumns((int) (width / columnWidth)); // Set the new preferred number
        });
        try { //We read user have image if something go wrong we show default
            userModel.readUserProfileIMG(userModel.getLoggedInUser());
            Image profileImage = userModel.getLoggedInUser().getProfileIMG();
            if (profileImage != null) {
                setProfilePicture(profileImage);
            }
        } catch (Exception ignored) {
        }
        try { //We read user have image if something go wrong we show default
            userModel.readUserProfileIMG(userModel.getLoggedInUser());
        } catch (Exception ignored) {
        }
        setupProfilePicture(); // We set up the Profile
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
        Image profileImage = userModel.getLoggedInUser().getProfileIMG();
        if (profileImage != null) {
            setProfilePicture(profileImage);
            return;
        }
        profilePicturePane.getChildren().clear();
        profilePicturePane.getChildren().addAll(profilePicture);
        profilePicture.setImage(defaultProfile);
        profilePicture.setScaleX(1);
        profilePicture.setScaleY(1);
    }

    public void setProfilePicture(Image img)    {
        Circle clip = new Circle(profilePicture.getFitWidth() / 2, profilePicture.getFitHeight() / 2, profilePicture.getFitWidth() / 2);
        profilePicture.setClip(clip);

        // Create a circle for the border
        Circle borderCircle = new Circle(clip.getCenterX(), clip.getCenterY(), clip.getRadius() - 19); // Adjust the radius for the border
        borderCircle.getStyleClass().add("borderCircleIMG");
        profilePicturePane.getChildren().clear();
        profilePicturePane.getChildren().addAll(borderCircle, profilePicture);
        profilePicture.setImage(img);
        profilePicture.setScaleX(0.61);
        profilePicture.setScaleY(0.61);
    }

    private void setupEvents()  {
        tilePane.getChildren().clear();
        List<Event> events = eventModel.getObsEvents();
        events.sort(Comparator.comparing(Event::getEventStartDateTime)); // Sort events by start date

        //We create all the event dynamic
        for (Event e : events) {
            Pane eventBox = createEventBox(e);
            allEventBoxes.put(e.getEventID(), eventBox);
            Insets insets = new Insets(20); // Set the insets for margin or padding
            TilePane.setMargin(eventBox, insets); // Apply the insets to the eventBox
            tilePane.getChildren().add(eventBox);
        } //The last is a + to add new one for coordinator
            Pane lastEventBox = createLastEventBox();
            Insets insets = new Insets(20); // Set the insets for margin or padding
            TilePane.setMargin(lastEventBox, insets); // Apply the insets to the lastEventBox
            tilePane.getChildren().add(lastEventBox);
    }

    // Method to create an event box
    private StackPane createEventBox(Event event) {
        StackPane eventBox = new StackPane();
        VBox vBoxEntireEvent = new VBox();
        HBox hBoxButton = new HBox();
        VBox vBoxLabels = new VBox();
        eventBox.setMinSize(300, 300);

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
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("You will delete event " + event.getEventName());
            alert.setContentText("Are you ok with this?");
            // Set the icon for the dialog window
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(mainIcon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    archivedEventModel.archiveEvent(event);
                    eventTicketsModel.deleteAllTicketsFromEvent(eventBeingUpdated);
                    eventModel.deleteEvent(event);
                    setupEvents();
                    tilePane.getChildren().remove(allEventBoxes.get(event.getEventID()));
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
        eventBox.getStyleClass().add("eventBox");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinatorEventCreateUpdate.fxml"));
            Parent root = loader.load();
            Stage EMSCoordinatorEventCUStage = new Stage();
            EMSCoordinatorEventCUStage.setTitle("Event Manager System Create Event");
            EMSCoordinatorEventCUStage.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSCoordinatorEventCUStage.initModality(Modality.APPLICATION_MODAL);
            EMSCoordinatorEventCreator controller = loader.getController();
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
    private void openArchivedEvents(ActionEvent actionEvent) {
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
        userModel.logOutUser();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMS.fxml"));
        Stage currentStage = (Stage) menuButtonLoggedInUser.getScene().getWindow();
        currentStage.setTitle("Event Manager System");
        Parent root = loader.load();
        EMSController controller = loader.getController();
        controller.setPrimaryStage(currentStage);
        controller.startupProgram();
        currentStage.setScene(new Scene(root));
    }
}
