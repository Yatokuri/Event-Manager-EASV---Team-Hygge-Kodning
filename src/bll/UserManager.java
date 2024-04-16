package bll;

import be.User;
import bll.util.crytographic.BCrypt;
import dal.db.User_DB;

import java.util.Collection;

public class UserManager {
    private final User_DB user_DB;

    public UserManager() throws Exception { user_DB = new User_DB(); }

//***********************************LOGIN*************************************
    public User checkLogin(String userName, String password) throws Exception {return user_DB.checkUserBCrypt(userName, password);}

//********************************CRUD*USER***********************************
    public be.User createNewUser(be.User newUser) throws Exception { //Make the user password to the BCrypt format
        newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        return user_DB.createUser(newUser); }
    public Collection<User> getAllUsers() throws Exception {return user_DB.getAllUsers();}
    public void updateUser(be.User selectedUser) throws Exception { //Change the usr password to the BCrypt format
        selectedUser.setPassword(BCrypt.hashpw(selectedUser.getPassword(), BCrypt.gensalt()));
        user_DB.updateUser(selectedUser);
    }
    public void deleteUser(be.User selectedUser) throws Exception {user_DB.removeUser(selectedUser);}

//***************************CRUD*PROFILE*IMAGE*******************************
    public void createUserProfileIMG(be.User selectedUser) throws Exception {user_DB.createUserProfileIMG(selectedUser);}
    public void readUserProfileIMG(be.User selectedUser) throws Exception {user_DB.readUserProfileIMG(selectedUser);}
    public void updateUserProfileIMG(be.User selectedUser) throws Exception {user_DB.updateUserProfileIMG(selectedUser);}
    public void deleteUserProfileIMG(be.User selectedUser) throws Exception {user_DB.deleteUserProfileIMG(selectedUser);}
}