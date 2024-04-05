package gui.controller;

import be.TicketSold;
import be.Tickets;
import gui.model.*;
import gui.util.TicketSerializerRecreate;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class EMSTicketShop implements Initializable {
    @FXML
    private TextField txtInputFName, txtInputLName, txtInputEmail;
    @FXML
    private Pane ticketArea;
    @FXML
    private Label lblTicketTitle;
    @FXML
    private VBox loadingBox;

    private boolean isFNameValid, isLNameValid, isEmailValid = false;
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private final Pattern namesPattern = Pattern.compile("[a-zæøåA-ZÆØÅ,0-9 *]{2,50}");


    public DisplayErrorModel displayErrorModel;
    private final ImageModel systemIMGModel;
    private final TicketModel ticketModel;
    private Tickets currentTicket;
    private EMSTicketMain emsTicketMain;
    public EMSTicketShop() throws Exception {
        displayErrorModel = new DisplayErrorModel();
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
        txtInputFName.textProperty().addListener((observable, oldValue, newValue) -> validateFName());
        txtInputLName.textProperty().addListener((observable, oldValue, newValue) -> validateLName());
        txtInputEmail.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
    }




    public void startupProgram() throws Exception { // This setup program
        lblTicketTitle.setText(currentTicket.getTicketName());
        ticketArea.getChildren().clear(); // Clear any existing content
        showLoadingAnimation();
        new Thread(() -> { // Load the ticket asynchronously so user can fast begin to sell
            try {
                Thread.sleep(500); // FAKE Loading
                Platform.runLater(() -> setupTicketView(currentTicket.getTicketJSON(), ticketArea));
            } catch (InterruptedException e) {
                displayErrorModel.displayErrorC("The ticket could not be loadet, You can still sell it!");
                Label lbl = new Label(" Ticket could not be shown");
                lbl.setStyle("-fx-font-size: 12px");
                ticketArea.getChildren().add(lbl);
            }
        }).start();
    }

    private void showLoadingAnimation() { // This show an indicator with lbl under while image got compressed etc.
        ProgressIndicator progressIndicator = new ProgressIndicator();
        loadingBox = new VBox();
        Label lbl = new Label("Loading the ticket...");
        lbl.setStyle("-fx-font-size: 18px");
        lbl.setWrapText(true);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(5);
        loadingBox.getChildren().addAll(progressIndicator, lbl);
        ticketArea.getChildren().add(loadingBox);
        progressIndicator.setMinSize(200, 200);
        progressIndicator.setMaxSize(200, 200);
        progressIndicator.setPrefSize(200, 200);
        loadingBox.setLayoutX(25);
        loadingBox.setLayoutY(50);
    }

    public void setupTicketView(String json, Pane paneName) {
        paneName.getChildren().clear(); // We clear the area, then we recreate it from the JSON
        Pane recreatedPane = TicketSerializerRecreate.cloneTicketAreaFromJson(json);
        // When its recreate we inset right Image from database
        for (Node node : recreatedPane.getChildren()) {
            if (node instanceof ImageView imageView) {
                String id = imageView.getId();
                Image image = getImageByID(id);
                if (image != null) {
                    imageView.setImage(image);
                }
            }
        }
        paneName.getChildren().addAll(recreatedPane.getChildren());
    }

    private Image getImageByID(String imageViewId) {
        try {
            return systemIMGModel.readSystemIMG(Integer.parseInt(imageViewId)).getImage();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Could not read image on the ticket");
        }
        return null;
    }

    public void validateFName(){
        if (!txtInputFName.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(namesPattern),txtInputFName.getText())){
                txtInputFName.setStyle("-fx-border-color: green;");
            }
            else {
                txtInputFName.setStyle("-fx-border-color: red;");
            }
        }
        else
            txtInputFName.setStyle("-fx-border-color: null");
    }

    public void validateLName(){
        if (!txtInputLName.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(namesPattern),txtInputLName.getText())){
                txtInputLName.setStyle("-fx-border-color: green;");
            }
            else {
                txtInputLName.setStyle("-fx-border-color: red;");
            }
        }
        else
            txtInputLName.setStyle("-fx-border-color: null");
    }

    public void validateEmail(){
        if (!txtInputEmail.getText().isEmpty()){
            if (Pattern.matches(String.valueOf(emailPattern),txtInputEmail.getText())){
                txtInputEmail.setStyle("-fx-border-color: green;");
            }
            else {
                txtInputEmail.setStyle("-fx-border-color: red;");
            }
        }
        else
            txtInputEmail.setStyle("-fx-border-color: null");
    }

    public void validateInputFields() {
        isFNameValid = validateField(txtInputFName, namesPattern);
        isLNameValid = validateField(txtInputLName, namesPattern);
        isEmailValid = validateField(txtInputEmail, emailPattern);
    }

    private boolean validateField(TextField field, Pattern pattern) {
        String fieldValue = field.getText();
        if (!fieldValue.isEmpty() && Pattern.matches(String.valueOf(pattern), fieldValue)) {
            field.setStyle("-fx-border-color: green;");
            return true;
        } else {
            field.setStyle("-fx-border-color: red;");
            return false;
        }
    }

    @FXML
    private void confirmButton() {
        validateInputFields();
        // Check if all fields are valid
        if (isFNameValid && isLNameValid && isEmailValid) {
            String FName = txtInputFName.getText();
            String LName = txtInputLName.getText();
            String Email = txtInputEmail.getText();
            TicketSold soldTicket = new TicketSold(FName,LName, Email, currentTicket.getTicketID(), 0 ,0);
            try {
                ticketModel.createNewSoldTicket(soldTicket);
                emsTicketMain.refreshUserTbl();
                //TODO Add the part there generated codes to the code DB
                cancelButton();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Display error message are missing or invalid
            StringBuilder errorMessage = new StringBuilder("Please fill in the following fields:");
            if (!isFNameValid) {
                errorMessage.append("\n- First Name");
            }
            if (!isLNameValid) {
                errorMessage.append("\n- Last Name");
            }
            if (!isEmailValid) {
                errorMessage.append("\n- Email");
            }
            displayErrorModel.displayErrorC(errorMessage.toString());
        }
    }

    @FXML
    private void cancelButton() {
        Stage parent = (Stage) lblTicketTitle.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
