package com.sopinet.trazeo.app;

import android.content.Intent;
import android.provider.Settings.Secure;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.StringHelper;
import com.sopinet.trazeo.app.chat.ChatCreator;
import com.sopinet.trazeo.app.chat.ChatListener;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.Login;
import com.sopinet.trazeo.app.gson.MyProfile;
import com.sopinet.trazeo.app.gson.Notifications;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.Constants;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import cn.pedant.SweetAlert.SweetAlertDialog;


@EActivity(R.layout.activity_login)
public class LoginSimpleActivity extends ActionBarActivity
    implements ChatListener{

    @ViewById
    Button email_sign_in_button;

    @ViewById
    EditText email;

    @ViewById
    LinearLayout login_progress_layout;

    @ViewById
    LinearLayout login_layout;

    @ViewById
    EditText password;

    @StringRes
    String error_title;

    @StringRes
    String access_denied;

    @StringRes
    String error_connection;

    @StringRes
    String server_error;

    @StringRes
    String error_loading;

    @StringRes
    String loading;

    @Pref
    MyPrefs_ myPrefs;

    @Extra
    boolean autologin;

    @InstanceState
    public Groups groups;

    @InstanceState
    public MyProfile myProfile;

    @InstanceState
    public TimestampData timestampData;

    @InstanceState
    public Cities cities;

    @InstanceState
    public Cities catalogCities;

    private boolean isErrorShowed = false;
    private int countDataLoaded = 0;
    private int POSTS = Constants.POSTS_AMOUNT;

    @AfterViews
    void init() {
        email_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });
        if (autologin) {
            checkLogin();
        }
    }

    @Click(R.id.register)
    void registerClick() {
        startActivity(new Intent(this, RegisterActivity_.class));
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_open_translate);
        finish();
    }

    void checkLogin() {

        if (NetHelper.isOnline(this)) {
            login_layout.setVisibility(View.GONE);
            login_progress_layout.setVisibility(View.VISIBLE);
            RequestParams params = new RequestParams();
            if (autologin) {
                params.put("email", myPrefs.email().get());
                params.put("pass", myPrefs.pass().get());
            } else {
                params.put("email", email.getText().toString());
                params.put("pass", StringHelper.md5(password.getText().toString()));
            }

            RestClient.post(RestClient.URL_API + RestClient.URL_API_LOGIN, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    String result = response.toString();

                    final Type objectCPD = new TypeToken<Login>() {}.getType();
                    Login login = new Gson().fromJson(result, objectCPD);
                    showResult(login);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    showError(server_error);
                }
            });
        } else {
            showError(error_connection);
        }

    }

    @UiThread
    void showError(String messageError) {
        POSTS = Constants.POSTS_AMOUNT;
        countDataLoaded = 0;
        login_layout.setVisibility(View.VISIBLE);
        login_progress_layout.setVisibility(View.GONE);
        if (!isErrorShowed) {
            isErrorShowed = true;
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(error_title)
                    .setContentText(messageError)
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
            autologin = false;
        }
    }

    @UiThread
    void showResult(Login login) {
        try {
            if (login.state.equals("1")) {
                if (!autologin) {
                    myPrefs.user_id().put(login.data.id);
                    myPrefs.email().put(email.getText().toString());
                    myPrefs.pass().put(StringHelper.md5(password.getText().toString()));
                }

                registerDevice();
                loadData();

            } else {
                login_layout.setVisibility(View.VISIBLE);
                login_progress_layout.setVisibility(View.GONE);
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(error_title)
                        .setContentText(access_denied)
                        .setConfirmText(getString(R.string.accept_button))
                        .show();
                autologin = false;
            }
        } catch (Exception e){
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(error_title)
                    .setContentText(server_error)
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
            autologin = false;
        }
    }

    /**
     * Carga los datos al inicio de la app.
     */
    void loadData() {

        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());

        RestClient.get(RestClient.URL_API + RestClient.URL_API_GET_CITIES, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                countDataLoaded++;
                String result = response.toString();
                final Type objectCPD = new TypeToken<Cities>() {
                }.getType();
                cities = new Gson().fromJson(result, objectCPD);
                if (countDataLoaded == POSTS) {
                    startApp();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }

        });

        RestClient.get(RestClient.URL_API + RestClient.URL_API_CATALOG_CITIES, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                countDataLoaded++;
                String result = response.toString();
                final Type objectCPD = new TypeToken<Cities>() {
                }.getType();
                catalogCities = new Gson().fromJson(result, objectCPD);
                catalogCities.data.add("Online");
                if (countDataLoaded == POSTS) {
                    startApp();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });

        RestClient.post(RestClient.URL_API + RestClient.URL_API_TIMESTAMP, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                countDataLoaded++;
                String resultTimestamp = response.toString();
                final Type objectTimeStamp = new TypeToken<TimestampData>() {
                }.getType();
                timestampData = new Gson().fromJson(resultTimestamp, objectTimeStamp);
                if (countDataLoaded == POSTS) {
                    startApp();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });

        RestClient.post(RestClient.URL_API + RestClient.URL_API_GROUPS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                String result = response.toString();
                final Type objectCPD = new TypeToken<Groups>() {
                }.getType();
                groups = new Gson().fromJson(result, objectCPD);
                saveGroupsInDB(); // Guardo los grupos en la base de datos
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });

        RestClient.post(RestClient.URL_API + RestClient.URL_API_PROFILE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                countDataLoaded++;
                String result = response.toString();
                final Type objectCPD = new TypeToken<MyProfile>() {
                }.getType();
                myProfile = new Gson().fromJson(result, objectCPD);
                myPrefs.myPoints().put(myProfile.data.points);
                myPrefs.isMonitor().put(myProfile.data.use_like.equals("monitor"));

                if (countDataLoaded == POSTS) {
                    startApp();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });

        RestClient.post(RestClient.URL_API + RestClient.URL_API_NOTIFICATIONS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                countDataLoaded++;
                String result = response.toString();
                final Type objectCPD = new TypeToken<Notifications>() {
                }.getType();
                Notifications notifications = new Gson().fromJson(result, objectCPD);
                if (notifications.data.size() == 2) {
                    myPrefs.notifications().put(notifications.data.get(0).value);
                    myPrefs.civiclubNotifications().put(notifications.data.get(1).value);
                    myPrefs.notificationsId().put(notifications.data.get(0).setting.id);
                    myPrefs.civiclubNotificationsId().put(notifications.data.get(1).setting.id);
                }
                if (countDataLoaded == POSTS) {
                    startApp();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });

    }

    private void startApp() {
        int new_user = -1;
        try {
            new_user = myPrefs.new_user().get();
        } catch (ClassCastException e) {
            myPrefs.new_user().remove();
            myPrefs.new_user().put(new_user);
        }

        Intent i = new Intent(this, SelectGroupActivity_.class);
        switch (new_user) {
            case -1: // Tutorial off
                break;
            case 0: // Inicio del tutorial
                i = new Intent(this, TutorialActivity_.class);
                break;
            case 1: // Pantalla mysettings en tutorial
                i = new Intent(this, MySettingsActivity_.class);
                break;
            case 2: // Pantalla nuevo niño en tutorial
                i = new Intent(this, ChildrenActivity_.class);
                break;
        }

        i.putExtra("groups", groups);
        i.putExtra("myProfile", myProfile);
        i.putExtra("timestampData", timestampData);
        i.putExtra("cities", cities);
        i.putExtra("catalogCities", catalogCities);
        startActivity(i);
    }

    @Background
    void saveGroupsInDB() {
        if(groups.data.size() > 0) {
            ChatCreator chatCreator = new ChatCreator(this, true);
            for(Group group : groups.data) {
                Group myGroup = Group.getGroupById(group.id);
                try {
                    // Crea el grupo en la db
                    if (myGroup == null) {
                        myGroup = group;
                    }
                    // Actualiza el grupo
                    else {
                        myGroup.isMonitor = group.isMonitor;
                        myGroup.visibility = group.visibility;
                        myGroup.name = group.name;
                        myGroup.hasride = group.hasride;
                        myGroup.admin = group.admin;
                        myGroup.city = group.city;
                        myGroup.ride_id = group.ride_id;
                        myGroup.school = group.school;
                    }

                    myGroup.save();

                    if (myGroup.chat == null) {
                        //Crea el chat
                        POSTS ++;
                        chatCreator.setOnChatListener(this);
                        chatCreator.createOrGetChat(group.id);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        countDataLoaded++;
        if (countDataLoaded == POSTS) {
            startApp();
        }

    }

    void registerDevice() {

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("device_token", myPrefs.res_id().get());
        params.put("device_id", Secure.getString(getContentResolver(), Secure.ANDROID_ID));
        params.put("device", "Android");

        RestClient.post(RestClient.URL_API + RestClient.URL_API_REGISTER_DEVICE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("state").equals("1")) {
                        countDataLoaded++;
                        if (countDataLoaded == POSTS) {
                            startApp();
                        }
                    } else {
                        showError(server_error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(server_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onChatCreated() {
        countDataLoaded++;
        if (countDataLoaded == POSTS) {
            startApp();
        }
    }
}