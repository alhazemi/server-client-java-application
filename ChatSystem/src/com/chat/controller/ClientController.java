package com.chat.controller;

import com.chat.model.Message;
import com.chat.view.ClientUI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientController {

    private ClientUI ui;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    public ClientController(ClientUI ui) {
        this.ui = ui;
    }

    public void connectToServer(String username) {
        this.username = username;
        try {
            socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Send username to server as the first message
            out.writeObject(username);
            out.flush();

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        Object obj = in.readObject();
                        if (obj instanceof Message) {
                            Message receivedMessage = (Message) obj;
                            switch (receivedMessage.getType()) {
                                case TEXT:
                                    ui.appendMessage(receivedMessage.getSender() + ": " + receivedMessage.getContent());
                                    break;
                                case AUDIO:
                                    ui.appendMessage(receivedMessage.getSender() + ": 🔊 Sent audio");
                                    playAudio(receivedMessage.getFileData());
                                    break;
                                case IMAGE:
                                    ui.appendMessage(receivedMessage.getSender() + ": 🖼️ Sent image:");
                                    ui.appendImage(receivedMessage.getFileData(), receivedMessage.getSender());
                                    break;
                                case EMOJI:
                                    ui.appendMessage(receivedMessage.getSender() + ": " + receivedMessage.getContent());
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    ui.appendMessage("Receive error: " + e.getMessage());
                }
            });
            receiveThread.start();

        } catch (Exception e) {
            ui.appendMessage("Connection error: " + e.getMessage());
        }
    }

    public void sendMessage(String msg, String receiver) {
        try {
            if (out != null) {
                Message message = new Message(username, receiver, msg, Message.MessageType.TEXT);
                out.writeObject(message);
                out.flush();
                ui.appendMessage("You to " + receiver + ": " + msg);
            } else {
                ui.appendMessage("Not connected to server.");
            }
        } catch (Exception e) {
            ui.appendMessage("Send error: " + e.getMessage());
        }
    }

    public void sendImage(byte[] imageBytes, String receiver) {
        try {
            if (out != null) {
                Message message = new Message(username, receiver, imageBytes, Message.MessageType.IMAGE);
                out.writeObject(message);
                out.flush();
                ui.appendMessage("You to " + receiver + ": [Sent an image]");
            }
        } catch (Exception e) {
            ui.appendMessage("Error sending image: " + e.getMessage());
        }
    }

    public void recordAndSendAudio(String receiver) {
        new Thread(() -> {
            TargetDataLine microphone = null;
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);

                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    ui.appendMessage("Audio line with little-endian format not supported.");
                    return;
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                long endTime = System.currentTimeMillis() + 5000; // 5 seconds recording

                while (System.currentTimeMillis() < endTime) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    baos.write(buffer, 0, bytesRead);
                }

                microphone.stop();
                microphone.close();

                ui.appendMessage("Recording stopped. Sending audio...");

                byte[] audioBytes = baos.toByteArray();

                if (out != null) {
                    Message message = new Message(username, receiver, audioBytes, Message.MessageType.AUDIO);
                    out.writeObject(message);
                    out.flush();
                }
            } catch (Exception e) {
                ui.appendMessage("Audio recording error: " + e.getMessage());
            } finally {
                if (microphone != null && microphone.isOpen()) {
                    microphone.stop();
                    microphone.close();
                }
            }
        }).start();
    }

    private void playAudio(byte[] audioData) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();

            speakers.write(audioData, 0, audioData.length);
            speakers.drain();
            speakers.stop();
            speakers.close();
        } catch (Exception e) {
            ui.appendMessage("Playback error: " + e.getMessage());
        }
    }
}


