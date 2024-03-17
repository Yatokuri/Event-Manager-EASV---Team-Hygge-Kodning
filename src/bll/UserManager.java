package bll;

import be.Event;
import be.User;
import bll.util.crytographic.BCrypt;
import dal.db.User_DB;

import java.util.Collection;
import java.util.List;

public class UserManager {

    private User_DB user_DB;

    public UserManager() throws Exception {

        user_DB = new User_DB();

    }


    public User checkLogin(String userName, String password) throws Exception {

        String adminPassword = password;
        //String hashedPassword = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
        //Print out the generated hash
        //System.out.println("Generated BCrypt hash for admin password: " + hashedPassword);

        return user_DB.checkUserBCrypt(userName, password);
    }


    public be.User createNewUser(be.User newUser) throws Exception {
        //Make the user password to the BCrypt format
        newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        return user_DB.createUser(newUser); }


    public void updateUser(be.User selectedUser) throws Exception { user_DB.updateUser(selectedUser); }

    public void deleteUser(be.User selectedUser) throws Exception { user_DB.removeUser(selectedUser);}

    public Collection<User> getAllUsers() throws Exception   {
        return user_DB.getAllUsers();
    }
}
