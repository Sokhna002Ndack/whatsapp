package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {

    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_MEMBRES = 7;

    public static void main(String[] args) {
        System.out.println("📡 Serveur de chat démarré sur le port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                synchronized (clients) {
                    if (clients.size() >= MAX_MEMBRES) {
                        socket.getOutputStream().write("❌ Limite de 7 membres atteinte. Connexion refusée.\n".getBytes());
                        socket.close();
                        continue;
                    }
                }

                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur serveur : " + e.getMessage());
        }
    }

    // 🔹 Ajouter un client à la liste
    public static synchronized void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    // 🔹 Retirer un client de la liste
    public static synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    // 🔹 Vérifie si un pseudo est déjà utilisé
    public static synchronized boolean isPseudoTaken(String pseudo) {
        return clients.stream()
                .filter(c -> c.getPseudo() != null)
                .anyMatch(c -> c.getPseudo().equalsIgnoreCase(pseudo));
    }

    // 🔹 Diffuser un message à tous les clients sauf l'expéditeur
    public static synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (!client.getPseudo().equals(sender.getPseudo())) {
                client.sendMessage(message);
            }
        }
    }

    // 🔹 Pour afficher le nombre de clients connectés (utile pour debug ou UI admin)
    public static synchronized int getClientCount() {
        return clients.size();
    }
}