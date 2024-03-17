package gui.model;

import be.User;
import bll.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserModel {
    private User currentLoggedInUser;
    private UserManager userManager;
    private ObservableList<User> usersToBeViewed;

    public UserModel() throws Exception {
        userManager = new UserManager() ;
        usersToBeViewed = FXCollections.observableArrayList();
        usersToBeViewed.addAll(userManager.getAllUsers());
    }


    public User signIn(String username, String password) throws Exception   {
        return userManager.checkLogin(username, password);
    }

    public User createNewUser(User user) throws Exception{ return userManager.createNewUser(user);}
    public void deleteUser(User user) throws Exception{ userManager.deleteUser(user);}

    public void updateUser(User user) throws Exception{ userManager.updateUser(user);}

    public void setLoggedInUser(User user) {currentLoggedInUser = user;}

    public User getLoggedInUser() {
       return currentLoggedInUser;
    }

    public ObservableList<User> getObsUsers() { return usersToBeViewed; }
}
