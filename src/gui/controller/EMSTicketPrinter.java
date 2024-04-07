package gui.controller;

import be.TicketSold;
import be.Tickets;
import be.User;
import gui.model.DisplayErrorModel;
import gui.model.ImageModel;
import gui.model.TicketModel;
import gui.util.TicketToPDF;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.net.URL;
import java.util.ResourceBundle;


public class EMSTicketPrinter implements Initializable {

    @FXML
    private Pane ticketArea;
    @FXML
    private Label lblTicketTitle;
    @FXML
    private VBox loadingBox;
    @FXML
    private TableColumn<TicketSold, String> colUserList, colUserPrint;
    @FXML
    private TableColumn<User, Void> colUsersAddRemoveToPrint, colUsersPrintRemoveFromPrint;
    @FXML
    private TableView<TicketSold> tblTicketPrint, tblTicketSold;
    @FXML
    private Button printAllTickets, printTickets;

    private final DisplayErrorModel displayErrorModel;
    private final ImageModel systemIMGModel;
    private final TicketModel ticketModel;
    private final TicketToPDF ticketToPDF;
    private Tickets currentTicket;
    private EMSTicketMain emsTicketMain;

    public EMSTicketPrinter() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        ticketToPDF = new TicketToPDF();
        systemIMGModel = ImageModel.getInstance();
        ticketModel = TicketModel.getInstance();
    }

    public void setCurrentTicket(Tickets currentTicket) {
        this.currentTicket = currentTicket;
    }
    public void setEMSTicketMain(EMSTicketMain emsTicketMain) {
        this.emsTicketMain = emsTicketMain;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void startupProgram() throws Exception {
        tblTicketPrint.setPlaceholder(new Label("No ticket selected"));
        lblTicketTitle.setText(currentTicket.getTicketName());
        ticketModel.ticketsUser(currentTicket);
        tblTicketSold.setItems(ticketModel.getObservableSoldTickets());
        colUserList.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));
        colUserPrint.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));
        colUsersAddRemoveToPrint.setCellFactory(EMSTicketPrinter.ButtonCell.forTableColumn(ticketModel, this));
        colUsersPrintRemoveFromPrint.setCellFactory(EMSTicketPrinter.ButtonCell.forTableColumn(ticketModel, this));
    }

    @FXML
    private void printTickets() {
        if (tblTicketPrint.getItems().isEmpty()) {
            displayErrorModel.displayErrorC("You have no tickets selected");
            return;
        }
        disableButtons(true);
        Platform.runLater(() -> {  // Execute the task asynchronously
            try {
                ticketToPDF.makeTicketsToPDF(tblTicketPrint.getItems(), ticketArea);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Problems with creating the PDF");
            } finally {
                Platform.runLater(() -> disableButtons(false));
            }
        });
    }

    @FXML
    private void printAllTickets() {
        disableButtons(true);
        Platform.runLater(() -> {  // Execute the task asynchronously
            try {
                ticketToPDF.makeTicketsToPDF(ticketModel.getObservableSoldTickets(), ticketArea);
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Could not save all tickets to PDF");
            } finally {
                Platform.runLater(() -> disableButtons(false));
            }
        });
    }

    // Method to enable/disable buttons
    private void disableButtons(boolean disable) {
        // Assuming btnPrintTickets and btnPrintAllTickets are the IDs of your buttons
        printAllTickets.setDisable(disable);
        printTickets.setDisable(disable);
    }

    public TableView getTBLTicketPrint() {
        return tblTicketPrint;
    }
    public TableView getTBLTicketSold() {
        return tblTicketSold;
    }

    @FXML
    private void cancelButton() {
        Stage parent = (Stage) lblTicketTitle.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final Button UsersAddToPrintButton;
        private Button UsersRemoveFromPrintButton = null;
        private final Button RemoveFromPrintButton;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");
        private final Image AddIcon = new Image("/Icons/Plus_Icon.png");
        private final Image RemoveIcon = new Image("Icons/Subtract_Icon.png");

        private TicketSold currentTicketSold;
        private final EMSTicketPrinter emsTicketPrinter;

        public ButtonCell(TicketModel ticketModel, EMSTicketPrinter emsTicketPrinter) {
            this.displayErrorModel = new DisplayErrorModel();
            this.emsTicketPrinter = emsTicketPrinter;

            UsersAddToPrintButton = new Button();
            ImageView editImage = new ImageView();
            editImage.setFitWidth(20);
            editImage.setFitHeight(20);
            editImage.setImage(AddIcon);
            UsersAddToPrintButton.setGraphic(editImage);
            UsersAddToPrintButton.setPrefWidth(20); // Set preferred width
            UsersAddToPrintButton.setPrefHeight(20); // Set preferred height
            UsersAddToPrintButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketsSold) {
                    currentTicketSold = ticketsSold;
                    if (!emsTicketPrinter.getTBLTicketPrint().getItems().contains(ticketsSold)) {
                        emsTicketPrinter.getTBLTicketPrint().getItems().add(ticketsSold);

                        setGraphic(UsersRemoveFromPrintButton);
                    } else {
                        System.out.println("Ticket already exists in the print list.");
                    }
                }
            });

            UsersRemoveFromPrintButton = new Button();
            ImageView removeImage = new ImageView();
            removeImage.setFitWidth(20);
            removeImage.setFitHeight(20);
            removeImage.setImage(RemoveIcon);
            UsersRemoveFromPrintButton.setGraphic(removeImage);
            UsersRemoveFromPrintButton.setPrefWidth(20);
            UsersRemoveFromPrintButton.setPrefHeight(20);
            UsersRemoveFromPrintButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketsSold) {
                    currentTicketSold = ticketsSold;
                    if (emsTicketPrinter.getTBLTicketPrint().getItems().contains(ticketsSold)) {
                        emsTicketPrinter.getTBLTicketPrint().getItems().remove(ticketsSold);
                        setGraphic(UsersAddToPrintButton);
                    } else {
                        System.out.println("Ticket does not exist in the print list.");
                    }
                }

            });

            RemoveFromPrintButton = new Button();
            RemoveFromPrintButton.setGraphic(removeImage);
            RemoveFromPrintButton.setPrefWidth(20);
            RemoveFromPrintButton.setPrefHeight(20);
            RemoveFromPrintButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof TicketSold ticketsSold) {
                    currentTicketSold = ticketsSold;
                    if (emsTicketPrinter.getTBLTicketPrint().getItems().contains(ticketsSold)) {
                        emsTicketPrinter.getTBLTicketPrint().getItems().remove(ticketsSold);
                    } else {
                        System.out.println("Ticket does not exist in the print list.");
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
                TableView<?> currentTableView = getTableView();

                if (currentTableView == emsTicketPrinter.getTBLTicketSold()) {
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 1)
                        setGraphic(UsersAddToPrintButton);
                } else if (currentTableView == emsTicketPrinter.getTBLTicketPrint()) {
                    if (getTableView().getColumns().indexOf(getTableColumn()) == 1)
                        setGraphic(RemoveFromPrintButton);
                }
            }
        }

        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn(TicketModel ticketModel, EMSTicketPrinter emsTicketPrinter) {
            return param -> new EMSTicketPrinter.ButtonCell<>(ticketModel, emsTicketPrinter);
        }
    }
}