package gui.controller;

import be.User;
import gui.model.DisplayErrorModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EMSAdminCoordinatorManager implements Initializable {
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnAddUser;


    @FXML
    private TableView<User> tblUser;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, Void> colRemove;
    private boolean isBoxVisible = false;

    private UserModel userModel;
    @FXML
    private VBox animatedSignUpBox;
    private Stage primaryStage;
    @FXML
    private MFXTextField txtInputUsername, txtInputPassword;

    public EMSAdminCoordinatorManager() {
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    private DisplayErrorModel displayErrorModel;

    public void startupProgram() {
        displayErrorModel = new DisplayErrorModel();
        colRemove.setCellFactory(ButtonCell.forTableColumn(userModel));
        // Custom cell factory for the colUsername column so we can do "● Name"
        colUsername.setCellFactory(column -> {
            return new TableCell<User, String>() {
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
                    }
                }
            };
        });

        // Add data to the table we don't want Admin
        ObservableList<User> userList = userModel.getObsUsers().filtered(user -> user.getUserAccessLevel() != 2);
        tblUser.setItems(userList);

        // Listener for height changes
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            Point2D point2D = btnAddUser.localToScene(btnAddUser.getWidth() / 2, btnAddUser.getHeight() / 2);
            // Set the Y coordinate of animatedSignUpBox to the bottom of the stage
            animatedSignUpBox.setLayoutY(primaryStage.getHeight() - animatedSignUpBox.getHeight() - 100);
            animatedSignUpBox.setLayoutX(point2D.getX()-27); // Set X coordinate same as above
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the table with the specified columns
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
    }


    public void btnAddUser(ActionEvent actionEvent) {
        if (!isBoxVisible) {
            enableAddUser();
        } else {
            disableAddUser();
        }
    }

    public void enableAddUser() {
        // Create TranslateTransition to animate the box
        TranslateTransition transition = new TranslateTransition(Duration.seconds(2.5), anchorPane);
        transition.setByY(-25);
        transition.play();
        animatedSignUpBox.setVisible(true);
        isBoxVisible = true;
    }


    public void disableAddUser() {
        // Create TranslateTransition to animate the box back down
        TranslateTransition transition = new TranslateTransition(Duration.seconds(2.5), anchorPane);
        transition.setByY(25);
        transition.setOnFinished(event -> {
            // Hide the box after the animation completes
            animatedSignUpBox.setVisible(false);
        });
        transition.play();
        isBoxVisible = false;
    }


    public void btnPreviousWindow() {
        Stage parent = (Stage) tblUser.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void btnConfirmNewUser() {
        String passwordInput = txtInputPassword.getText();
        String usernameInput = txtInputUsername.getText();

        // Check if the username already exists
        for (User user : userModel.getObsUsers()) {
            if (user.getUserName().equals(usernameInput)) {
                displayErrorModel.displayErrorC("Username already exists");
                txtInputUsername.clear();
                return;
            }
        }

        if (usernameInput.isEmpty() && passwordInput.isEmpty()) {
            displayErrorModel.displayErrorC("Missing username or password");
            return;
        } else if (usernameInput.isEmpty()) {
            displayErrorModel.displayErrorC("Missing username");
            return;
        } else if (passwordInput.isEmpty()) {
            displayErrorModel.displayErrorC("Missing password");
            return;
        }


        User newUser = new User(usernameInput, passwordInput, 1);
        try {
            userModel.createNewUser(newUser);
            txtInputUsername.clear();
            txtInputPassword.clear();
            disableAddUser();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("User not created try again");
        }
    }

    @FXML
    private void btnCancelNewUser(ActionEvent actionEvent) {
        disableAddUser();
    }

    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final javafx.scene.control.Button deleteButton;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");
        public ButtonCell(UserModel userModel) {
            this.displayErrorModel = new DisplayErrorModel();

            deleteButton = new javafx.scene.control.Button("- ");
            deleteButton.setPrefWidth(20); // Set preferred width
            deleteButton.setPrefHeight(20); // Set preferred height
            deleteButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof User user) {
                    if (user.getUserAccessLevel() == 2) { // Prevent admin from removing itself
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("You will delete user " + user.getUserName());
                    alert.setContentText("Are you ok with this?");
                    // Set the icon for the dialog window
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(mainIcon);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            userModel.deleteUser(user); // We delete the User
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
                if (rowData instanceof User user) {
                    if (user.getUserAccessLevel() != 2) {
                        setGraphic(deleteButton);
                    } else {
                        setGraphic(null); // Don't show the button if access level is 2
                    }
                }
            }
        }
        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn(UserModel userModel) {
            return param -> new ButtonCell<>(userModel);
        }
    }
}
