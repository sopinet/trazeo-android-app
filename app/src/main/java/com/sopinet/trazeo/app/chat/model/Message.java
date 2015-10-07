package com.sopinet.trazeo.app.chat.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Messages")
public class Message extends Model{

    // Tipos de mensaje
    public static final int RECEIVED = 0;
    public static final int SENT = 1;
    public static final int NOTIFICATION = 2;

    // Message States

    public static final int NOT_SENT = 0;
    public static final int SERVER_RECEIVED = 1;
    public static final int USER_RECEIVED = 2;
    public static final int ERROR_RECEIVED = 3;

    // Campos

    @Column(name = "Idd")
    public String id;

    @Column(name = "Chat")
    public Chat chat;

    @Column(name = "Text")
    public String text;

    @Column(name = "Type")
    public int type;

    @Column(name = "Username")
    public String username;

    @Column(name = "Time")
    public String time;

    @Column(name = "MessageState")
    public int messageState = NOT_SENT;

    public Message() {
        super();
    }

    public Message(String username, String text, String time, int type) {
        this.username = username;
        this.text = text;
        this.time = time;
        this.type = type;
    }
}
