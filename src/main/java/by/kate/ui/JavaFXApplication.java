package by.kate.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JavaFXApplication extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        Parent root = FXMLLoader.load(classLoader.getResource("fxml/main.fxml"));

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Welcome");
        stage.setScene(scene);
        stage.show();
    }

}