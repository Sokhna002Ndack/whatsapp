package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.net.Socket;

public class ChatController {

    @FXML private TextArea messageArea;
    @FXML private TextField inputField;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String pseudo;

    @FXML
    public void initialize() {
        askPseudo();
        connectToServer();
    }

    private void askPseudo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion");
        dialog.setHeaderText("Entrez votre pseudo :");
        dialog.setContentText("Pseudo :");
        dialog.showAndWait().ifPresentOrElse(p -> pseudo = p, () -> System.exit(0));
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // ⚠ Attente de la demande du serveur
            String ask = in.readLine();
            System.out.println("Serveur : " + ask); // Pour debug, tu peux retirer ensuite

            // ✅ Envoi du pseudo après la demande
            out.println(pseudo);

            // Thread pour recevoir les messages
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        String finalMsg = msg;
                        Platform.runLater(() -> messageArea.appendText(finalMsg + "\n"));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> messageArea.appendText("❌ Déconnecté du serveur.\n"));
                }
            }).start();

        } catch (IOException e) {
            showError("Erreur de connexion", "Impossible de se connecter au serveur.");
        }
    }

    @FXML
    public void handleSend() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            out.println(msg);
            inputField.clear();
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(title);
            alert.setContentText(content);
            alert.showAndWait();
            System.exit(1);
        });
    }
}