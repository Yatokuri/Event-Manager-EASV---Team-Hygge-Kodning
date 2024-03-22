package be;

import javafx.scene.image.Image;

public class User {
    private String userName, password;
    private Image profileIMG;
    private int userAccessLevel;

    public User(String userName, String password, int userAccessLevel){
        this.userName = userName;
        this.password = password;
        this.userAccessLevel = userAccessLevel;
    }

    public int getUserAccessLevel() {
        return userAccessLevel;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {this.password = password;}

    private void setUserName(String userName) {
        this.userName = userName;
    }

    public Image getProfileIMG() {
        return profileIMG;
    }

    public void setProfileIMG(Image profileIMG) {
        this.profileIMG = profileIMG;
    }
}
