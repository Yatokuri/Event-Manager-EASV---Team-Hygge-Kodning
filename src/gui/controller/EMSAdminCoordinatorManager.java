package gui.controller;

import be.User;
import gui.model.UserModel;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class EMSAdminCoordinatorManager implements Initializable {

    @FXML
    private TableView<User> tblUser;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, Void> colRemove;

    private UserModel userModel;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public void startupProgram() {
        System.out.println(userModel.getObsUsers());

        // Add data to the table
        ObservableList<User> userList = userModel.getObsUsers();
        tblUser.setItems(userList);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the table with the specified columns
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colRemove.setCellFactory(ButtonCell.forTableColumn());

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
    }



    public void btnAddUser(ActionEvent actionEvent) {
        //TODO
    }


    public void btnPreviousWindow() {
        Stage parent = (Stage) tblUser.getScene().getWindow();
        Event.fireEvent(parent, new WindowEvent(parent, WindowEvent.WINDOW_CLOSE_REQUEST));
    }


    // Custom cell class for the button in the table column to remove user etc
    private static class ButtonCell<S> extends TableCell<S, Void> {
        private final javafx.scene.control.Button deleteButton;

        public ButtonCell() {
            deleteButton = new javafx.scene.control.Button("- ");
            deleteButton.setPrefWidth(20); // Set preferred width
            deleteButton.setPrefHeight(20); // Set preferred height

            deleteButton.setOnAction(event -> {
                S rowData = getTableView().getItems().get(getIndex());
                if (rowData instanceof User user) {
                    System.out.println("Removing user: " + user.getUserName());
                    //TODO User remove function
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(deleteButton);
            }
        }

        public static <S> javafx.util.Callback<TableColumn<S, Void>, TableCell<S, Void>> forTableColumn() {
            return param -> new ButtonCell<>();
        }
    }
}
