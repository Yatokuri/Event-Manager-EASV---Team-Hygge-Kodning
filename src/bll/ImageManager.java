package bll;

import dal.db.Image_DB;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageManager {
    private final Image_DB image_DB;

    public ImageManager() throws Exception { image_DB = new Image_DB();  }
    public int createSystemIMG(Image image) throws Exception { return image_DB.createSystemIMG(image);}
    public ImageView readSystemIMG(int selectedUser) throws Exception { return image_DB.readSystemIMG(selectedUser);}
    public void updateSystemIMG(int id, Image image) throws Exception { image_DB.uploadSystemIMG(id, image);}
    public void deleteSystemIMG(int selectedUser) throws Exception { image_DB.deleteSystemIMG(selectedUser);}
}
