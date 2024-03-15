package gui.model;

import be.User;
import bll.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserModel {
    private User currentLoggedInUser;
    private UserManager userManager;
    private final ObservableList<User> obsUsers = FXCollections.observableArrayList();

    public UserModel() throws Exception {
        userManager = new UserManager() ;
        obsUsers.clear();
        obsUsers.addAll(userManager.getAllUsers());
    }


    public User signIn(String username, String password) throws Exception   {

        System.out.println("tog");

        return userManager.checkLogin(username, password);

    }

    public User createNewUser(User user) throws Exception{ return userManager.createNewUser(user);}
    public void deleteUser(User user) throws Exception{ userManager.deleteUser(user);}

    public void setLoggedInUser(User user) {
        currentLoggedInUser = user;
        System.out.println("use sæt " + user.getUserName());

    }

    public User getLoggedInUser() {
       return currentLoggedInUser;
    }

    public ObservableList<User> getObsUsers() { return obsUsers; }
}
