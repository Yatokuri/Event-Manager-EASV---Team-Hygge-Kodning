package gui.controller;

import be.Tickets;
import be.User;
import gui.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EMSTicketMain implements Initializable {
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private ImageView profilePicture;
    @FXML Button createTicketButton;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public Label lblEventTitle;
    @FXML
    private StackPane profilePicturePane;
    @FXML
    private TableView<Tickets> tblEventTickets;
    @FXML
    private TableColumn<Tickets, String> colTicketName;
    @FXML
    private TableColumn<Tickets, Integer> colTicketQuantity;
    @FXML
    private TableColumn<User, Void> colRemove;
    private EMSCoordinator emsCoordinator;
    public Scene ticketMainStage;
    private EventModel eventModel;
    private UserModel userModel;
    private TicketModel ticketModel;
    private final DisplayErrorModel displayErrorModel;

    private be.Event selectedEvent;
    private User currentUser;
    private Stage emsCoordinatorStage;
    private boolean menuButtonVisible = false;

    private final Image defaultProfile = new Image("Icons/User_Icon.png");
    private ArchivedEventModel archivedEventModel;

    public void setEMSCoordinatorStage(Stage emsCoordinatorStage) {
        this.emsCoordinatorStage = emsCoordinatorStage;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSTicketMain() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        userModel = UserModel.getInstance();
        ticketModel = TicketModel.getInstance();
        eventModel = EventModel.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void startupProgram() { //This setup program
        currentUser = userModel.getLoggedInUser();
        selectedEvent = emsCoordinator.getEventBeingUpdated();
        lblEventTitle.setText(selectedEvent.getEventName());
        menuButtonLoggedInUser.setText(currentUser.getUserName());
        if (currentUser.getProfileIMG() != null)   { //If user have a picture set it
            setProfilePicture(currentUser.getProfileIMG());
        }
        setupTableview();
    }

    public void setupTableview() {
        tblEventTickets.setItems(ticketModel.getObsTickets());
        colTicketName.setCellValueFactory(new PropertyValueFactory<>("ticketName"));;
        colTicketQuantity.setCellValueFactory(new PropertyValueFactory<>("ticketQuantity"));
        tblEventTickets.setPlaceholder(new Label("No ticket found"));
        colTicketName.setOnEditCommit(event -> {
            String ticketName = event.getNewValue(); // Get the new ticket name
            System.out.println("Clicked ticket name: " + ticketName); // Print the clicked ticket name
        });
        colRemove.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel));
        // Custom cell factory for the colUsername column so we can do "● Name"
        colTicketName.setCellFactory(column -> {
            return new TableCell<Tickets, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label bullet = new Label("● ");
                        Label usernameLabel = new Label(item);
                        setGraphic(new HBox(bullet, usernameLabel)); // Add bullet and username to HBox
                        setOnMouseClicked(event -> { // So we can click on ticket to see them
                            if (!isEmpty()) {
                                Tickets ticket = getTableView().getItems().get(getIndex());
                                ticketModel.setCurrentTicket(ticket);
                                createTicket();
                            }
                        });
                    }
                }
            };
        });
    }
    public void createTicketButton() { //When use of the button no one is selected
        ticketModel.setCurrentTicket(null);
        createTicket();
    }
    public void createTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSTicketDesigner.fxml"));
            Parent root = loader.load();
            Stage EMSTicketDesigner = new Stage();
            EMSTicketDesigner.setTitle("Ticket Designer");
            EMSTicketDesigner.getIcons().add(new Image("/icons/mainIcon.png"));
            EMSTicketDesigner.setMaximized(false);
            EMSTicketDesigner controller = loader.getController();
            controller.setEMSCoordinator(emsCoordinator);
            controller.startupProgram();
            EMSTicketDesigner.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSTicketDesigner.show();
            // Close emsCoordinator and event information windows
            Stage ticketMainStage = (Stage) profilePicture.getScene().getWindow();
            ticketMainStage.close();
            controller.setEMSTicketMainStage(ticketMainStage); // Pass the emsCoordinator stage
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Ticket Window");
            alert.showAndWait();
        }
    }



    @FXML
    private void backButton() {
        // Bring the emsCoordinatorStage to front
        emsCoordinatorStage.show(); //1
        emsCoordinator.startupProgram();
        Stage parent = (Stage) lblEventTitle.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));

    }
//*******************PROFILE*DROPDOWN*MENU************************************
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
            controller.setEMSTicketMain(this);
            controller.startupProgram();
            EMSProfileSettings.setResizable(false);
            EMSProfileSettings.setScene(new Scene(root)); // Set the scene in the existing stage
            EMSProfileSettings.showAndWait();
            Platform.runLater(this::startupProgram);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
            alert.showAndWait();
        }
    }

    @FXML
    private void logoutUser() throws IOException {
        userModel.logOutUser();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMS.fxml"));
        Stage currentStage = (Stage) profilePicture.getScene().getWindow();        currentStage.setTitle("Event Manager System");
        Parent root = loader.load();
        EMSController controller = loader.getController();
        controller.setPrimaryStage(currentStage);
        controller.startupProgram();
        currentStage.setScene(new Scene(root));
    }

    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final javafx.scene.control.Button deleteButton;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");
        public ButtonCell(TicketModel ticketModel) {
            this.displayErrorModel = new DisplayErrorModel();

            deleteButton = new javafx.scene.control.Button("- ");
            deleteButton.setPrefWidth(20); // Set preferred width
            deleteButton.setPrefHeight(20); // Set preferred height
            deleteButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("You will delete ticket " + tickets.getTicketName());
                    alert.setContentText("Are you ok with this?");
                    // Set the icon for the dialog window
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            //ticketModel.deleteTicket(tickets);
                            System.out.println("You want to delete " +  tickets.getTicketName() + "but it deactivated");
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("User not deleted try again");
                        }
                    } else {
                        return;
                    }
                }
            });
        }



        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets) {
                        setGraphic(deleteButton);
                }
            }
        }
        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn(TicketModel ticketModel) {
            return param -> new ButtonCell<>(ticketModel);
        }
    }
}