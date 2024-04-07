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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableColumn<User, Void> colUsersRemoveFromPrint, colUsersAddToPrint, colUsersPrintRemoveFromPrint, colUsersTicketRemove, colUsersTicketPrint, colUsersTicketPDF;
    @FXML
    private TableView<TicketSold> tblTicketPrint, tblTicketSold;

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
        lblTicketTitle.setText(currentTicket.getTicketName());
        ticketModel.ticketsUser(currentTicket);
        tblTicketSold.setItems(ticketModel.getObservableSoldTickets());
        colUserList.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));;
        colUserPrint.setCellValueFactory(new PropertyValueFactory<>("CustomUserInfo"));;
    }

    @FXML
    private void printTickets() {
      /*  List<TicketSold> ticketIDs = new ArrayList<>;
        // Run PDF creation on a separate thread
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    try {
                        ticketToPDF.makeTicketsToPDF(ticketIDs, emsTicketMain.getTicketArea());
                    } catch (Exception e) {
                        displayErrorModel.displayErrorC("Problems with creating the PDF");
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();

       */
    }

    @FXML
    private void cancelButton() {
        Stage parent = (Stage) lblTicketTitle.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}