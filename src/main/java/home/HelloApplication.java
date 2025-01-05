package home;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // Make the window draggable
            makeWindowDraggable(primaryStage, root);

            primaryStage.setTitle("GIMATAG PHARMACY");
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.show();

            // Call the method to establish a database connection
            // createConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double xOffset = 0;
    private double yOffset = 0;

    public void makeWindowDraggable(Stage primaryStage, Parent root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();


        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }


    public static void main(String[] args) {
        launch();
    }
}
