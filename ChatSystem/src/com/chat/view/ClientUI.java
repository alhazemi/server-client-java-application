package com.chat.view;

import com.chat.controller.ClientController;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class ClientUI extends JFrame {

    private JTextPane chatArea;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton emojiButton;
    private JButton recordButton;
    private JButton imageButton;
    private JTextField usernameField;
    private JTextField receiverField;
    private JButton connectButton;

    private ClientController controller;

    private final String[] emojis = {
            "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
            "😋", "😎", "😍", "😘", "🥰", "😗", "😙", "😚", "🙂", "🤗",
            "🤩", "🤔", "🤨", "😐", "😑", "😶", "🙄", "😏", "😣", "😥",
            "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝"
    };

    public ClientUI() {
        setTitle("Client Chat");
        setSize(550, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        controller = new ClientController(this);

        // Top Panel
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        topPanel.add(new JLabel("Your Username:"));
        usernameField = new JTextField();
        topPanel.add(usernameField);

        topPanel.add(new JLabel("Receiver Username:"));
        receiverField = new JTextField();
        topPanel.add(receiverField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());
        topPanel.add(connectButton);

        add(topPanel, BorderLayout.NORTH);

        // Chat Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageArea = new JTextArea(3, 30);
        messageArea.setLineWrap(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);
        inputPanel.add(msgScroll, BorderLayout.CENTER);

        Dimension btnSize = new Dimension(60, 35);

        emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        emojiButton.setBackground(Color.YELLOW);
        emojiButton.setPreferredSize(btnSize);
        emojiButton.addActionListener(e -> {
            String emoji = showEmojiPanel();
            if (emoji != null) {
                messageArea.append(emoji);
            }
        });

        imageButton = new JButton("📷");
        imageButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        imageButton.setBackground(Color.PINK);
        imageButton.setPreferredSize(btnSize);
        imageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = chooser.getSelectedFile();
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    controller.sendImage(imageBytes, receiverField.getText());
                } catch (Exception ex) {
                    appendMessage("Error reading image file: " + ex.getMessage());
                }
            }
        });

        recordButton = new JButton("🎙️");
        recordButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        recordButton.setBackground(Color.GREEN.darker());
        recordButton.setForeground(Color.WHITE);
        recordButton.setPreferredSize(btnSize);
        recordButton.addActionListener(e -> controller.recordAndSendAudio(receiverField.getText()));

        sendButton = new JButton("➤");
        sendButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        sendButton.setBackground(new Color(0, 122, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(btnSize);
        sendButton.setEnabled(false); // disabled until connection
        sendButton.addActionListener(e -> {
            String msg = messageArea.getText().trim();
            if (!msg.isEmpty()) {
                controller.sendMessage(msg, receiverField.getText());
                messageArea.setText("");
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(emojiButton);
        buttonPanel.add(imageButton);
        buttonPanel.add(recordButton);
        buttonPanel.add(sendButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            controller.connectToServer(username);
            usernameField.setEditable(false);
            sendButton.setEnabled(true);
            appendMessage("✅ Connected to server as: " + username);
        } else {
            appendMessage("❌ Please enter your username.");
        }
    }

    private String showEmojiPanel() {
        JDialog emojiDialog = new JDialog(this, "Select Emoji", true);
        emojiDialog.setUndecorated(true);
        emojiDialog.setSize(300, 300);
        emojiDialog.setLocationRelativeTo(emojiButton);

        JPanel panel = new JPanel(new GridLayout(5, 8, 4, 4));
        final String[] selectedEmoji = {null};

        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            btn.addActionListener(e -> {
                selectedEmoji[0] = emoji;
                emojiDialog.dispose();
            });
            panel.add(btn);
        }

        emojiDialog.add(new JScrollPane(panel));
        emojiDialog.setVisible(true);
        return selectedEmoji[0];
    }

    public void appendMessage(String msg) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), msg + "\n\n", null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendImage(byte[] imageBytes, String sender) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img != null) {
                ImageIcon icon = new ImageIcon(img.getScaledInstance(200, -1, Image.SCALE_SMOOTH));
                StyledDocument doc = chatArea.getStyledDocument();
                doc.insertString(doc.getLength(), sender + ": ", null);
                Style style = chatArea.addStyle("ImageStyle", null);
                StyleConstants.setIcon(style, icon);
                doc.insertString(doc.getLength(), "ignored text", style);
                doc.insertString(doc.getLength(), "\n\n", null);
                chatArea.setCaretPosition(doc.getLength());
            } else {
                appendMessage("[Error displaying image]");
            }
        } catch (Exception e) {
            appendMessage("[Image error: " + e.getMessage() + "]");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientUI::new);
    }
}
