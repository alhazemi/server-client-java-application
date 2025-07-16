package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static synchronized void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
        System.out.println("Client " + username + " added.");
    }

    public static synchronized void removeClient(String username) {
        clients.remove(username);
        System.out.println("Client " + username + " removed.");
    }

    public static synchronized ClientHandler getClientHandler(String username) {
        return clients.get(username);
    }
}


