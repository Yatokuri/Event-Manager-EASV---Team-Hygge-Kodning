import gui.controller.EMSController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EMS.fxml"));
        EMSController controller = new EMSController(primaryStage); // Pass the stage instance to the controller
        loader.setController(controller);
        Parent root = loader.load();

        primaryStage.getIcons().add(new Image("/icons/mainIcon.png"));
        primaryStage.setTitle("Event Manager System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }



    public static void main(String[] args) {launch(args);}
}

