package gui.controller;

import be.User;
import gui.model.DisplayErrorModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.Objects;
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
    private TableColumn<User, Void> colRemove, colResetPassword;
    private User currentUser;
    private UserModel userModel;
    @FXML
    private VBox animatedSignUpBox, animatedResetBox;
    private Stage primaryStage;
    @FXML
    private MFXTextField txtInputUsername, txtInputPassword, txtInputNewPassword, txtInputNewPasswordConfirm;
    @FXML
    private Label lblNewPasswordUsername;

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
        colRemove.setCellFactory(ButtonCell.forButton(userModel, this));
        colResetPassword.setCellFactory(ButtonCell.forButton(userModel, this));
        // Custom cell factory for the colUsername column so we can do "● Name"
        colUsername.setCellFactory(column -> new TableCell<>() {
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
        });

        // Add data to the table we don't want Admin
        ObservableList<User> userList = userModel.getObsUsers().filtered(user -> user.getUserAccessLevel() != 2);
        tblUser.setItems(userList);

        // Listener for height changes
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            Point2D point2D = btnAddUser.localToScene(btnAddUser.getWidth() / 2, btnAddUser.getHeight() / 2);
            // Set the Y coordinate of animatedSignUpBox to the bottom of the stage
            animatedSignUpBox.setLayoutY(primaryStage.getHeight() - animatedSignUpBox.getHeight() + 100);
            animatedSignUpBox.setLayoutX(point2D.getX()-27); // Set X coordinate same as above
        });
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            Point2D point2D = btnAddUser.localToScene(btnAddUser.getWidth() / 2, btnAddUser.getHeight() / 2);
            // Set the Y coordinate of animatedResetBox to the bottom of the stage
            animatedResetBox.setLayoutY(primaryStage.getHeight() - animatedResetBox.getHeight() + 100);
            animatedResetBox.setLayoutX(point2D.getX()-27); // Set X coordinate same as above
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the table with the specified columns
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        tblUser.setPlaceholder(new Label("No user found"));
    }


    public void btnAddUser() {
        if (isTransitionInProgress) {
            return;
        }
        isTransitionInProgress = true; // Transition starts
        if (!animatedSignUpBox.isVisible()) {
            if (animatedResetBox.isVisible())  {
                disableResetPassword();
                PauseTransition pause = new PauseTransition(Duration.millis(250));
                pause.setOnFinished(event -> {
                    enableAddUser();
                    isTransitionInProgress = false; // Transition ends
                });
                pause.play();
            } else {
                enableAddUser();
            }
        } else {
            disableAddUser();
        }
    }

    public void enableAddUser() {
        isTransitionInProgress = true; // Transition starts
        // Create TranslateTransition to animate the box
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.80), animatedSignUpBox);
        transition.setByY(-200);
        transition.play();
        animatedSignUpBox.setVisible(true);
        animatedSignUpBox.setLayoutX(17);
        animatedSignUpBox.setLayoutY(primaryStage.getHeight() - animatedSignUpBox.getHeight() + 100);
        // Set a handler for when the transition finishes to mark it as complete
        transition.setOnFinished(event -> isTransitionInProgress = false); // Transition ends
    }


    public void disableAddUser() {
        isTransitionInProgress = true; // Transition starts
        // Create TranslateTransition to animate the box back down
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), animatedSignUpBox);
        transition.setByY(200);
        // Set a handler for when the transition finishes
        transition.setOnFinished(event -> {
            // Hide the box after the animation completes
            animatedSignUpBox.setVisible(false);
            animatedSignUpBox.setLayoutX(17);
            animatedSignUpBox.setLayoutY(primaryStage.getHeight() - animatedSignUpBox.getHeight() + 100);
            isTransitionInProgress = false; // Transition ends
        });
        transition.play();
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
    private boolean isTransitionInProgress = false;

    private void tblResetPassword(User user) {
        if (isTransitionInProgress) {
            return;
        }
        isTransitionInProgress = true; // Transition starts
        if (!animatedResetBox.isVisible()) {
            if (animatedSignUpBox.isVisible())  {
                disableAddUser();
                PauseTransition pause = new PauseTransition(Duration.millis(250));
                pause.setOnFinished(event -> {
                    enableResetPassword();
                    isTransitionInProgress = false; // Transition ends
                });
                pause.play();
            } else {
                enableResetPassword();
            }
            currentUser = user;
            lblNewPasswordUsername.setText(currentUser.getUserName());
        } else {
            currentUser = null;
            disableResetPassword();
        }
    }



    public void enableResetPassword() { // Here the reset password get showed up with animation
        isTransitionInProgress = true; // Transition starts
        // Create TranslateTransition to animate the box up
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.80), animatedResetBox);
        transition.setByY(-200);
        transition.play();
        animatedResetBox.setVisible(true);
        animatedResetBox.setLayoutX(17);
        animatedResetBox.setLayoutY(primaryStage.getHeight() - animatedResetBox.getHeight() + 100);
        transition.setOnFinished(event -> isTransitionInProgress = false); // Transition ends
    }


    public void disableResetPassword() { // Here the reset password disappear up with animation
        isTransitionInProgress = true; // Transition starts
        // Create TranslateTransition to animate the box back down
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.50), animatedResetBox);
        transition.setByY(200);
        // Set a handler for when the transition finishes
        transition.setOnFinished(event -> {
            // Clear input fields and hide the box after the animation completes
            txtInputNewPassword.clear();
            txtInputNewPasswordConfirm.clear();
            animatedResetBox.setVisible(false);
            animatedResetBox.setLayoutX(17);
            animatedResetBox.setLayoutY(primaryStage.getHeight() - animatedResetBox.getHeight() + 100);
            isTransitionInProgress = false; // Transition ends
        });
        transition.play();
    }
    @FXML
    private void btnCancelNewUser() {
        disableAddUser();
    }
    @FXML
    private void btnConfirmNewPassword() {
        String newPasswordInput = txtInputNewPassword.getText();
        String newConfirmPasswordInput = txtInputNewPasswordConfirm.getText();

        if (newPasswordInput.isEmpty() || newConfirmPasswordInput.isEmpty()) {
            displayErrorModel.displayErrorC("Not all field filled");
            return;
        }

        if (!Objects.equals(newPasswordInput, newConfirmPasswordInput)) {
            displayErrorModel.displayErrorC("Not identical passwords");
            return;
        }

        try {
            currentUser.setPassword(newPasswordInput);
            userModel.updateUser(currentUser);
            disableResetPassword();
        } catch (Exception e) {
            displayErrorModel.displayErrorC("Password could not be updated");
        }
    }

    @FXML
    private void btnCancelNewPassword() {
        disableResetPassword();
    }

    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final javafx.scene.control.Button deleteButton;
        private final javafx.scene.control.Button resetButton;
        private final DisplayErrorModel displayErrorModel;
        private final Image mainIcon = new Image("Icons/mainIcon.png");

        public ButtonCell(UserModel userModel,  EMSAdminCoordinatorManager emsAdminCoordinatorManager) {
            displayErrorModel = new DisplayErrorModel();
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

                    Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                        try {
                            userModel.deleteUser(user); // We delete the User
                        } catch (Exception e) {
                            displayErrorModel.displayErrorC("User not deleted try again");
                        }
                    }
                }
            });

            resetButton = new javafx.scene.control.Button("R");
            resetButton.setStyle("-fx-font-size: 12");
            resetButton.setPrefWidth(20); // Set preferred width
            resetButton.setPrefHeight(20); // Set preferred height
            resetButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof User user) {
                    emsAdminCoordinatorManager.tblResetPassword(user);
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
                if (getTableView().getColumns().indexOf(getTableColumn()) == 1) { // Assuming colResetPassword is at index 1
                    setGraphic(resetButton);
                } else if (rowData instanceof User user) {
                    if (user.getUserAccessLevel() != 2) {
                        setGraphic(deleteButton);
                    } else {
                        setGraphic(null); // Don't show the button if access level is 2
                    }
                }
            }
        }

        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forButton(UserModel userModel,EMSAdminCoordinatorManager emsAdminCoordinatorManager) {
            return param -> new ButtonCell<>(userModel, emsAdminCoordinatorManager);
        }
    }
}