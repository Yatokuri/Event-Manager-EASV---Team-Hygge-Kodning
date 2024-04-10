package be;

import javafx.scene.image.Image;

public class User {
    private final String userName;
    private String password;
    private Image profileIMG;
    private final int userAccessLevel;

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

    public Image getProfileIMG() {
        return profileIMG;
    }

    public void setProfileIMG(Image profileIMG) {
        this.profileIMG = profileIMG;
    }
}
