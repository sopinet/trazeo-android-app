package com.sopinet.trazeo.app.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sopinet.trazeo.app.ChatActivity;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.chat.model.Message;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;

@EFragment
public class ChatFragment extends Fragment{

    View root;
    Context context;
    ChatAdapter chatAdapter;

    ListView chatMsgs;
    EditText inputText;
    Button sendBtn;
    ArrayList<Message> messages;

    public ChatFragment() {}

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_chat, container, false);

        context = root.getContext();

        chatMsgs = (ListView) root.findViewById(R.id.chatMsgs);
        inputText = (EditText) root.findViewById(R.id.inputText);
        sendBtn = (Button) root.findViewById(R.id.sendBtn);

        configureMessagesList();
        configureSendButton();

        return root;
    }

    void configureMessagesList() {
        messages = new ArrayList<>();
        messages.addAll(((ChatActivity) context).group.chat.messages());
        chatAdapter = new ChatAdapter(context, messages);
        chatMsgs.setAdapter(chatAdapter);
    }

    void configureSendButton() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputText.getText().toString().isEmpty())
                    sendMessage();
            }
        });
    }

    public void sendMessage() {
        Message message = new Message(((ChatActivity) context).username, inputText.getText().toString(),
                ChatUtils.getTime(), Message.SENT);

        chatAdapter.addItem(message);
        inputText.setText("");

        message.chat = ((ChatActivity) context).group.chat;
        message.save();

        ((ChatActivity) context).sendGcmMessage(message, chatAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        configureMessagesList();
        context.registerReceiver(broadcastReceiver, new IntentFilter("com.sopinet.trazeo.app.chatmessage"));
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Receptor de mensajes
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Si el mensaje viene del mismo chat
            if(intent.getStringExtra("groupId").equals(((ChatActivity) context).groupId))  {
                // Añado el mensaje a la lista
                long longId = intent.getLongExtra("longId", 0);
                Message message = Message.load(Message.class, longId);
                chatAdapter.addItem(message);
                messages.add(message);
            }
        }
    };
}
