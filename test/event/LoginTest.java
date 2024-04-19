package event;

import gui.controller.EMSController;
import gui.model.UserModel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LoginTest  {
    private EMSController emsController;
    private UserModel userModel;

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    public void setUp() throws Exception {
        emsController = new EMSController();
       userModel = UserModel.getInstance();
    }

    @DisplayName("Login no username entered")
    @Test
    void testLoginWithEmptyUsername() {
        Platform.runLater(() -> {
        // Act + assert
        Exception err = Assertions.assertThrows(IllegalArgumentException.class, () -> {
           emsController.btnLoginSystemLogin("", "password");
        });
        // Extra assert
        String expectedMessage = "Wrong username or password";
        String actualMessage = err.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);
    });
    }

    @DisplayName("Login no password entered")
    @Test
    void testLoginWithEmptyPassword() {
        Platform.runLater(() -> {
            // Act + assert
            Exception err = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                emsController.btnLoginSystemLogin("JohnDoe", "");
            });
            // Extra assert
            String expectedMessage = "Wrong username or password";
            String actualMessage = err.getMessage();
            Assertions.assertEquals(expectedMessage, actualMessage);
        });
    }

    @DisplayName("Login no username & password entered")
    @Test
    void testLoginWithEmptyUsernamePassword() {
        Platform.runLater(() -> {
            // Act + assert
            Exception err = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                emsController.btnLoginSystemLogin("", "");
            });
            // Extra assert
            String expectedMessage = "Wrong username or password";
            String actualMessage = err.getMessage();
            Assertions.assertEquals(expectedMessage, actualMessage);
        });
    }

    @DisplayName("Login correct admin login")
    @Test
    void testLoginWithAdminCorrect() throws Exception {
        Platform.runLater(() -> {
            // Act
            try {
                emsController.btnLoginSystemLogin("a", "a");
            } catch (InterruptedException ignored) {
            }
        });
        CountDownLatch latch = new CountDownLatch(3);
        latch.countDown(); // We want to make sure userModel is not null, if it null also mean login was wrong
        boolean initialized = latch.await(3, TimeUnit.SECONDS);
        if (!initialized) {
            // Assert
            int expectedUserName = 2;
            int loggedInUserName = userModel.getLoggedInUser().getUserAccessLevel();
            Assertions.assertEquals(expectedUserName, loggedInUserName);
        }
    }


    @DisplayName("Login correcter coordinator login")
    @Test
    void testLoginWithCoordinator() throws InterruptedException {
        Platform.runLater(() -> {
            // Act
            try {
                emsController.btnLoginSystemLogin("1", "1");
            } catch (InterruptedException ignored) {
            }
        });
        CountDownLatch latch = new CountDownLatch(3);
        latch.countDown(); // We want to make sure userModel is not null, if it null also mean login was wrong
        boolean initialized = latch.await(3, TimeUnit.SECONDS);
        if (!initialized) {
            // Assert
            int expectedUserName = 1;
            int loggedInUserName = userModel.getLoggedInUser().getUserAccessLevel();
            Assertions.assertEquals(expectedUserName, loggedInUserName);
        }
    }
}

//Disclaimer when testing, the error "because "currentStage" is null" exist
//because login is correct, but it cannot open new window cause the way we test