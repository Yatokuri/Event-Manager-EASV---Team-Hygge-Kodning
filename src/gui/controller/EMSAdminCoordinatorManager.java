package gui.controller;

import gui.model.UserModel;

public class EMSAdminCoordinatorManager {

    private UserModel userModel;

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public void startupProgram() {
        System.out.println(userModel.getObsUsers());
    }
}
