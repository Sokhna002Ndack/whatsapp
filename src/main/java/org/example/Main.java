package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Dao.MembreDao;
import org.example.Dao.MessageDao;
import org.example.model.Membre;
import org.example.model.Message;

import java.time.LocalDateTime;
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/chat.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Chat WhatsApp TCP");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}