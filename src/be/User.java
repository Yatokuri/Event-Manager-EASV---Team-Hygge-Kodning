package be;

public class User {
    private String userName, password;
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


}
