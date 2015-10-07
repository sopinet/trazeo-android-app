package com.sopinet.trazeo.app.chat.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

@Table(name = "Chats")
public class Chat extends Model {

    @Column(name = "Idd")
    public String id = "";

    /**
     * When chat is on foreground (visible), sets 1, else 0
     */
    @Column(name = "Visibility")
    public int visibility = 0;

    public List<Message> messages() { return getMany(Message.class, "Chat");}

    public Chat() {
        super();
    }
}
