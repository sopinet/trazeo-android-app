package com.sopinet.trazeo.app.chat;

import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.chat.model.Chat;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ChatCreator {

    private Context context;
    private ArrayList<ChatListener> listeners;
    private boolean syncPost;

    public ChatCreator(Context context, boolean syncPost) {
        this.context = context;
        listeners = new ArrayList<>();
        this.syncPost = syncPost;
    }

    public void setOnChatListener(ChatListener chatListener) {
        listeners.add(chatListener);
    }

    public void createOrGetChat(final String groupId) {

        SharedPreferences myPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.getString("email", ""));
        params.put("pass", myPrefs.getString("pass", ""));
        params.put("device_token", myPrefs.getString("res_id", ""));
        params.put("group_id", groupId);

        if (syncPost) {
            RestClient.syncPost(RestClient.URL_API + RestClient.URL_API_CREATE_CHAT, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    createChatDb(groupId, response);
                }
            });
        } else {
            RestClient.post(RestClient.URL_API + RestClient.URL_API_CREATE_CHAT, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    createChatDb(groupId, response);
                }
            });
        }

    }

    private void createChatDb(String groupId, JSONObject response) {
        String chatId = null;
        try {
            chatId = response.getJSONObject("data").getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (chatId != null) {
            Chat chat = new Chat();
            chat.id = chatId;
            chat.save();

            Group group = Group.getGroupById(groupId);
            group.chat = chat;
            group.save();

            for(ChatListener listener : listeners) {
                listener.onChatCreated();
            }
            listeners.clear();
        }
    }
}
