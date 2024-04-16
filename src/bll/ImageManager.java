package bll;

import dal.db.Image_DB;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageManager {
    private final Image_DB image_DB;
    public ImageManager() throws Exception {image_DB = new Image_DB();}

    //********************************CRUD*IMAGE*******************************
    public int createSystemIMG(Image newImage) throws Exception {return image_DB.createSystemIMG(newImage);}
    public ImageView readSystemIMG(int selectedImage) throws Exception {return image_DB.readSystemIMG(selectedImage);}
    public void updateSystemIMG(int id, Image selectedImage) throws Exception {image_DB.uploadSystemIMG(id, selectedImage);}
    public void deleteSystemIMG(int selectedImage) throws Exception {image_DB.deleteSystemIMG(selectedImage);}
    public int getNextIDSystemIMG() throws Exception {return image_DB.getNextIDSystemIMG();}
}