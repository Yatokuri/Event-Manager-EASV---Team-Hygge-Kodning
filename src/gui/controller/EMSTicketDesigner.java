package gui.controller;

import be.User;
import gui.model.*;
import gui.util.TicketSerializerRecreate;
import javafx.application.Platform;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EMSTicketDesigner implements Initializable {
    @FXML
    private MenuButton menuButtonLoggedInUser;
    @FXML
    private ImageView profilePicture;
    @FXML
    private Pane ticketArea, ticketAreaClone;
    @FXML
    private CheckBox boldCheckBox, italicCheckBox, underlineCheckBox;
    @FXML
    private Slider textfontSizeSlider, textRotateSlider, imageSizeSider, imageRotateSlider;
    @FXML
    private HBox deleteDropRelease;
    @FXML
    private TextField txtInputSelectedText, txtInputSelectedImage, txtJsonInput, txtInputTicketName;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    public Label lblEventTitle;
    @FXML
    private StackPane profilePicturePane;
    private double xOffset = 0;
    private double yOffset = 0;

    private EMSCoordinator emsCoordinator;
    private EventModel eventModel;
    private UserModel userModel;
    private TicketModel ticketModel;
    private final DisplayErrorModel displayErrorModel;

    private be.Event selectedEvent;
    private User currentUser;
    private Stage emsTicketMainStage;
    private boolean menuButtonVisible = false;

    private final Image defaultProfile = new Image("Icons/User_Icon.png");
    private final Image mainIcon = new Image ("/Icons/mainIcon.png");
    private final Image picturePlaceholder = new Image ("/Icons/LoginBackground.png");
    private ArchivedEventModel archivedEventModel;
    private Node selectedNode;  // Global variable to store the selected Node

    public void setEMSTicketMainStage(Stage emsTicketMainStage) {
        this.emsTicketMainStage = emsTicketMainStage;
    }
    public void setEMSCoordinator(EMSCoordinator emsCoordinator) {
        this.emsCoordinator = emsCoordinator;
    }

    public EMSTicketDesigner() throws Exception {
        displayErrorModel = new DisplayErrorModel();
        userModel = UserModel.getInstance();
        ticketModel = TicketModel.getInstance();
        eventModel = EventModel.getInstance();
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
                label.setPadding(new Insets(-0.37 * fontSize, 0, -0.35 * fontSize, 0));
            }
            applyFontStyleChanges(newValue.doubleValue());
        });
        textRotateSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String formattedRotateValue = String.format(Locale.US, "%.2f", newValue.doubleValue());
            applyRotateChanges(Double.parseDouble(formattedRotateValue));
        });
        imageSizeSider.valueProperty().addListener((observable, oldValue, newValue) -> applyImageSizeChanges(newValue.doubleValue()));
        imageRotateSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String formattedRotateValue = String.format(Locale.US, "%.2f", newValue.doubleValue());
            applyImageRotateChanges(Double.parseDouble(formattedRotateValue));
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDragAndDrop();
        enableTextSelection();
    }

    @FXML
    private void btnAddGenerateQR() {
        //TODO User can add Image move, resize and rotate them and generate automatic
    }
    @FXML
    private void btnAddID() {
        //TODO User can add used fx. seat row and number etc.
    }

    @FXML // Button to clear selected nodes
    private void btnDeleteSelectedNote() {
        if (selectedNode != null) {
            ticketArea.getChildren().remove(selectedNode);
            txtInputSelectedText.clear();
            selectedNode = null;
        }
    }

    @FXML // Button to clear everything in the ticket
    private void btnDeleteEverything(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("You will everything on the ticket");
        alert.setContentText("Are you ok with this?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon); // Set the icon for the dialog window

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ticketArea.getChildren().clear();
        }
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
        dialog.setHeaderText(null);
        dialog.setContentText("Enter text to add to the Ticket:");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(mainIcon);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            Label label = new Label(text);
            setupNode(label);
            setSelectedNode(label);
            label.setMaxWidth(ticketArea.getWidth()); // Set maximum width to ticket area width
            label.setWrapText(true); // Enable text wrapping
            ticketArea.getChildren().add(label);
        });
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
            ImageView imageView = new ImageView(image);
            setupNode(imageView);
            setSelectedNode(imageView);

            // Assuming you want to maintain the aspect ratio but fit the image within certain bounds:
            double maxSize = 100; // Example max size for the slider or based on your UI design
            double scale = Math.min(maxSize / image.getWidth(), maxSize / image.getHeight());
            imageView.setFitWidth(image.getWidth() * scale);
            imageView.setFitHeight(image.getHeight() * scale);
            imageSizeSider.setValue(maxSize * scale);

            imageRotateSlider.setValue(imageView.getRotate());
            ticketArea.getChildren().add(imageView);
        }
    }

    private void setupNode(Node node) {
        node.setOnMousePressed(this::handleMousePressed);
        node.setOnMouseDragged(this::handleMouseDragged);
        node.setOnMouseReleased(this::handleMouseReleased);
        if (node instanceof Label label) {
            setDefaultFont(label); // Use default font
           // updateFontStyle(label); // Use same font as the selected before
        }
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
        } else if (intersectedNode == deleteDropRelease && selectedNode != null) {
            // If dropped into deleteDropRelease, remove the node and clear selections
            ticketArea.getChildren().remove(selectedNode);
            txtInputSelectedText.clear();
            txtInputSelectedImage.clear();
            selectedNode = null;
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
        if (selectedNode instanceof Label) {
            Color selectedColor = colorPicker.getValue();
            ((Label) selectedNode).setTextFill(selectedColor);
        }
    }

    @FXML //Get the selected rotate and set it
    private void applyRotateChanges(double rotateValue) {
        if (selectedNode != null) {
            selectedNode.setRotate(rotateValue);
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
        } else if (node instanceof ImageView imageView) {
            String imageName = getImageNameFromURL(imageView.getImage().getUrl());
            txtInputSelectedImage.setText(imageName);
            txtInputSelectedText.setText(null);
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
        //TODO make the methods through layer there take image with the ID from db
        System.out.println("Get IMG from DB " + imageViewId);
        return picturePlaceholder;
    }

    @FXML // Here create the Ticket object and send it through layer to database
    private void saveButton(ActionEvent actionEvent) {
        for (Node node : ticketArea.getChildren()) {  // We go through all image
            if (node instanceof ImageView imageView) {
                /* String newId = ticketModel.getNextIdFromDBAndSave(); //TODO Method to set next Image ID and save Image in db before we do JSON
                 imageView.setId(newId)); */
            }
        }
        String json = TicketSerializerRecreate.serializeTicketAreaToJson(ticketArea);
        String ticketName = txtInputTicketName.getText();
        //TODO Save it in Database
    }
    @FXML // Only to test stuff - Don't Delete
    private void btnCreateCheckJson() {
        btnCreateJson();
        btnCheckJson();
        txtJsonInput.clear();
    }
    @FXML //Only to test stuff - Don't Delete
    private void btnCreateJson() { //Here we create JSON of TicketArea and print to Console
        int newId = 0;
        for (Node node : ticketArea.getChildren()) {  // Assign IDs to all ImageView nodes in ticketArea
            if (node instanceof ImageView imageView) {
                //String newId = ticketModel.getIdFromData(); //TODO see saveButton
                imageView.setId(String.valueOf(newId));
                newId++;
            }
        }
        String json = TicketSerializerRecreate.serializeTicketAreaToJson(ticketArea);
        txtJsonInput.setText(json);
        System.out.println(json);
    }
    @FXML //Only to test stuff - Don't Delete
    private void btnCheckJson() { //We take JSOn from input and create the Ticket ticketAreaClone from it
        if (txtJsonInput.getText().isEmpty())   {return;}
        ticketAreaClone.getChildren().clear(); // We clear the area, then we recreate it from the JSON
        Pane recreatedPane = TicketSerializerRecreate.cloneTicketAreaFromJson(txtJsonInput.getText());
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
        ticketAreaClone.getChildren().addAll(recreatedPane.getChildren());
    }

    @FXML
    private void backButton() {
        // Bring the emsCoordinatorStage to front
        emsTicketMainStage.show(); //1
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
}
