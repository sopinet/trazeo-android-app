package com.sopinet.trazeo.app;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.chat.ChatAdapter;
import com.sopinet.trazeo.app.chat.ChatCreator;
import com.sopinet.trazeo.app.chat.ChatFragment;
import com.sopinet.trazeo.app.chat.ChatListener;
import com.sopinet.trazeo.app.chat.GcmIntentService;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.chat.model.Message;
import com.sopinet.trazeo.app.gson.Child;
import com.sopinet.trazeo.app.gson.Member;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_chat)
public class ChatActivity extends AppCompatActivity
    implements ChatListener {

    @Pref
    public MyPrefs_ myPrefs;

    @ViewById
    LinearLayout login_progress_layout;

    @ViewById
    FrameLayout chatContainer;

    @Extra
    public String username;

    @Extra
    public String groupId;

    public Group group;

    @StringRes
    String error_title;

    @StringRes
    String server_error;

    @StringRes
    String error_loading;

    @ViewById
    Toolbar toolbar;

    ChatFragment chatFragment;

    FragmentManager fragmentManager;

    @InstanceState
    ArrayList<Member> members;

    @InstanceState
    boolean isErrorShowed = false;

    @InstanceState
    boolean isGetMembersFirstTime = true;

    @AfterViews
    void init() {

        this.fragmentManager = getSupportFragmentManager();
        group = Group.getGroupById(groupId);
        members = new ArrayList<>();

        if (group == null) {
            List<Group> groups = Group.getAllGroups();
            for (Group group : groups) {
                if (group.rideCreator.equals("owner") && group.hasride.equals("true")) {
                    this.group = group;
                    break;
                }
            }
        }

        if(group.chat == null) {
            createOrGetChat();
        } else {
            showFragment();
        }

        getMembers();
        configureToolBar();
    }

    @Override
    public void onChatCreated() {
        showFragment();
    }

    private void configureToolBar() {
        toolbar.setTitle(group.name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Background
    void createOrGetChat() {
        ChatCreator chatCreator = new ChatCreator(this, true);
        chatCreator.setOnChatListener(this);
        chatCreator.createOrGetChat(groupId);
    }

    /**
     * Funcion para mostrar el Fragment que contiene el chat
     */
    public void showFragment(){
            chatFragment = ChatFragment.newInstance();
            this.fragmentManager.beginTransaction()
                    .replace(R.id.chatContainer, chatFragment)
                    .commit();
    }


    public void sendGcmMessage(final Message message, final ChatAdapter adapter) {

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("password", myPrefs.pass().get());
        params.put("from", myPrefs.res_id().get());
        params.put("chatid", group.chat.id);
        params.put("type", GcmIntentService.TEXT);
        params.put("time", message.time);
        params.put("text", message.text);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHAT_REPLY, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Message noticeMessage = Message.load(Message.class, message.getId());
                noticeMessage.messageState = Message.SERVER_RECEIVED;
                noticeMessage.save();
                if (adapter != null) {
                    adapter.updateStateOfMessage(noticeMessage);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray response) {
                Message noticeMessage = Message.load(Message.class, message.getId());
                noticeMessage.messageState = Message.ERROR_RECEIVED;
                noticeMessage.save();
                if (adapter != null) {
                    adapter.updateStateOfMessage(noticeMessage);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Message noticeMessage = Message.load(Message.class, message.getId());
                noticeMessage.messageState = Message.ERROR_RECEIVED;
                noticeMessage.save();
                if (adapter != null) {
                    adapter.updateStateOfMessage(noticeMessage);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void getMembers() {

        if (!isGetMembersFirstTime) {
            chatContainer.setVisibility(View.GONE);
            login_progress_layout.setVisibility(View.VISIBLE);
        }

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("group_id", group.id);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MEMBERS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray result;
                try {
                    result = response.getJSONArray("data");
                    members = new ArrayList<>();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject jsonObject = result.getJSONObject(i);
                        String name = "";
                        String mobile = "";
                        try {
                            name = jsonObject.getString("name");
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            mobile = jsonObject.getString("mobile");
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        Member member = new Member(name, mobile);
                        try {
                            JSONArray childrens = jsonObject.getJSONArray("childrens");
                            for (int j = 0; j < childrens.length(); j++) {
                                member.childrens.add(new Child(childrens.get(j) + ""));
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        members.add(member);
                    }

                    if (!isGetMembersFirstTime) {
                        isGetMembersFirstTime = true;
                        Intent i = new Intent(ChatActivity.this, MembersActivity_.class);
                        i.putExtra("members", members);
                        startActivity(i);
                        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_open_translate);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(server_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray response) {
                if (!isGetMembersFirstTime)
                    showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!isGetMembersFirstTime)
                    showError(server_error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
               if (!isGetMembersFirstTime)
                    showError(server_error);
            }
        });
    }

    @UiThread
    void showError(String messageError) {
        chatContainer.setVisibility(View.VISIBLE);
        login_progress_layout.setVisibility(View.GONE);

        if (!isErrorShowed) {
            isErrorShowed = true;
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(error_title)
                    .setContentText(messageError)
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatContainer.setVisibility(View.VISIBLE);
        login_progress_layout.setVisibility(View.GONE);
        group.chat.visibility = 1;
        group.save();
    }

    @Override
    protected void onPause() {
        super.onPause();
        group.chat.visibility = 0;
        group.hasMessage = 0;
        group.save();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_members:
                if (members.size() > 0) {
                    Intent i = new Intent(ChatActivity.this, MembersActivity_.class);
                    i.putExtra("members", members);
                    startActivity(i);
                    overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_open_translate);
                }
                else {
                    isGetMembersFirstTime = false;
                    getMembers();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
