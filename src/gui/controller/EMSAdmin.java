package gui.controller;

import gui.model.UserModel;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EMSAdmin {

    public HBox eventHBox;
    @FXML
    public VBox navBar;
    private UserModel userModel;




    public void startupProgram() {
        System.out.println("koen "  + userModel.getLoggedInUser());
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public void eventList()
    {
        HBox eventHBox = new HBox();
        eventHBox.setAlignment(Pos.CENTER);

    }


}
