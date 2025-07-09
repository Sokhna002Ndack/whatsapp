package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.net.Socket;

public class ChatController {

    @FXML
    private TextArea messageArea;
    @FXML
    private TextField inputField;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
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
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

           // out = new PrintWriter(socket.getOutputStream(), true);

            // 🔄 Attendre la demande du pseudo
            String ask = in.readLine();
            System.out.println("Serveur dit : " + ask);

            // 📤 Envoi du pseudo
            out.println(pseudo);

            // 🧵 Thread de réception des messages
            Thread receiver = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        String timestamp = java.time.LocalTime.now().withNano(0).toString();
                        String finalMsg = "[" + timestamp + "] " + msg;

                        // Affiche dans l'interface
                        String finalMsg1 = msg;
                        Platform.runLater(() -> {
                            messageArea.appendText(finalMsg + "\n");
                            showPopupNotification(finalMsg1); // 🔔 notification
                            playNotificationSound();    // 🔊 son
                        });

                        System.out.println("📩 Reçu : " + finalMsg);
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        messageArea.appendText("❌ Déconnecté du serveur.\n");
                        showError("Connexion perdue", "Vous avez été déconnecté du serveur.");
                    });
                }
            });
            receiver.setDaemon(true); // permet de fermer proprement
            receiver.start();

        } catch (IOException e) {
            showError("Erreur de connexion", "Impossible de se connecter au serveur.");
        }
    }

    private void playNotificationSound() {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Erreur de son : " + e.getMessage());
        }
    }

    private void showPopupNotification(String msg) {
        if (msg.contains(pseudo)) return; // ignore les messages de soi-même

        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setTitle("Nouveau message");
        popup.setHeaderText("Message reçu !");
        popup.setContentText(msg.length() > 80 ? msg.substring(0, 80) + "..." : msg);
        popup.setResizable(false);

        // Affiche sans bloquer l’UI
        new Thread(() -> {
            try {
                Thread.sleep(300); // petit délai pour éviter conflits
                Platform.runLater(() -> popup.show());
            } catch (InterruptedException ignored) {}
        }).start();
    }

    @FXML
    public void handleQuit() {
        try {
            if (out != null) {
                out.println("/quit"); // ✅ Envoie la commande spéciale au serveur
            }
            closeConnection();
            Platform.exit(); // Ferme l'application proprement
        } catch (Exception e) {
            showError("Erreur", "Impossible de quitter proprement.");
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }

    @FXML
    public void handleSend() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
           // out.println("message-----------------------" +msg);
            try {
                out.println(msg); // ✅ On envoie sans fermer le flux
                inputField.clear();
            } catch (Exception e) {
                showError("Erreur d'envoi", "Impossible d'envoyer le message.");
            }
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