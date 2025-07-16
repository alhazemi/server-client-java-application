package com.chat.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private String content;
    private MessageType type;
    private byte[] fileData; // For image and audio

    public enum MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        EMOJI
    }

    // Constructor for text/emoji messages
    public Message(String sender, String receiver, String content, MessageType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
    }

    // Constructor for image/audio messages
    public Message(String sender, String receiver, byte[] fileData, MessageType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.fileData = fileData;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getFileData() {
        return fileData;
    }
}


