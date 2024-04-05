package gui.controller;

import be.TicketSold;
import be.Tickets;
import be.User;
import gui.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.input.MouseButton;
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
    private TableView<TicketSold> tblEventTicketsUsers;
    @FXML
    private TableColumn<Tickets, String> colTicketName;
    @FXML
    private TableColumn<Tickets, Integer> colTicketQuantity;
    @FXML
    private TableColumn<TicketSold, String> colUser;
    @FXML
    private TableColumn<User, Void> colEdit, colRemove, colPrintSale, colUsersTicketRemove;
    private EMSCoordinator emsCoordinator;
    public Scene ticketMainStage;
    private EventModel eventModel;
    private UserModel userModel;
    private EventTicketsModel eventTicketsModel;
    private GlobalTicketsModel globalTicketsModel;
    private TicketModel ticketModel;
    private final DisplayErrorModel displayErrorModel;

    private be.Event selectedEvent;
    private User currentUser;
    private Stage emsCoordinatorStage;
    private Tickets selectedTicket;
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
        globalTicketsModel = GlobalTicketsModel.getInstance();
        eventTicketsModel = EventTicketsModel.getInstance();
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

    public void recreateTableview() {
        try {
            eventTicketsModel.eventTickets(selectedEvent);
            ObservableList<Tickets> combinedList = FXCollections.observableArrayList();
            combinedList.addAll(eventTicketsModel.getObservableEventsTickets());
            combinedList.addAll(globalTicketsModel.getObservableGlobalTickets());
            tblEventTickets.setItems(combinedList);
            tblEventTickets.setRowFactory(tv -> {
                TableRow<Tickets> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                        Tickets rowData = row.getItem();
                        if (rowData != null) {
                            setupTblEventTicketsUsers(rowData);
                        }
                    }
                });
                return row;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setupTableview() {
        recreateTableview();
        colTicketName.setCellValueFactory(new PropertyValueFactory<>("ticketName"));;
        colTicketQuantity.setCellValueFactory(new PropertyValueFactory<>("ticketQuantity"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));;
        tblEventTickets.setPlaceholder(new Label("No ticket found"));
        tblEventTicketsUsers.setPlaceholder(new Label("No ticket selected"));
        colTicketName.setOnEditCommit(event -> {
            String ticketName = event.getNewValue(); // Get the new ticket name
            System.out.println("Clicked ticket name: " + ticketName); // Print the clicked ticket name
        });
        colRemove.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colEdit.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel,this));
        colPrintSale.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel,this));
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
                        getTableView().setOnMouseClicked(event -> {
                            if (getTableView().getSelectionModel().getSelectedItem() == null) {
                                setupTblEventTicketsUsers(null);
                            }
                        });

                        setOnMouseClicked(event -> { // So we can click on ticket to see them
                            if (!isEmpty()) {
                                Tickets ticket = getTableView().getItems().get(getIndex());
                                ticketModel.setCurrentTicket(ticket);
                                selectedTicket = ticket;

                            }
                        });
                    }
                }
            };
        });
    }
    public void setupTblEventTicketsUsers(Tickets ticket) {
        if (ticket == null) {
            tblEventTicketsUsers.getItems().clear();
            tblEventTicketsUsers.setPlaceholder(new Label("No ticket selected"));
            return;
        }
        try {
            ticketModel.ticketsUser(ticket);
            tblEventTicketsUsers.setItems(ticketModel.getObservableSoldTickets());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        tblEventTicketsUsers.setPlaceholder(new Label(ticket.getTicketName() + " has not been sold yet"));

        //TODO Add stuff there check if users have purchase this ticket
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
            EMSTicketDesigner.setMaximized(true);
            EMSTicketDesigner controller = loader.getController();
            controller.setEMSCoordinator(emsCoordinator);
            controller.setEMSTicketMain(this);
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

    public void refreshUserTbl() throws Exception {
        ticketModel.ticketsUser(selectedTicket);
        tblEventTicketsUsers.setItems(ticketModel.getObservableSoldTickets());
    }

    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final Button deleteButton, editButton, printButton, saleButton;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");
        private final Image deleteIcon = new Image("/Icons/Trash_Icon.png");
        private final Image editIcon = new Image("Icons/Pencil_Icon.png");
        private final Image printIcon = new Image("Icons/Print_Icon.png");
        private final Image saleIcon = new Image("Icons/Basket_Icon.png");
        private Tickets currentTicket;
        private EMSTicketMain emsTicketMain;
        public ButtonCell(TicketModel ticketModel, EMSTicketMain emsTicketMain) {
            this.displayErrorModel = new DisplayErrorModel();
            this.emsTicketMain = emsTicketMain;
            deleteButton = new Button();
            ImageView deleteImage = new ImageView();
            deleteImage.setFitWidth(20);
            deleteImage.setFitHeight(20);
            deleteImage.setImage(deleteIcon);
            deleteButton.setGraphic(deleteImage);
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
                            //eventTicketsModel.deleteTicketsFromEvent(tickets);
                            //recreateTableview();
                            System.out.println("You want to delete " +  tickets.getTicketName() + "but it deactivated");
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("User not deleted try again");
                        }
                    } else {
                        return;
                    }
                }
            });

            editButton = new Button();
            ImageView editImage = new ImageView();
            editImage.setFitWidth(20);
            editImage.setFitHeight(20);
            editImage.setImage(editIcon);
            editButton.setGraphic(editImage);
            editButton.setPrefWidth(20); // Set preferred width
            editButton.setPrefHeight(20); // Set preferred height
            editButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets) {

                }
            });

            saleButton = new Button();
            ImageView saleImage = new ImageView();
            saleImage.setFitWidth(20);
            saleImage.setFitHeight(20);
            saleImage.setImage(saleIcon);
            saleButton.setGraphic(saleImage);
            saleButton.setPrefWidth(20);
            saleButton.setPrefHeight(20);
            saleButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets){
                    currentTicket = tickets;
                    openShopWindow();
                }
            });

            printButton = new Button();
            ImageView printImage = new ImageView();
            printImage.setFitWidth(20);
            printImage.setFitHeight(20);
            printImage.setImage(printIcon);
            printButton.setGraphic(printImage);
            printButton.setPrefWidth(20);
            printButton.setPrefHeight(20);
            printButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets){
                    openPrintWindow();
                }
            });
        }

        private void openShopWindow() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSTicketShop.fxml"));
                Parent root = loader.load();
                Stage EMSTicketShop = new Stage();
                EMSTicketShop.setTitle("Ticket Shop");
                EMSTicketShop.getIcons().add(new Image("/icons/mainIcon.png"));
                EMSTicketShop.initModality(Modality.APPLICATION_MODAL);
                EMSTicketShop controller = loader.getController();
                EMSTicketShop.setResizable(false);
                controller.setCurrentTicket(currentTicket);
                controller.setEMSTicketMain(emsTicketMain);
                controller.startupProgram();
                EMSTicketShop.setScene(new Scene(root)); // Set the scene in the existing stage
                EMSTicketShop.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Try to restart program");
                alert.showAndWait();
            }

        }

        private void openPrintWindow() {
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                S rowData = getTableView().getItems().get(getIndex());
                if (getTableView().getColumns().indexOf(getTableColumn()) == 2)
                    setGraphic(editButton);
                if (getTableView().getColumns().indexOf(getTableColumn()) == 3)
                    setGraphic(deleteButton);
                if (rowData instanceof Tickets tickets){
                    if ((getTableView().getColumns().indexOf(getTableColumn()) == 4) && tickets.getIsILocal() == 1)
                        setGraphic(saleButton);
                    else if ((getTableView().getColumns().indexOf(getTableColumn()) == 4) && tickets.getIsILocal() == 0)
                        setGraphic(printButton);
                }
            }
        }
        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn(TicketModel ticketModel, EMSTicketMain emsTicketMain) {
            return param -> new ButtonCell<>(ticketModel, emsTicketMain);
        }
    }
}
