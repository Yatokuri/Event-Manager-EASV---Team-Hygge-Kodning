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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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

    private Node[] focusNodes; // To make tab work correct
    private int currentFocusIndex;
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
        txtInputFName.textProperty().addListener((observable, oldValue, newValue) -> validateTextField(txtInputFName, namesPattern));
        txtInputLName.textProperty().addListener((observable, oldValue, newValue) -> validateTextField(txtInputLName, namesPattern));
        txtInputEmail.textProperty().addListener((observable, oldValue, newValue) -> validateTextField(txtInputEmail, emailPattern));
        focusNodes = new Node[]{txtInputFName, txtInputLName, txtInputEmail};
        currentFocusIndex = 0;
        // Add event filter to handle Tab key press
        for (Node node : focusNodes) {
            node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabKeyPress);
        }
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
                displayErrorModel.displayErrorC("The ticket could not be loaded, You can still sell it!");
                Label lbl = new Label(" Ticket could not be shown");
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
                ticketArea.getChildren().add(lbl);
            }
        }).start();
    }

    private void showLoadingAnimation() { // This show an indicator with lbl under while image got compressed etc.
        ProgressIndicator progressIndicator = new ProgressIndicator();
        loadingBox = new VBox();
        Label lbl = new Label("Loading the ticket...");
        lbl.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        lbl.setWrapText(true);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(5);
        loadingBox.getChildren().addAll(progressIndicator, lbl);
        ticketArea.getChildren().add(loadingBox);
        progressIndicator.setMinSize(200, 80);
        progressIndicator.setMaxSize(200, 80);
        progressIndicator.setPrefSize(200, 80);
        loadingBox.setLayoutX(125);
        loadingBox.setLayoutY(12.5);
    }

    private void handleTabKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            event.consume();
            currentFocusIndex = (currentFocusIndex + 1) % focusNodes.length;
            focusNodes[currentFocusIndex].requestFocus();
        }
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
        }   // Set layout properties to position the node in the top-left corner
        for (Node node : recreatedPane.getChildren()) {
            if (node instanceof ImageView imageView) {
                imageView.setFitHeight(imageView.getFitHeight() != 0 ? imageView.getFitHeight()/2 : 0);
                imageView.setFitWidth(imageView.getFitWidth() != 0 ? imageView.getFitWidth()/2 : 0);
            }
            if (node instanceof Label lbl) {
                double fontSize = lbl.getFont().getSize();
                double dividedFontSize = (fontSize != 0) ? fontSize / 2 : 0;
                lbl.setFont(Font.font(dividedFontSize));
            }
                node.setLayoutX(node.getLayoutX() != 0 ? node.getLayoutX() / 2 : 0);
                node.setLayoutY(node.getLayoutY() != 0 ? node.getLayoutY() / 2 : 0);
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

    public void validateTextField(TextField textField, Pattern pattern) {
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

    public void validateInputFields() {
        isFNameValid = validateField(txtInputFName, namesPattern);
        isLNameValid = validateField(txtInputLName, namesPattern);
        isEmailValid = validateField(txtInputEmail, emailPattern);
    }

    private boolean validateField(TextField field, Pattern pattern) {
        String fieldValue = field.getText();
        if (!fieldValue.isEmpty() && pattern.matcher(fieldValue).matches()) {
            field.getStyleClass().removeAll("textFieldInvalid");
            field.getStyleClass().add("textFieldValid");
            return true;
        } else {
            field.getStyleClass().removeAll("textFieldValid");
            field.getStyleClass().add("textFieldInvalid");
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
            TicketSold soldTicket = new TicketSold(FName,LName, Email, currentTicket.getTicketID(),0);
            try {
                TicketSold newTicketSold = ticketModel.createNewSoldTicket(soldTicket);
                currentTicket.setTicketQuantity(currentTicket.getTicketQuantity()+1);
                ticketModel.updateTicket(currentTicket);
                if (ticketModel.readTicket(newTicketSold.getTicketID()).getTicketJSON().contains("\"ty\":\"QR\"") || ticketModel.readTicket(newTicketSold.getTicketID()).getTicketJSON().contains("\"ty\":\"BC\"")) { // Means there is a QR / BARCODE
                    ticketModel.createNewSoldTicketCode(newTicketSold);
                }
                emsTicketMain.refreshUserTbl();
                emsTicketMain.getTblEventTickets().refresh();
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
