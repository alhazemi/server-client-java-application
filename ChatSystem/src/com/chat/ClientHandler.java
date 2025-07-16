package com.chat;

import com.chat.model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private Map<String, ClientHandler> clients;

    public ClientHandler(Socket socket, Map<String, ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // First message from client should be username
            Object initialMessage = in.readObject();
            if (initialMessage instanceof String) {
                this.username = (String) initialMessage;
                Server.addClient(this.username, this);
                System.out.println(this.username + " has connected.");
            } else {
                System.err.println("Unexpected initial message from client.");
                return;
            }

            while (true) {
                Object receivedObject = in.readObject();
                if (receivedObject instanceof Message) {
                    Message message = (Message) receivedObject;
                    System.out.println("Received message from " + message.getSender() + " to " + message.getReceiver() + ": " + message.getContent());
                    sendMessageToClient(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client " + username + " disconnected: " + e.getMessage());
        } finally {
            if (username != null) {
                Server.removeClient(username);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + username + ": " + e.getMessage());
        }
    }

    private void sendMessageToClient(Message message) {
        ClientHandler receiverHandler = Server.getClientHandler(message.getReceiver());
        if (receiverHandler != null) {
            receiverHandler.sendMessage(message);
        } else {
            System.out.println("Receiver " + message.getReceiver() + " not found. Message from " + message.getSender() + " not delivered.");
            // Optionally, send a delivery failure message back to the sender
            Message deliveryFailure = new Message("Server", message.getSender(), "User " + message.getReceiver() + " is not online.", Message.MessageType.TEXT);
            sendMessage(deliveryFailure);
        }
    }
}


