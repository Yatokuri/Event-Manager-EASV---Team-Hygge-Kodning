package gui.model;

import bll.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageModel {

    private static ImageModel instance;
    private final ImageManager imageManager;

    private ImageModel() throws Exception {
        imageManager = new ImageManager();
    }

    // Public method to get the singleton instance, so we have control over data
    public static ImageModel getInstance() throws Exception {
        if (instance == null) {
            synchronized (ImageModel.class) { if (instance == null) { instance = new ImageModel(); } }
        }
        return instance;
    }

    public int createSystemIMG(Image image) throws Exception{ return imageManager.createSystemIMG(image);}

    public ImageView readSystemIMG(int IMGID) throws Exception{ return imageManager.readSystemIMG(IMGID);}

    public void updateSystemIMG(int IMGID, Image image) throws Exception{ imageManager.updateSystemIMG(IMGID, image);}

    public void deleteSystemIMG(int IMGID) throws Exception{ imageManager.deleteSystemIMG(IMGID);}

}