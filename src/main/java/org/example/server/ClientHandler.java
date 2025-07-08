package org.example.server;

import org.example.Dao.MembreDao;
import org.example.model.Membre;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String pseudo;
    private final static List<String> blacklist = List.of("con", "merde", "idiot", "putain");

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getPseudo() {
        return pseudo;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Demander un pseudo
            out.println("Veuillez entrer votre pseudo : ");
            pseudo = in.readLine();

            if (pseudo == null || pseudo.trim().isEmpty()) {
                out.println("❌ Pseudo invalide.");
                return;
            }

            // Vérifie que le pseudo n'est pas déjà pris (en mémoire)
            if (ChatServer.isPseudoTaken(pseudo)) {
                out.println("❌ Ce pseudo est déjà utilisé. Connexion refusée.");
                return;
            }

            // Vérification dans la BD
            MembreDao membreDao = new MembreDao();
            Membre membre = membreDao.findOrCreate(pseudo);

            // Vérifie si banni
            if (membre.isBanned()) {
                out.println("🚫 Vous êtes banni du serveur.");
                return;
            }

            // Ajout dans la liste des clients connectés
            ChatServer.addClient(this);

            out.println("✅ Bienvenue " + pseudo + " !");
            ChatServer.broadcast("👋 " + pseudo + " a rejoint le chat.", this);
            System.out.println("✅ " + pseudo + " est connecté.");

            // Écoute des messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.trim().isEmpty()) continue;

                if (contientInjure(message)) {
                    out.println("⚠ Message bloqué : langage inapproprié.");
                    continue;
                }

                membreDao.save(membre, message); // sauvegarde en BD
                ChatServer.broadcast(pseudo + " : " + message, this);
            }

            // Déconnexion volontaire ou socket fermé
            System.out.println("📴 " + pseudo + " s’est déconnecté proprement.");

        } catch (IOException e) {
            System.err.println("❌ Erreur avec le client " + pseudo + " : " + e.getMessage());
        } finally {
            if (pseudo != null) {
                ChatServer.broadcast("👋 " + pseudo + " a quitté le chat.", this);
                System.out.println("👋 " + pseudo + " a quitté le chat.");
            }
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private boolean contientInjure(String message) {
        String lower = message.toLowerCase();
        return blacklist.stream().anyMatch(lower::contains);
    }

    public void sendMessage(String message) {
        if (out != null) {
            try {
                out.println(message);
            } catch (Exception e) {
                System.err.println("❌ Erreur d'envoi à " + pseudo + ": " + e.getMessage());
            }
        }
    }
}