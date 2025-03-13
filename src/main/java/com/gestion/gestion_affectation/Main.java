package com.gestion.gestion_affectation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
double x,y = 0;
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/home.fxml")));
        //FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("fxml/home.fxml"));
       // Scene scene = new Scene(fxmlLoader.load());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/gestion_affectation/css/styling.css").toExternalForm());
        stage.initStyle(StageStyle.UNDECORATED);
        root.setOnMousePressed(evt ->{
            x = evt.getSceneX();
            y = evt.getSceneY();
        });
        root.setOnMouseDragged(evt ->{
            stage.setX(evt.getScreenX()- x);
            stage.setY(evt.getScreenY()- y);
        });
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}