package gui.controller;

import be.Event;
import be.TicketSold;
import be.Tickets;
import be.User;
import gui.model.*;
import gui.util.MailSender;
import gui.util.TicketToPDF;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EMSTicketMain implements Initializable {
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private ImageView profilePicture;
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
    private TableColumn<User, Void> colEdit, colRemove, colPrintSale, colUsersTicketRemove, colUsersTicketPrint, colUsersTicketPDF, colCheckTicket, colUsersTicketEmail;
    @FXML
    private MFXComboBox<be.Event> comboBoxEventList;
    @FXML
    private Pane ticketArea;
    @FXML
    private ImageView backgroundIMGBlur;
    private EMSCoordinator emsCoordinator;
    private final EventModel eventModel;
    private final UserModel userModel;
    private final EventTicketsModel eventTicketsModel;
    private final GlobalTicketsModel globalTicketsModel;
    private final TicketModel ticketModel;
    private final DisplayErrorModel displayErrorModel;
    private final NavbarModel navbarModel;

    private be.Event selectedEvent;
    private User currentUser;
    private Scene emsCoordinatorStage, emsTicketStage;
    private Tickets selectedTicket;
    private boolean menuButtonVisible = false;
    private final Image mainIcon = new Image("Icons/mainIcon.png");
    private ObservableList<Tickets> combinedListTicketTBL;
    private ObservableList<be.Event> eventsListComboBox;

    public void setEMSCoordinatorStage(Scene emsCoordinatorStage) {this.emsCoordinatorStage = emsCoordinatorStage;
    }
    public void setEMSTicketMain(Scene emsTicketStage) {
        this.emsTicketStage = emsTicketStage;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSTicketMain() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        userModel = UserModel.getInstance();
        ticketModel = TicketModel.getInstance();
        eventModel = EventModel.getInstance();
        navbarModel = NavbarModel.getInstance();
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
            navbarModel.setProfilePicture(profilePicture, profilePicturePane, currentUser.getProfileIMG());
        }
        // Bind the fitWidth and fitHeight properties of the background image to the width and height of the AnchorPane
        backgroundIMGBlur.fitWidthProperty().bind(anchorPane.widthProperty());
        backgroundIMGBlur.fitHeightProperty().bind(anchorPane.heightProperty());
        comboBoxEventList.setText(String.valueOf(selectedEvent));
        tblEventTickets.setPlaceholder(new Label("Loading data..."));
        tblEventTicketsUsers.setPlaceholder(new Label("Loading data..."));
        setupTableview();
        setupComboBox();

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
    }
    public void setupComboBox() {
      //  comboBoxEventList.getSelectionModel().selectItem(selectedEvent);
        comboBoxEventList.setOnAction((ActionEvent event) -> {
            be.Event selectedEventFromCombo = comboBoxEventList.getSelectionModel().getSelectedItem();
            if (selectedEventFromCombo != null) {
                selectedEvent = selectedEventFromCombo;
                recreateTableview();
                lblEventTitle.setText(selectedEvent.getEventName());
                tblEventTicketsUsers.setPlaceholder(new Label("No ticket selected"));
                tblEventTicketsUsers.getItems().clear();
                emsCoordinator.setEventBeingUpdated(selectedEvent);
            }
        });
    }

    public void addNewTicket(Tickets newTicket) {
        tblEventTickets.getItems().add(newTicket);
        tblEventTickets.sort();
        recreateTableview();
    }

    public void recreateTableview() {
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Load the ticket data
                eventTicketsModel.eventTickets(selectedEvent);
                ObservableList<Tickets> eventTickets = eventTicketsModel.getObservableEventsTickets();
                ObservableList<Tickets> globalTickets = globalTicketsModel.getObservableGlobalTickets();
                ObservableList<Event> eventsList = eventModel.getObsEvents();

                // Update the UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    combinedListTicketTBL = FXCollections.observableArrayList();
                    combinedListTicketTBL.addAll(eventTickets);
                    combinedListTicketTBL.addAll(globalTickets);
                    eventsListComboBox = FXCollections.observableArrayList();
                    eventsListComboBox.addAll(eventsList);
                    tblEventTickets.setItems(combinedListTicketTBL);
                    comboBoxEventList.setItems(eventsListComboBox);
                    tblEventTickets.setPlaceholder(new Label("No ticket found"));
                    tblEventTicketsUsers.setPlaceholder(new Label("No ticket selected"));
                });
                return null;
            }
        };

        // Start the task in a new thread
        Thread loadDataThread = new Thread(loadDataTask);
        loadDataThread.start();
    }

    public void setupTableview() {
        colTicketName.setCellValueFactory(new PropertyValueFactory<>("ticketName"));
        colTicketQuantity.setCellValueFactory(new PropertyValueFactory<>("ticketQuantity"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));
        colRemove.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colEdit.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel,this));
        colPrintSale.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel,this));
        colUsersTicketRemove.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colUsersTicketPDF.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colUsersTicketPrint.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colCheckTicket.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        colUsersTicketEmail.setCellFactory(EMSTicketMain.ButtonCell.forTableColumn(ticketModel, this));
        // Custom cell factory for the colUsername column so we can do "● Name"
        colTicketName.setCellFactory(column -> new TableCell<>() {
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
        if (ticket.getIsILocal() == 1)
            tblEventTicketsUsers.setPlaceholder(new Label(ticket.getTicketName() + " has not been sold yet"));
        else {
            tblEventTicketsUsers.setPlaceholder(new Label(ticket.getTicketName() + " cannot be sold"));
        }
    }
    public void updateTicketButton(Tickets tickets) { // This is when user want to edit
        ticketModel.setCurrentTicket(tickets);
        openCUTicket("update");
    }
    public be.Event getSelectedEvent() {return selectedEvent; }

    @FXML
    private void createTicketButton() {
        openCUTicket("create");
    }

    public void openCUTicket(String type) {
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
            controller.setType(type);
            controller.startupProgram();
            Scene scene = new Scene(root);
            Stage parent = (Stage) lblEventTitle.getScene().getWindow();
            parent.setScene(scene);
            controller.setEMSTicketMainStage(emsTicketStage); // Pass the emsCoordinator stage
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Ticket Window");
            alert.showAndWait();
        }
    }

    @FXML
    private void backButton() {
        // Bring the emsCoordinatorStage to front
        Stage parent = (Stage) lblEventTitle.getScene().getWindow();
        emsCoordinator.startupProgram();
        parent.setScene(emsCoordinatorStage);
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
        navbarModel.setupProfilePicture(profilePicture, profilePicturePane);
    }

    @FXML
    private void openArchivedEvents() throws Exception {
        if (currentUser.getUserAccessLevel() == 1) { //Not admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSCoordinator.fxml"));
            Stage currentStage = (Stage) profilePicture.getScene().getWindow();
            Parent root = loader.load();
            currentStage.setTitle("Event Manager System Coordinator");
            EMSCoordinator controller = loader.getController();
            controller.openArchivedEvents();
            controller.setUserModel(userModel);
            controller.startupProgram();
            currentStage.setScene(new Scene(root));
        }
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
        Stage currentStage = (Stage) profilePicture.getScene().getWindow();
        navbarModel.logoutUser(currentStage);
    }

    public void refreshUserTbl() throws Exception { // This method try to update the tbl Users
        if (selectedTicket == null) {
            Tickets selectedRow = tblEventTickets.getSelectionModel().getSelectedItem();
            if (selectedRow != null) {
                selectedTicket = selectedRow;
            } else {
                return;
            }
        }
        ticketModel.ticketsUser(selectedTicket);
        tblEventTicketsUsers.setItems(ticketModel.getObservableSoldTickets());
    }

    @FXML
    private void checkTicketButton() {
        checkTicket(null, ticketModel);
    }


    private void checkTicket(Tickets tickets, TicketModel ticketModel) {
        String type;
        boolean isLocal;
        if (tickets == null)    {
            isLocal = false;
            type = "All types of";
        } else {
            isLocal = (tickets.getIsILocal() == 1);
            type = isLocal ? "Local" : "Global";
        }

        // Create a text input dialog to prompt the user for the code
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter " + type + " Ticket Code");
        dialog.setHeaderText("Enter the code to check for " + type.toLowerCase() + " ticket:");
        dialog.setContentText("Code:");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            boolean codeExists;
            try {
                if (tickets == null) {
                    codeExists = ticketModel.checkGlobalTicketCode(input) ||
                            ticketModel.checkLocalTicketAllCode(input);

                } else if (isLocal) {
                    codeExists = ticketModel.checkLocalTicketCode(input, tickets.getTicketID());
                } else {
                    codeExists = ticketModel.checkGlobalTicketCode(input);
                }
                // Show the result in an alert dialog
                showAlert(input, codeExists);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Problem with checking code try again!");
            }
        });


    }

    private void showAlert(String code, boolean codeExists) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Code Check Result");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        // Set the alert content based on the result
        if (codeExists) {
            alert.setHeaderText(code + "  Exists");
            alert.setContentText("The ticket is valid.");
        } else {
            alert.setHeaderText(code + "  Not Found");
            alert.setContentText("The ticket is invalid.");
        }
        alert.showAndWait();
    }

    public TableView<TicketSold> getTblEventTicketsUsers() {
        return tblEventTicketsUsers;
    }
    public TableView<Tickets> getTblEventTickets() {
        return tblEventTickets;
    }
    public be.Event getCurrentEvent() {
        return selectedEvent;
    }
    public Pane getTicketArea() {
        return ticketArea;
    }
    public Tickets getSelectedTicket() {
        selectedTicket = tblEventTickets.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {return null;}
        return selectedTicket;
    }
    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final Button deleteButton, editButton, printButton, saleButton, removeUserButton, printButtonUser, tickettoPDFUserButton, checkTicketButton, emailButtonUser;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");
        private Tickets currentTicket;
        private TicketSold currentTicketSold;
        private final EMSTicketMain emsTicketMain;
        public ButtonCell(TicketModel ticketModel, EMSTicketMain emsTicketMain) {
            this.displayErrorModel = new DisplayErrorModel();
            this.emsTicketMain = emsTicketMain;
            deleteButton = new Button();
            ImageView deleteImage = new ImageView();
            deleteImage.setFitWidth(20);
            deleteImage.setFitHeight(20);
            Image deleteIcon = new Image("/Icons/Trash_Icon.png");
            deleteImage.setImage(deleteIcon);
            deleteButton.setGraphic(deleteImage);
            deleteButton.setPrefWidth(20); // Set preferred width
            deleteButton.setPrefHeight(20); // Set preferred height
            deleteButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets) {
                    currentTicket = tickets;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Delete Ticket");
                    alert.setContentText("You are about to delete ticket " + tickets.getTicketName() + ". \nThis action cannot be undone.\n\nAre you sure you want to proceed?");

                    // Set the icon for the dialog window
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            if (currentTicket.getIsILocal() == 0) {
                                GlobalTicketsModel globalTicketsModel = GlobalTicketsModel.getInstance();
                                globalTicketsModel.deleteGlobalTickets(currentTicket);
                                emsTicketMain.tblEventTickets.getItems().remove(currentTicket);
                                return;
                            }
                            if (currentTicket.getTicketQuantity() <= 0) {
                                EventTicketsModel eventTicketsModel = EventTicketsModel.getInstance();
                                eventTicketsModel.deleteTicketsFromEvent(currentTicket, emsTicketMain.getCurrentEvent());
                                emsTicketMain.tblEventTickets.getItems().remove(currentTicket);
                            } else {
                                displayErrorModel.displayErrorC("You cannot delete a ticket, which has been sold!");
                            }
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("Ticket not deleted try again");
                        }
                    }
                }
            });

            editButton = new Button();
            ImageView editImage = new ImageView();
            editImage.setFitWidth(20);
            editImage.setFitHeight(20);
            Image editIcon = new Image("Icons/Pencil_Icon.png");
            editImage.setImage(editIcon);
            editButton.setGraphic(editImage);
            editButton.setPrefWidth(20); // Set preferred width
            editButton.setPrefHeight(20); // Set preferred height
            editButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets) {
                    emsTicketMain.updateTicketButton(tickets);
                }
            });

            saleButton = new Button();
            ImageView saleImage = new ImageView();
            saleImage.setFitWidth(20);
            saleImage.setFitHeight(20);
            Image saleIcon = new Image("Icons/Basket_Icon.png");
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

            checkTicketButton = new Button();
            ImageView checkTicketImage = new ImageView();
            checkTicketImage.setFitWidth(20);
            checkTicketImage.setFitHeight(20);
            Image checkTicketIcon = new Image("Icons/CheckTicket_Icon.png");
            checkTicketImage.setImage(checkTicketIcon);
            checkTicketButton.setGraphic(checkTicketImage);
            checkTicketButton.setPrefWidth(20);
            checkTicketButton.setPrefHeight(20);
            checkTicketButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets){
                    currentTicket = tickets;
                    emsTicketMain.checkTicket(currentTicket, ticketModel);
                }
            });

            printButton = new Button();
            ImageView printImage = new ImageView();
            printImage.setFitWidth(20);
            printImage.setFitHeight(20);
            Image printIcon = new Image("Icons/Print_Icon.png");
            printImage.setImage(printIcon);
            printButton.setGraphic(printImage);
            printButton.setPrefWidth(20);
            printButton.setPrefHeight(20);
            printButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof Tickets tickets){
                    currentTicket = tickets;
                    openPrintWindow(ticketModel);
                }
            });
            removeUserButton = new Button();
            ImageView removeUserImage = new ImageView();
            removeUserImage.setFitWidth(20);
            removeUserImage.setFitHeight(20);
            removeUserImage.setImage(deleteIcon); // You need to define the removeUserIcon image
            removeUserButton.setGraphic(removeUserImage);
            removeUserButton.setPrefWidth(20);
            removeUserButton.setPrefHeight(20);
            removeUserButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketSold) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("You will delete " + ticketSold.getFirstName() + " ticket");
                    alert.setContentText("Are you ok with this? User can no longer use it!");
                    // Set the icon for the dialog window
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            currentTicket = emsTicketMain.getSelectedTicket();
                            currentTicket.setTicketQuantity(currentTicket.getTicketQuantity()-1);
                            emsTicketMain.getTblEventTickets().refresh();
                            ticketModel.updateTicket(currentTicket);
                            ticketModel.deleteSoldTicketCode(ticketSold);
                            ticketModel.deleteSoldTicket(ticketSold);
                            emsTicketMain.refreshUserTbl(); // Refresh the table view after removing the user
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("User not removed. Please try again.");
                        }
                    }
                }
            });
            printButtonUser = new Button();
            printButtonUser.setGraphic(printImage);
            printButtonUser.setPrefWidth(20);
            printButtonUser.setPrefHeight(20);
            printButtonUser.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketSold){
                    currentTicketSold = ticketSold;
                    openLocalPrintWindow();
                }
            });

            emailButtonUser = new Button();
            ImageView emailUserImage = new ImageView();
            emailUserImage.setFitWidth(20);
            emailUserImage.setFitHeight(20);
            Image MailIcon = new Image("Icons/MailSend_Icon.png");
            emailUserImage.setImage(MailIcon); // You need to define the removeUserIcon image
            emailButtonUser.setGraphic(emailUserImage);
            emailButtonUser.setPrefWidth(20);
            emailButtonUser.setPrefHeight(20);
            emailButtonUser.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketSold){
                    currentTicketSold = ticketSold;
                    if (!MailSender.testConnection())    {
                        displayErrorModel.displayErrorCT("""
                                Dear User,

                                We regret to inform you that our email system is unable to send statements due to potential internet restrictions.
                                It appears that your current internet connection is blocking our email service.

                                To resolve this issue and ensure timely receipt of your statements, please try the following steps:
                                1. Check your internet connection and ensure that it is stable.
                                2. Verify if any firewall or security settings are blocking outgoing email traffic.
                                3. Contact your internet service provider for assistance in unblocking our email service.
                                4. Your IT department has blocked the system we used, contact them.
                   
                                We apologize for any inconvenience caused and appreciate your understanding.""", "Warning: Your Internet May Be Blocking Email System");
                        return;
                    }

                    try {
                        TicketToPDF ticketToPDF = new TicketToPDF();
                        ticketModel.setCurrentTicket(emsTicketMain.getTblEventTickets().getSelectionModel().getSelectedItem());
                        ticketToPDF.makeTicketToPDF(ticketSold, emsTicketMain.getTicketArea(), emsTicketMain.getSelectedEvent(), true);
                    } catch (Exception e) {
                        displayErrorModel.displayErrorC("Could not send the email try again");
                    }
                }
            });

            tickettoPDFUserButton = new Button();
            ImageView pdfUserImage = new ImageView();
            pdfUserImage.setFitWidth(20);
            pdfUserImage.setFitHeight(20);
            Image PDFIcon = new Image("Icons/SavePDF_Icon.png");
            pdfUserImage.setImage(PDFIcon); // You need to define the PDFIcon image
            tickettoPDFUserButton.setGraphic(pdfUserImage);
            tickettoPDFUserButton.setPrefWidth(20);
            tickettoPDFUserButton.setPrefHeight(20);
            tickettoPDFUserButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketSold){
                    currentTicketSold = ticketSold;
                    try {
                        TicketToPDF ticketToPDF = new TicketToPDF();
                        ticketModel.setCurrentTicket(emsTicketMain.getTblEventTickets().getSelectionModel().getSelectedItem());
                        ticketToPDF.makeTicketToPDF(ticketSold, emsTicketMain.getTicketArea(), emsTicketMain.getSelectedEvent(), false);
                    } catch (Exception e) {
                        displayErrorModel.displayErrorC("Try to save as PDF again");
                    }
                }
            });
        }

        private void openPrintWindow(TicketModel ticketModel) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save as PDF");
            dialog.setHeaderText("Enter the number of tickets for: " + currentTicket.getTicketName());
            dialog.setContentText("Number of Tickets:");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(mainIcon);

            // Set validation for numeric input
            dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) { // Allow only digits
                    dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(input -> {
                // Check if the input is empty or not a valid number
                if (input.isEmpty() || Integer.parseInt(input) <= 0 || Integer.parseInt(input) > 100) {
                    displayErrorModel.displayErrorC("Please enter a valid number of tickets (1 to 100).");
                } else {
                    // The user entered a valid number
                    TicketToPDF ticketToPDF;
                    try {
                        ticketToPDF = new TicketToPDF();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Platform.runLater(() -> {  // Execute the task asynchronously
                        try {
                            ticketToPDF.makeGlobalTicketToPDF(currentTicket, Integer.parseInt(input), emsTicketMain.getTicketArea(), false);
                            currentTicket.setTicketQuantity(currentTicket.getTicketQuantity()+Integer.parseInt(input));
                            ticketModel.updateTicket(currentTicket); // We update sold quantity
                            emsTicketMain.getTblEventTickets().refresh();
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("Could not save all ticket(s) to PDF");
                        }
                    });
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

        private void openLocalPrintWindow() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMSTicketPrinter.fxml"));
                Parent root = loader.load();
                Stage EMSTicketPrinter = new Stage();
                EMSTicketPrinter.setTitle("Ticket Printer");
                EMSTicketPrinter.getIcons().add(new Image("/icons/mainIcon.png"));
                EMSTicketPrinter.initModality(Modality.APPLICATION_MODAL);
                EMSTicketPrinter controller = loader.getController();
                EMSTicketPrinter.setResizable(true);
                controller.setCurrentTicket(emsTicketMain.getSelectedTicket());
                controller.setEMSTicketMain(emsTicketMain);
                controller.startupProgram();
                EMSTicketPrinter.setScene(new Scene(root)); // Set the scene in the existing stage
                EMSTicketPrinter.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Try to select a ticket again");
                alert.showAndWait();
            }
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                S rowData = getTableView().getItems().get(getIndex());
                TableView<?> currentTableView = getTableView();

                if (currentTableView == emsTicketMain.getTblEventTickets()) {
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 2)
                        setGraphic(checkTicketButton);
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 3)
                        setGraphic(editButton);
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 4)
                        setGraphic(deleteButton);
                    if (rowData instanceof Tickets tickets) {
                        if ((getTableView().getColumns().indexOf(getTableColumn()) == 5) && tickets.getIsILocal() == 1)
                            setGraphic(saleButton);
                        else if ((getTableView().getColumns().indexOf(getTableColumn()) == 5) && tickets.getIsILocal() == 0)
                            setGraphic(printButton);
                    }
                }
                else if (currentTableView == emsTicketMain.getTblEventTicketsUsers()) {
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 1)
                        setGraphic(removeUserButton);
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 2)
                        setGraphic(tickettoPDFUserButton);
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 3)
                        setGraphic(emailButtonUser);
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 4)
                        setGraphic(printButtonUser);
                }
            }
        }
        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn(TicketModel ticketModel, EMSTicketMain emsTicketMain) {
            return param -> new ButtonCell<>(ticketModel, emsTicketMain);
        }
    }
}
