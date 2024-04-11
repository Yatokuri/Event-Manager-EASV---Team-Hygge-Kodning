package gui.controller;

import be.Tickets;
import be.User;
import gui.model.*;
import gui.util.BarCode;
import gui.util.ImageCompressor;
import gui.util.TicketSerializerRecreate;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EMSTicketDesigner implements Initializable {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private ImageView profilePicture, deleteDropReleaseIMG;
    @FXML
    private Pane ticketArea;
    @FXML
    private CheckBox boldCheckBox, italicCheckBox, underlineCheckBox;
    @FXML
    private Slider textfontSizeSlider, textRotateSlider, imageSizeSlider, imageRotateSlider;
    @FXML
    private HBox deleteDropRelease, eventHBoxSection;
    @FXML
    private VBox loadingBox;
    @FXML
    private TextField txtInputSelectedText, txtInputSelectedImage, txtInputTicketName;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    public Label lblEventTitle;
    @FXML
    private StackPane profilePicturePane;
    @FXML
    private Button saveButton, backButton, btnSetupTicketDesign, btnReplaceMissingTicketInfo, btnAddID;
    @FXML
    private MFXToggleButton toggleButtonType;
    @FXML
    private Tooltip toolTipPlaceholder;
    private double xOffset = 0;
    private double yOffset = 0;
    private Label lblEventName, lblEventStartDateTime, lblEventEndDateTime, lblEventNotes, lblEventLocationGuide, lblEventLocation;     // Automatic generated labels
    private EMSCoordinator emsCoordinator;
    private EMSTicketMain emsTicketMain;
    private EventModel eventModel;
    private final UserModel userModel;
    private final TicketModel ticketModel;
    private final ImageModel systemIMGModel;
    private final EventTicketsModel eventTicketsModel;
    private final GlobalTicketsModel globalTicketsModel;
    private final DisplayErrorModel displayErrorModel;

    private be.Event selectedEvent;
    private User currentUser;
    private Stage emsTicketMainStage;
    private boolean menuButtonVisible = false;
    private boolean cancelledNewTicket = false;
    private int isItLocalTicket = 1;
    private final List<Integer> imgListDeleteIfUpdate = new ArrayList<>();
    private Tickets currentTicket;
    private String type; // Do we update or delete
    private final Image defaultProfile = new Image("Icons/User_Icon.png");
    private final Image mainIcon = new Image ("/Icons/mainIcon.png");
    private final Image picturePlaceholder = new Image ("/Icons/LoginBackground.png");
    private ArchivedEventModel archivedEventModel;
    private Node selectedNode;  // Global variable to store the selected Node

    private static final int EVENT_TICKET_WIDTH = 900;
    private static final int EVENT_TICKET_HEIGHT = 250;
    private static final int ONE_TIME_TICKET_WIDTH = 200;
    private static final int ONE_TIME_TICKET_HEIGHT = 300;

    public void setEMSTicketMainStage(Stage emsTicketMainStage) {
        this.emsTicketMainStage = emsTicketMainStage;
    }

    public void setEMSTicketMain(EMSTicketMain emsTicketMain) {this.emsTicketMain = emsTicketMain;}
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }
    void setType(String type) { this.type = type; }

    public EMSTicketDesigner() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        userModel = UserModel.getInstance();
        ticketModel = TicketModel.getInstance();
        eventModel = EventModel.getInstance();
        systemIMGModel = ImageModel.getInstance();
        eventTicketsModel = EventTicketsModel.getInstance();
        globalTicketsModel = GlobalTicketsModel.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toolTipPlaceholder.setText("Write these variable in a text-field. \n In the PDF version real data will replace it \n %f% = Name \n %l%  = Lastname \n %fl% = Full name\n");
        toolTipPlaceholder.setHideDelay(Duration.millis(1000000));
        btnAddID.setOnMouseEntered(event -> {
            double x = btnAddID.localToScreen(btnAddID.getBoundsInLocal()).getMinX();
            double y = btnAddID.localToScreen(btnAddID.getBoundsInLocal()).getMaxY();
            toolTipPlaceholder.show(btnAddID, x, y);
        });
        // Hide tooltip when mouse exits
        btnAddID.setOnMouseExited(event -> toolTipPlaceholder.hide());
        setupDragAndDrop();
        enableTextSelection();
        changeTicketDisplay();
        txtInputTicketName.textProperty().addListener((observable, oldValue, newValue) -> validateTicketName());
        Platform.runLater(() -> imageRotateSlider.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {   // Check if the pressed key is the Delete key
                deleteKeyPress();
            }
        }));
    }
    public void startupProgram() { //This setup program
        currentUser = userModel.getLoggedInUser();
        selectedEvent = emsCoordinator.getEventBeingUpdated();
        lblEventTitle.setText(selectedEvent.getEventName());
        menuButtonLoggedInUser.setText(currentUser.getUserName());
        if (currentUser.getProfileIMG() != null)   { //If user have a picture set it
            setProfilePicture(currentUser.getProfileIMG());
        }
        // Add a Listener to the 4 sliders their update size and rotation on text and image
        textfontSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double fontSize = newValue.doubleValue();
            // Calculate padding dynamically based on font size so remove gap
            if (selectedNode instanceof Label label) {
                label.setPadding(new Insets(-0.36 * fontSize, 0, -0.36 * fontSize, 0));
            }
            applyFontStyleChanges(newValue.doubleValue());
        });

        imageSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String formattedRotateValue = String.format(Locale.US, "%.2f", newValue.doubleValue());
            applyImageSizeChanges(Double.parseDouble(formattedRotateValue));
        });
        addSliderListener(textRotateSlider.valueProperty(), this::applyTextRotateChanges);
        addSliderListener(imageRotateSlider.valueProperty(), this::applyImageRotateChanges);
        currentTicket = ticketModel.getCurrentTicket();
        if (Objects.equals(type, "update"))   { // Mean we want look at a Ticket
            setupUpdateVersion();
        }
    }

    private void deleteKeyPress()   {
        if (selectedNode instanceof ImageView imageView) {
            if (imageView.getId() != null)
                try { // In update mode we must wait with delete IMG to the want update so they can go back
                    int id = Integer.parseInt(imageView.getId());
                    imgListDeleteIfUpdate.add(id);
                } catch (NumberFormatException ignored) {}
            }
        if (selectedNode != null) {
            ticketArea.getChildren().remove(selectedNode);
            txtInputSelectedText.clear();
            selectedNode = null;
        }
    }

    private void setupUpdateVersion()    {
        saveButton.setText("Update");
        setupTicketView(currentTicket.getTicketJSON(), ticketArea);
        txtInputTicketName.setText(currentTicket.getTicketName());
        txtInputTicketName.setDisable(true);
        toggleButtonType.setSelected(currentTicket.getIsILocal() != 1);
        toggleButtonType.setDisable(true);
        if (currentTicket.getIsILocal() == 0) {
            btnReplaceMissingTicketInfo.setDisable(true);
            btnSetupTicketDesign.setDisable(true);
        }
        isItLocalTicket = currentTicket.getIsILocal();
        changeTicketDisplay();
    }

    private void addSliderListener(DoubleProperty sliderProperty, Consumer<Double> changeHandler) {
        sliderProperty.addListener((observable, oldValue, newValue) -> {
            double snappedValue = newValue.doubleValue(); // Make it easy hit core position
            if (newValue.doubleValue() >= 87 && newValue.doubleValue() <= 93) {
                snappedValue = 90; // Snap to 90 if between 87 and 93
            } else if (newValue.doubleValue() >= 177 && newValue.doubleValue() <= 183) {
                snappedValue = 180; // Snap to 180 if between 177 and 183
            } else if (newValue.doubleValue() >= 267 && newValue.doubleValue() <= 273) {
                snappedValue = 270; // Snap to 270 if between 267 and 273
            }
            String formattedValue = String.format(Locale.US, "%.2f", snappedValue);
            changeHandler.accept(Double.parseDouble(formattedValue));
        });
    }

    private void applyImageSizeChanges(double newSize) {
        if (selectedNode instanceof ImageView imageView) {
            imageView.setFitWidth(newSize);  // Adjust the width of the image
            imageView.setFitHeight(newSize); // Adjust the height of the image
        }
    }

    private void applyImageRotateChanges(double newSize) {
        if (selectedNode instanceof ImageView imageView) {
            imageView.setRotate(newSize); // Set the rotation of the image
        }
    }

    public void validateTicketName(){
        if (!txtInputTicketName.getText().isEmpty()){
            if (Pattern.matches("[a-zæøåA-ZÆØÅ0-9\s*]{3,30}",txtInputTicketName.getText())){
                txtInputTicketName.setStyle("-fx-border-color: green;");
            }
            else {
                txtInputTicketName.setStyle("-fx-border-color: red;");
            }
        }
        else
            txtInputTicketName.setStyle("-fx-border-color: null");
    }

    @FXML
    private void btnAddGenerateQR() throws Exception {
        // Check if there's already an ImageView with 'isQRCode' property set to true
        if (checkForProperty(ticketArea, "isQRCode")) {
            displayErrorModel.displayErrorC("Only 1 QRCODE allowed");
            return; // Exit the method if such an ImageView exists
        }
        // Generate QR code image with specified data and error correction level
        BufferedImage qrCodeImage = BarCode.generateQRCodeImage("Placeholder every ticket will get a unique later", 100, 100);
        // Save QR code image to file named "QR.png"
        File outputFile = new File("tmpFiles/QR.png");
        ImageIO.write(qrCodeImage, "png", outputFile);
        // Load saved image into ImageView
        Image image = new Image(outputFile.toURI().toString());
        setupNewImage(image, "QR");
    }
    @FXML
    public void btnAddBarcode() throws IOException {
        // Check if there's already an ImageView with 'isBarcode' property set to true
        if (checkForProperty(ticketArea, "isBarcode")) {
            displayErrorModel.displayErrorC("Only 1 Barcode allowed");
            return; // Exit the method if such an ImageView exists
        }
        //Generate Barcode
        UUID uuid = UUID.randomUUID();
        BufferedImage barcodeImage = BarCode.generateCode128BarcodeImage(uuid,200, 100);
        File outputFile = new File("tmpFiles/Barcode.png");
        ImageIO.write(barcodeImage, "png", outputFile);

        Image image = new Image(outputFile.toURI().toString());
        setupNewImage(image, "BC");
    }
    @FXML
    private void btnAddID() {
        //TODO User can add used fx. seat row and number etc.
    }

    // Method to check if any nodes have the specified Properties
    private boolean checkForProperty(Pane ticketArea, String prop) {
        for (Node node : ticketArea.getChildren()) {
            if (node instanceof ImageView imageView) {
                Boolean isBarcode = (Boolean) imageView.getProperties().get(prop);
                if (Boolean.TRUE.equals(isBarcode)) {
                    return true; // Found an ImageView with the 'prop' property set to true
                }
            }
            if (node instanceof Label lbl) {
                Boolean isBarcode = (Boolean) lbl.getProperties().get(prop);
                if (Boolean.TRUE.equals(isBarcode)) {
                    return true; // Found an ImageView with the 'prop' property set to true
                }
            }
        }
        return false; // No ImageView with the 'prop' property set to true was found
    }

    @FXML // Button to clear everything in the ticket
    private void btnDeleteEverything() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("You will delete everything on the ticket");
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon); // Set the icon for the dialog window

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Node node : ticketArea.getChildren()) {
                if (node instanceof ImageView imageView) {
                    if (imageView.getId() != null)
                        try {
                            int id = Integer.parseInt(imageView.getId());
                            imgListDeleteIfUpdate.add(id);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        ticketArea.getChildren().clear();
    }

    // Method to update font style based on the selected text
    private void updateFontStyle(Label label) {
        boldCheckBox.setSelected(label.getFont().getStyle().contains("Bold"));
        italicCheckBox.setSelected(label.getFont().getStyle().contains("Italic"));
        underlineCheckBox.setSelected(label.isUnderline());
        textfontSizeSlider.setValue(label.getFont().getSize());
        colorPicker.setValue((Color) label.getTextFill());
    }

    @FXML
    private void btnAddText() { // Open dialog box and  add text to Ticket
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Waiting for Input");
        dialog.setHeaderText("Enter text to add to the Ticket:");
        dialog.setContentText(null);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::setupLabel);
    }

    private Label setupLabel(String text)  {
        Label label = new Label(text);
        setupNode(label);
        setSelectedNode(label);
        label.setMaxWidth(ticketArea.getWidth()); // Set maximum width to ticket area width
        label.setWrapText(true); // Enable text wrapping
        ticketArea.getChildren().add(label);
        label.setTextFill(Color.BLACK); //Only if you do default
        setDefaultFont(label); // Use default font
        // updateFontStyle(label); // Use same font as the selected before
        return label;
    }
    // Method to edit the label text
    private void editLabelText(Label label) {
        TextInputDialog dialog = new TextInputDialog(label.getText());
        dialog.setTitle("Edit Text");
        dialog.setHeaderText("Change the current text:");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(label::setText);
    }

    @FXML
    private void btnAddImage() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        // Set the owner window for the file chooser dialog
        Stage ownerStage = (Stage) profilePicture.getScene().getWindow();

        // Show the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(ownerStage);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            setupNewImage(image, "Normal");
        }
    }

    private void setupNewImage(Image image, String type) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(!Objects.equals(type, "QR") || !Objects.equals(type, "BC")); //If Image is QR the ratio should not be changeable to make sure it can be scanned
        if (Objects.equals(type, "QR"))   imageView.getProperties().put("isQRCode", true);
        if (Objects.equals(type, "BC")) {  imageView.getProperties().put("isBarcode", true); }
        setupNode(imageView);
        setSelectedNode(imageView);
        double quarterWidth = ticketArea.getWidth() * 0.25; // New img fill 25% of ticket width
        imageView.setFitWidth(quarterWidth);
        imageSizeSlider.setValue(quarterWidth);
        imageRotateSlider.setValue(imageView.getRotate());
        ticketArea.getChildren().add(imageView);
    }

    private void setupNode(Node node) {
        node.setOnMousePressed(this::handleMousePressed);
        node.setOnMouseDragged(this::handleMouseDragged);
        node.setOnMouseReleased(this::handleMouseReleased);
        if (node instanceof Label label) {
            label.setOnMouseClicked(event -> { // Handler if user want change image
                if (event.getClickCount() == 2) {
                    editLabelText(label);
                }
            });
        }
    }

    public void btnReplaceMissingTicketInfo() {
        if (!selectedEvent.getEventName().isEmpty() && (!checkForProperty(ticketArea, "isEventName")))    {
            setupEventName();
        }
        if (!selectedEvent.getEventStartDateTime().isEmpty() && (!checkForProperty(ticketArea, "isEventStartDateTime")))    {
            setupEventStartDateTime();
        }
        if (!Objects.equals(selectedEvent.getEventEndDateTime(), selectedEvent.getEventStartDateTime()) && (!checkForProperty(ticketArea, "isEventEndDateTime")))    {
            setupEventEndDateTime();
        }
        if (!selectedEvent.getEventNotes().isEmpty() && (!checkForProperty(ticketArea, "isEventNotes")))    {
            setupEventNotes();
        }
        if (selectedEvent.getLocationGuidance() != null && (!checkForProperty(ticketArea, "isEventLocationGuide")))    {
            setupEventLocationGuide();
        }
        if (!selectedEvent.getLocation().isEmpty() && (!checkForProperty(ticketArea, "isEventLocation")))    {
            setupEventLocation();
        }
    }

    public void btnSetupTicketDesign() { // We set up ticket with default info we just use missing cause first time all is missing
        btnReplaceMissingTicketInfo();
    }


    // TODO make it looks nice
    private void setupEventName(){
        lblEventName = setupLabel(selectedEvent.getEventName());
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, 43);
        lblEventName.setFont(defaultFont );
        lblEventName.setLayoutX(10);
        lblEventName.setLayoutY(0);
        lblEventName.setTextFill(Color.web("#108cf1")); // #FFFF00 represents blue color in hexadecimal
        lblEventName.getProperties().put("isEventName" , true);
    }

    private void setupEventStartDateTime(){
        lblEventStartDateTime = setupLabel(timeDateConverter(selectedEvent.getEventStartDateTime()));
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, 15);
        lblEventStartDateTime.setFont(defaultFont );
        lblEventStartDateTime.setLayoutX(10);
        lblEventStartDateTime.setLayoutY(55);
        lblEventStartDateTime.getProperties().put("isEventStartDateTime" , true);
    }
    private void setupEventEndDateTime(){
        lblEventEndDateTime = setupLabel(timeDateConverter(selectedEvent.getEventEndDateTime()));
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, 15);
        lblEventEndDateTime.setFont(defaultFont );
        lblEventEndDateTime.setLayoutX(135);
        lblEventEndDateTime.setLayoutY(55);
        lblEventEndDateTime.getProperties().put("isEventEndDateTime" , true);
    }
    private void setupEventNotes(){
        lblEventNotes = setupLabel(selectedEvent.getEventNotes());
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, 12);
        lblEventNotes.setFont(defaultFont );
        lblEventNotes.setLayoutX(10);
        lblEventNotes.setLayoutY(135);
        lblEventNotes.getProperties().put("isEventNotes" , true);
    }
    private void setupEventLocationGuide(){
        lblEventLocationGuide = setupLabel(selectedEvent.getLocationGuidance());
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, 16);
        lblEventLocationGuide.setFont(defaultFont );
        lblEventLocationGuide.setLayoutX(10);
        lblEventLocationGuide.setLayoutY(108);
        lblEventLocationGuide.getProperties().put("isEventLocationGuide" , true);
    }
    private void setupEventLocation(){
        lblEventLocation = setupLabel(selectedEvent.getLocation());
        Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, 18);
        lblEventLocation.setFont(defaultFont );
        lblEventLocation.setLayoutX(10);
        lblEventLocation.setLayoutY(80);
        lblEventLocation.getProperties().put("isEventLocation" , true);
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getSource() instanceof Node) {
            selectedNode = (Node) event.getSource();
            setSelectedNode(selectedNode);
            xOffset = event.getSceneX() - selectedNode.getLayoutX();
            yOffset = event.getSceneY() - selectedNode.getLayoutY();
            // Set the cursor to indicate that dragging is in progress
            selectedNode.setCursor(Cursor.CLOSED_HAND);
        }
    }
    public String timeDateConverter( String timeDate)   { // Format LocalDateTime object to desired format
        LocalDateTime dateTime = LocalDateTime.parse(timeDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));   
    }

    private void handleMouseDragged(MouseEvent event) { //TODO Stuff can be place, where it not should and cannot all place where it should
        if (selectedNode != null) {
            double newX = event.getSceneX() - xOffset;
            double newY = event.getSceneY() - yOffset;

            // Adjust the position of the selected node to stay within the bounds of the ticket area
            newX = Math.max(0, Math.min(newX, ticketArea.getWidth() - selectedNode.getLayoutBounds().getWidth()));
            newY = Math.max(0, Math.min(newY, ticketArea.getHeight() - selectedNode.getLayoutBounds().getHeight()));

            selectedNode.setLayoutX(newX);
            selectedNode.setLayoutY(newY);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        Node intersectedNode = event.getPickResult().getIntersectedNode();
            // Check if the mouse was released within the ticketArea or deleteDropRelease
        if (intersectedNode == ticketArea) {
            // If released inside the ticketArea, adjust the selectedNode's position
            selectedNode.setLayoutX(event.getSceneX() - xOffset);
            selectedNode.setLayoutY(event.getSceneY() - yOffset);
        } else if (intersectedNode == deleteDropReleaseIMG || intersectedNode == deleteDropRelease) {
            // If dropped into deleteDropRelease, remove the node and clear selections
            if (selectedNode != null) {
                if (selectedNode instanceof ImageView img) {
                    try { //We add the IMG to a list, so we can delete it later if we update (Update mode)
                        int id = Integer.parseInt(img.getId());
                        imgListDeleteIfUpdate.add(id);
                    } catch (NumberFormatException ignored) {
                    }
                }
                ticketArea.getChildren().remove(selectedNode);
                txtInputSelectedText.clear();
                txtInputSelectedImage.clear();
                selectedNode = null;
            }
        }
    }

    private void setupDragAndDrop() {
        // Drag-over for ticketArea and deleteDropRelease
        EventHandler<DragEvent> dragOverHandler = event -> {
            if (event.getGestureSource() != deleteDropRelease && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        };

        ticketArea.setOnDragOver(dragOverHandler);
        deleteDropRelease.setOnDragOver(dragOverHandler);

        // Drag-dropped for ticketArea and deleteDropRelease
        EventHandler<DragEvent> dragDroppedHandler = event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                Node node = db.hasString() ? new Text(db.getString()) : null;
                if (node != null) {
                    if (event.getSource() == deleteDropRelease) {
                        ticketArea.getChildren().remove(node);
                    } else if (event.getSource() == ticketArea) {
                        ticketArea.getChildren().add(node);
                    }
                    event.setDropCompleted(true);
                }
            }
            event.consume();
        };

        ticketArea.setOnDragDropped(dragDroppedHandler);
        deleteDropRelease.setOnDragDropped(dragDroppedHandler);
    }




    @FXML   // When user use checkbox update style
    private void boldItalicUnderlineChanged() {
        applyFontStyleChanges(textfontSizeSlider.getValue());
    }

    @FXML
    private void colorPicker() { // Get the selected color and set it
        if (selectedNode instanceof Label label) {
            Color selectedColor = colorPicker.getValue();
            if (!label.getTextFill().equals(selectedColor)) {
                label.setTextFill(selectedColor); // Prevent from using wrong color
            }
        }
    }
    @FXML //Get the selected rotate and set it
    private void applyTextRotateChanges(double rotateValue) {
            if (selectedNode instanceof Label lbl) {
                lbl.setRotate(rotateValue);
        }
    }

    private void setDefaultFont(Label label) {
        Font defaultFont = Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, 12);
        label.setFont(defaultFont);
        label.setTextFill(Color.BLACK);
        label.setUnderline(false);
        label.setWrapText(true); // Enable text wrapping for labels
    }


    // Apply font style changes to the selected text
    private void applyFontStyleChanges(double fontSize) {
        if (selectedNode != null && selectedNode instanceof Label label) {
            colorPicker.setValue((Color) label.getTextFill());
            Font font = Font.font(
                    label.getFont().getFamily(),
                    boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                    italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSize
            );
            label.setFont(font);
            label.setUnderline(underlineCheckBox.isSelected());
            label.setTextFill(colorPicker.getValue());
            label.setMaxWidth(ticketArea.getWidth()); // Set maximum width to ticket area width
            label.setWrapText(true);
        }
    }

    // Update method parameters and handle different node types
    private void setSelectedNode(Node node) {
        selectedNode = node;
        if (node instanceof Label label) {
            updateFontStyle(label);
            txtInputSelectedText.setText(label.getText());
            txtInputSelectedImage.setText(null);
            txtInputSelectedText.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, 12));
            textRotateSlider.setValue(label.getRotate());
        } else if (node instanceof ImageView imageView) {
            if (imageView.getProperties().containsKey("imageCauseWeUpdate")) { // Use containsKey() to check if the property exists
                txtInputSelectedImage.setText("IMG.png ");
            } else {
                String imageName = getImageNameFromURL(imageView.getImage().getUrl());
                txtInputSelectedImage.setText(imageName);
            }
            txtInputSelectedText.setText(null);
            imageSizeSlider.setValue(imageView.getFitWidth());
            imageRotateSlider.setValue(imageView.getRotate());
        }
    }
    private String getImageNameFromURL(String imageURL) { // Extract the image name from the URL
        String[] parts = imageURL.split("/");
        return parts[parts.length - 1];
    }

    // Method to enable text selection in ticketArea
    private void enableTextSelection() {
        ticketArea.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.setOnMouseClicked(event -> {
                    selectedNode = label; // Update selectedText variable
                    updateFontStyle(label); // Update font style when text is clicked
                    txtInputSelectedText.setText(label.getText());
                    txtInputSelectedText.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, 12));
                });
            }
        });
    }

    // This method get us the Image from the database and inset it to the Ticket
    private Image getImageByID(String imageViewId) {
        System.out.println("Get IMG from DB " + imageViewId);
        try {
           return systemIMGModel.readSystemIMG(Integer.parseInt(imageViewId)).getImage();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Could not read image");
        }
        return picturePlaceholder;
    }
    @FXML // Here create the Ticket object and send it through layer to database
    private void saveButton() {
        String ticketName = txtInputTicketName.getText();
        if (ticketName.isEmpty()) {
            displayErrorModel.displayErrorC("Missing Intern Name");
            return;
            /*} else if (checkNodesOutsideTicketArea()) {
            displayErrorModel.displayErrorC("Oops! It looks like some of your text or images extend beyond the ticket boundaries.\n Please ensure all content stays within the ticket area to avoid cropping or printing issues.");
            return; */
        } else if (ticketArea.getChildren().isEmpty()) {
            displayErrorModel.displayErrorC("Missing Design in Ticket");
            return;
        }
        if (isItLocalTicket == 1) {

            if (!checkForProperty(ticketArea, "isEventName")) {
                displayErrorModel.displayErrorC("Missing Event Name on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isEventLocation")) {
                displayErrorModel.displayErrorC("Missing Event Location of Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isEventNotes")) {
                displayErrorModel.displayErrorC("Missing Event Notes on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isEventLocationGuide") && selectedEvent.getLocationGuidance() != null){
                displayErrorModel.displayErrorC("Missing Location Guidance on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isEventStartDateTime")) {
                displayErrorModel.displayErrorC("Missing Start Date & Time on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isEventEndDateTime") && !Objects.equals(selectedEvent.getEventEndDateTime(), selectedEvent.getEventStartDateTime())){
                displayErrorModel.displayErrorC("Missing End Date & Time on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isBarcode")) {
                displayErrorModel.displayErrorC("No Barcode on Ticket");
                return;
            }
            if (!checkForProperty(ticketArea, "isQRCode")) {
                displayErrorModel.displayErrorC("No QR Code on Ticket");
                return;
            }
        }
        if (!checkForProperty(ticketArea, "isQRCode") && !checkForProperty(ticketArea, "isBarcode")) {
            displayErrorModel.displayErrorC("Missing Barcode or QR code");
            return;
        }

        int totalCharacters = getTotalCharactersIMGDB();
        String json = TicketSerializerRecreate.serializeTicketAreaToJson(ticketArea);
        //TODO Save it in Database Json can max be 4000 sign so make ticket max or make Json 2
        //We check that the JSON doesnt fill more then 4000 signs and with space for the ID the image will give when this is true
        if (json.length()+totalCharacters >= 4000){
            displayErrorModel.displayErrorC("Too many things in your ticket at this moment " + json.length() + "/4000");
            return;
        }
        // Show loading animation
        showLoadingAnimation();
        cancelledNewTicket = false;
        ImageCompressor.enableCompressor();
        saveButton.setDisable(true);
        backButton.setText("Cancel");


        // Create a background task
        new Thread(() -> {
            try {
                saveImageAndSetID(); // Perform long-running operations here
                Platform.runLater(() -> updateUIAfterImageProcessing(ticketName)); // Update UI after processing
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (cancelledNewTicket) {return;}
                    enableGUIObject();
                    displayErrorModel.displayErrorC("Could not save image and set ID");
                });
            }
        }).start();
    }

    private ProgressIndicator showLoadingAnimation() { // This show an indicator with lbl under while image got compressed etc.
        ProgressIndicator progressIndicator = new ProgressIndicator();
        loadingBox = new VBox();
        Label lbl = new Label("Please wait while the system creates the ticket.");
        lbl.setStyle("-fx-font-size: 18px");
        lbl.setWrapText(true);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(5);
        loadingBox.getChildren().addAll(progressIndicator, lbl);
        anchorPane.getChildren().add(loadingBox);
        progressIndicator.setMinSize(250, 250);
        progressIndicator.setMaxSize(250, 250);
        progressIndicator.setPrefSize(250, 250);
        setLoadingBoxPosition(loadingBox);

        // Update position when anchorPane size changes
        anchorPane.widthProperty().addListener((obs, oldWidth, newWidth) -> setLoadingBoxPosition(loadingBox));
        anchorPane.heightProperty().addListener((obs, oldHeight, newHeight) -> setLoadingBoxPosition(loadingBox));
        eventHBoxSection.setVisible(false);
        return progressIndicator;
    }

    private void setLoadingBoxPosition(VBox loadingBox) {
        Platform.runLater(() -> {
            double centerX = (eventHBoxSection.getWidth() / 2 - (double) 370 / 2);
            double centerY = eventHBoxSection.getHeight() / 2;
            loadingBox.setLayoutX(centerX);
            loadingBox.setLayoutY(centerY);
        });
    }

    private void updateUIAfterImageProcessing(String ticketName) {
        Platform.runLater(() -> {
            try {
                if (cancelledNewTicket) {return;} // If user want to cancel
                String jsonUpdated = TicketSerializerRecreate.serializeTicketAreaToJson(ticketArea);

                if (Objects.equals(type, "update"))   {
                    Tickets updatedTicket = new Tickets(currentTicket.getTicketID(), currentTicket.getTicketQuantity(), currentTicket.getTicketName(), jsonUpdated, currentTicket.getIsILocal());
                    ticketModel.updateTicket(updatedTicket);
                    for (Integer i : imgListDeleteIfUpdate)   {
                        systemIMGModel.deleteSystemIMG(i);
                    }
                    backButton.setText("Back");
                    backButton();
                    return;
                }
                Tickets newTicket = new Tickets(0, 0, ticketName, jsonUpdated, isItLocalTicket);

                if (isItLocalTicket == 1)   {
                    ticketModel.createNewTicket(newTicket);
                }
            if (isItLocalTicket == 0)   { // Mean it's a global ticket
                    globalTicketsModel.addGlobalTickets(newTicket);
                    backButton.setText("Back");
                    backButton();
                }
            else {
                eventTicketsModel.addTicketsToEvent(newTicket, selectedEvent);
                backButton.setText("Back");
                backButton(); }
            } catch (Exception e) {
                displayErrorModel.displayErrorC("Could not save image and set ID");
            } finally {
                anchorPane.getChildren().remove(loadingBox); // Remove loading animation
            }
        });
    }

    // Here we calculate how many character image are going to use in the database
    private int getTotalCharactersIMGDB() {
        int totalCharacters;
        try {
            int value = systemIMGModel.getNextIDSystemIMG();
            int numberOfImages = ticketArea.getChildren().size(); // Number of images
            StringBuilder concatenatedIds = new StringBuilder();

            for (int i = 0; i < numberOfImages; i++) {
                concatenatedIds.append(value + i);
            }
            totalCharacters = (concatenatedIds.length()-(numberOfImages));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return totalCharacters;
    }

    // Here we save each img in ticket and give the image instance right ID
    public void saveImageAndSetID() {
        for (Node node : ticketArea.getChildren()) {
            if (node instanceof ImageView imageView) {
                String newId;
                if (imageView.getId() != null)  { // Means image is already on DB (Update mode)
                    continue;
                }
                try {
                    String imageUrl = imageView.getImage().getUrl();
                    if (imageUrl != null) {
                        // Set the ID for the image view with original image
                        Image compressedImage = ImageCompressor.compressImageTo500KB(imageView.getImage());
                        if (compressedImage != null) {
                            newId = String.valueOf(systemIMGModel.createSystemIMG(compressedImage));
                            imageView.setId(newId);
                        }
                    } else {
                        displayErrorModel.displayErrorC("One of the pictures is corrupt");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    //This method set up a JSON in a given pane
    public void setupTicketView(String json, Pane paneName) {
        paneName.getChildren().clear(); // We clear the area, then we recreate it from the JSON
        Pane recreatedPane = TicketSerializerRecreate.cloneTicketAreaFromJson(json);
        // When its recreate we inset right Image from database
        for (Node node : recreatedPane.getChildren()) {
            setupNode(node);
            if (node instanceof ImageView imageView) {
                String id = imageView.getId();
                Image image = getImageByID(id);
                if (image != null) {
                    imageView.setImage(image);
                    imageView.getProperties().put("imageCauseWeUpdate", true);
                    imageView.setPreserveRatio(!Objects.equals(type, "QR") || !Objects.equals(type, "BC")); //If Image is QR the ratio should not be changeable
                }
            }
            if (node instanceof Label lbl) { // Set maximum width to ticket area width
                if (currentTicket.getIsILocal() == 1)   {
                    lbl.setMaxWidth(EVENT_TICKET_WIDTH);
                }
                else {
                    lbl.setMaxWidth(ONE_TIME_TICKET_HEIGHT);
                }
            }
        }
        paneName.getChildren().addAll(recreatedPane.getChildren());
    }


    private void enableGUIObject()    {
        saveButton.setDisable(false);
        eventHBoxSection.setVisible(true);
        backButton.setText("Back");
        anchorPane.getChildren().remove(loadingBox);
    }

    @FXML
    private void backButton() {
        // Bring the emsCoordinatorStage to front
        if (Objects.equals(backButton.getText(), "Cancel")) {
            cancelledNewTicket = true; // So other method in class know it cancel time
            ImageCompressor.cancelCompressor(); // Tell IMG compressor to stop compress IMG
            enableGUIObject();
            for (Node node : ticketArea.getChildren()) { //If there was added IMG removed them
                if (node instanceof ImageView imageView) {
                    String imageId = imageView.getId();
                    if (imageId != null && !"0".equals(imageId)) {
                        try {
                            systemIMGModel.deleteSystemIMG(Integer.parseInt(imageId));
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("Could not delete image");
                        }
                    }
                }
            }
            return;
        }
        emsTicketMainStage.show();
        emsCoordinator.startupProgram();
        emsTicketMain.recreateTableview();
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
    private void openArchivedEvents() {
        //TODO Make a display for archived events so the coordinators can get inspired by past events
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
            controller.setEMSTicketDesigner(this);
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

    @FXML
    private void toggleButtonType() {
        boolean previousSelection = isItLocalTicket == 0;
        if (toggleButtonType.isSelected()) {
            warningBox(previousSelection);
        } else {
            warningBox(previousSelection);
        }
    }

    private void warningBox(boolean previousSelection){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Warning, changing ticket type will wipe current ticket");
        alert.setContentText("Are you ok with this?");
        // Set the icon for the dialog window
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ticketArea.getChildren().clear();
            if (isItLocalTicket == 0) {
                isItLocalTicket = 1;
                btnSetupTicketDesign.setDisable(false);
                btnReplaceMissingTicketInfo.setDisable(false);
            }
            else if (isItLocalTicket == 1) {
                isItLocalTicket = 0;
                btnSetupTicketDesign.setDisable(true);
                btnReplaceMissingTicketInfo.setDisable(true);
            }
            changeTicketDisplay();

        } else {
            // Revert toggle button selection to previous state
            toggleButtonType.setSelected(previousSelection);
        }
    }

    private void changeTicketDisplay(){
        if (isItLocalTicket == 1)
            ticketArea.setPrefSize(EVENT_TICKET_WIDTH, EVENT_TICKET_HEIGHT);

        if (isItLocalTicket == 0)
            ticketArea.setPrefSize(ONE_TIME_TICKET_WIDTH, ONE_TIME_TICKET_HEIGHT);
    }

    @FXML
    private void btnToggleButtonType() {
        toggleButtonType.setSelected(!toggleButtonType.isSelected());
        toggleButtonType();
    }

    @FXML
    private void btnJSON(ActionEvent actionEvent) {
        System.out.println(TicketSerializerRecreate.serializeTicketAreaToJson(ticketArea));
    }
}
