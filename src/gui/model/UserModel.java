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

    public User createNewUser(User user) throws Exception {
        User newUser = userManager.createNewUser(user);
        usersToBeViewed.add(newUser);
        return newUser;
    }
    public void deleteUser(User user) throws Exception {
        userManager.deleteUser(user);
        usersToBeViewed.remove(user);
    }

    public void createUserProfileIMG(User user) throws Exception{ userManager.createUserProfileIMG(user);}
    public void readUserProfileIMG(User user) throws Exception{ userManager.readUserProfileIMG(user);}
    public void updateUserProfileIMG(User user) throws Exception{ userManager.updateUserProfileIMG(user);}
    public void deleteUserProfileIMG(User user) throws Exception{ userManager.deleteUserProfileIMG(user);}
    public void updateUser(User user) throws Exception{ userManager.updateUser(user);}

    public void setLoggedInUser(User user) {currentLoggedInUser = user;}

    public void logOutUser(){ currentLoggedInUser = null; }

    public User getLoggedInUser() {
       return currentLoggedInUser;
    }

    public ObservableList<User> getObsUsers() { return usersToBeViewed; }
}
