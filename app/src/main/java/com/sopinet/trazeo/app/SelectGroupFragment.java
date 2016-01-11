package com.sopinet.trazeo.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.chat.ChatCreator;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gson.Children;
import com.sopinet.trazeo.app.gson.CreateRide;
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.MyPoints;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.ChildSpinnerAdapter;
import com.sopinet.trazeo.app.helpers.Constants;
import com.sopinet.trazeo.app.helpers.GroupAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;


@EFragment
public class SelectGroupFragment extends Fragment {

    LinearLayout groups_progress_layout;
    LinearLayout main_groups_layout;

    Groups groups;

    SelectGroupActivity context;

    MyPrefs_ myPrefs;

    Dialog dialogJoinDisjoin;

    @InstanceState
    TimestampData timestampData;

    @InstanceState
    long serverTimestamp;

    @InstanceState
    long localTimestamp;

    String error_loading;

    String server_error;

    String no_children;

    GroupAdapter groupAdapter;

    ListView listSelectGroup;

    public static SelectGroupFragment newInstance() {
        // Instantiate a new fragment
        SelectGroupFragment fragment = new SelectGroupFragment();
        // Save the parameters
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    public SelectGroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        context = (SelectGroupActivity) getActivity();
        myPrefs = context.myPrefs;
        groups = context.groups;
        timestampData = context.timestampData;
        localTimestamp = System.currentTimeMillis();
        serverTimestamp = parseFromStringToLong(timestampData.data);
        error_loading = getString(R.string.error_loading);
        server_error = getString(R.string.server_error);
        no_children = getString(R.string.no_children);

        if (groups.data.size() > 0) {
            if (myPrefs.new_user().get() != -1) {
                myPrefs.new_user().put(-1);
            }
            for (Group group : groups.data) {
                Group myGroup = Group.getGroupById(group.id);

                if (myGroup.hasride.equals("true") && myGroup.rideCreator.equals("owner")) {
                    goActivityMonitor(myGroup);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (broadcastReceiver != null)
            context.registerReceiver(broadcastReceiver, new IntentFilter("com.sopinet.trazeo.app.chatmessage"));
        if (listSelectGroup != null && groupAdapter != null) {
            listSelectGroup.setAdapter(groupAdapter);
        }
        if (main_groups_layout.getVisibility() == View.GONE)
            showGroupsLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_group, container, false);

        listSelectGroup = (ListView) root.findViewById(R.id.listSelectGroup);
        groups_progress_layout = (LinearLayout) root.findViewById(R.id.groups_progress_layout);
        main_groups_layout = (LinearLayout) root.findViewById(R.id.main_groups_layout);

        main_groups_layout.setVisibility(View.VISIBLE);
        groupAdapter = new GroupAdapter(getActivity(), this, R.layout.group_list_item, groups.data);
        listSelectGroup.setAdapter(groupAdapter);

        if (myPrefs.new_user().get() == 3) {
            buildHelpDialog();
        }

        return root;
    }

    public long parseFromStringToLong(String data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date parsedDate = new Date();

        try {
            parsedDate = dateFormat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate.getTime();
    }

    public void showGroupsLayout() {
        main_groups_layout.setVisibility(View.VISIBLE);
        groups_progress_layout.setVisibility(View.GONE);
    }

    public void hideGroupsLayout() {
        main_groups_layout.setVisibility(View.GONE);
        groups_progress_layout.setVisibility(View.VISIBLE);
    }

    /**
     * Crea un nuevo paseo
     *
     * @param id_group id del grupo
     */
    public void createRide(final String id_group) {
        hideGroupsLayout();

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("id_group", id_group);
        params.put("latitude", "");
        params.put("longitude", "");
        RestClient.post(RestClient.URL_API + RestClient.URL_API_RIDE_CREATE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Group group = Group.getGroupById(id_group);
                goGroup(response.toString(), group);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Group group = Group.getGroupById(id_group);
                group.rideCreator = "owner";
                group.save();
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Group group = Group.getGroupById(id_group);
                group.rideCreator = "owner";
                group.save();
                showError(server_error);
            }
        });
    }

    @UiThread
    void goGroup(String result, Group group) {
        final Type objectCPD = new TypeToken<CreateRide>() {}.getType();
        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
        CreateRide createRide = gson.fromJson(result, objectCPD);

        try {
            // No tiene permisos para iniciar el paseo
            if (createRide.data.id_ride.equals("-1")) {
                buildCantInitRideDialog();
                return;
            }

            group.hasride = "true";
            group.ride_id = createRide.data.id_ride;
            group.rideCreator = "owner";
            group.save();
            goActivityMonitor(group);
        } catch (Exception e) {
            showError(error_loading);
        }
    }

    void goActivityMonitor(Group group) {
        Intent i = new Intent(getActivity(), MonitorActivity_.class);
        i.putExtra("serverTimestamp", serverTimestamp);
        i.putExtra("localTimestamp", localTimestamp);
        i.putExtra("myProfileName", context.myProfile.data.name);
        i.putExtra("groupId", group.id);
        startActivityForResult(i, Constants.MONITORACTIVITY);
    }

    void goActivitySee(String rideId) {
        Intent intent = new Intent(getActivity(), SeeActivity_.class);
        intent.putExtra("rideId", rideId);
        startActivityForResult(intent, Constants.SEEACTIVITY);
    }

    private void goNewGroup() {
        Intent i = new Intent(getActivity(), NewGroupActivity_.class);
        startActivityForResult(i, Constants.NEWGROUPACTIVITY);
    }

    public void refreshGroups() {
        final Fragment fragmentContext = this;
        hideGroupsLayout();
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        RestClient.post(RestClient.URL_API + RestClient.URL_API_GROUPS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                final Type objectCPD = new TypeToken<Groups>() {
                }.getType();
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                groups = gson.fromJson(result, objectCPD);

                for (Group group : groups.data) {
                    Group myGroup = Group.getGroupById(group.id);
                    try {
                        if (myGroup == null) {
                            myGroup = group;
                            myGroup.save();
                            createChat(group.id);
                        } else {
                            myGroup.isMonitor = group.isMonitor;
                            myGroup.visibility = group.visibility;
                            myGroup.name = group.name;
                            myGroup.hasride = group.hasride;
                            myGroup.admin = group.admin;
                            myGroup.city = group.city;
                            myGroup.school = group.school;
                            myGroup.ride_id = group.ride_id;
                            myGroup.save();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                groupAdapter = new GroupAdapter(context, fragmentContext, R.layout.group_list_item, groups.data);
                listSelectGroup.setAdapter(groupAdapter);
                showGroupsLayout();
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

    void createChat(String groupId){
        ChatCreator chatCreator = new ChatCreator(getActivity(), false);
        chatCreator.createOrGetChat(groupId);
    }

    public void buildDisjoinDialog(final String id_group) {

        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.you_unlink_group))
                .setCancelText(getString(R.string.yes))
                .setConfirmText(getString(R.string.no))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        unLinkGroup(id_group);
                        sDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }

    public void buildJoinDisJoinChildDialog(final String id_group, final boolean join) {
        RequestParams params = new RequestParams();
        params.add("email", myPrefs.email().get());
        params.add("pass", myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_GET_CHILDREN, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                final Type objectCPD = new TypeToken<Children>() {
                }.getType();
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                Children children = gson.fromJson(response.toString(), objectCPD);
                showJoinDisjoinChildDialog(children, id_group, join);
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

    @UiThread
    public void showJoinDisjoinChildDialog(Children children, final String id_group, final boolean join) {
        if (children != null) {
            LayoutInflater factory = LayoutInflater.from(context);
            final View customDialogView = factory.inflate(
                    R.layout.dialog_joinchild, (ViewGroup) context.findViewById(R.id.parent_joinchild_dialog));

            dialogJoinDisjoin = new Dialog(context, R.style.Theme_Dialog);
            dialogJoinDisjoin.setContentView(customDialogView);
            dialogJoinDisjoin.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            final Spinner spinner = (Spinner) dialogJoinDisjoin.findViewById(R.id.contactsListSpinner);

            ChildSpinnerAdapter adapter = new ChildSpinnerAdapter(context, R.layout.child_spinner_row, children);
            spinner.setAdapter(adapter);

            TextView okButton = (TextView) dialogJoinDisjoin.findViewById(R.id.okButton);
            okButton.setText(join ? getString(R.string.join) : getString(R.string.disjoin));

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestParams params = new RequestParams();
                    params.add("email", myPrefs.email().get());
                    params.add("pass", myPrefs.pass().get());
                    params.add("id_group", id_group);
                    params.add("id_child", ((EChild) spinner.getSelectedItem()).id);
                    params.add("add", join ? "true" : "false");

                    RestClient.post(RestClient.URL_API + RestClient.URL_API_JOIN_DISJOIN_CHILD, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String msg = null;
                            try {
                                msg = response.getString("msg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            showJoinDisJoinChildResult(msg, join);
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
            });

            TextView cancelButton = (TextView) dialogJoinDisjoin.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogJoinDisjoin.dismiss();
                }
            });

            dialogJoinDisjoin.show();
        } else {
            showError(no_children);
        }

    }

    @UiThread
    public void showJoinDisJoinChildResult(String response, boolean join) {
        switch (response) {

            //si el niño esta en el grupo y se intenta añadir-> Child on group: true
            //si el niño no esta en el grupo y se intenta desvincular-> Child on group: false

            case "Ok":
                dialogJoinDisjoin.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText(getString(R.string.info))
                        .setContentText(join ? getString(R.string.child_joined) :
                                getString(R.string.child_disjoined))
                        .setConfirmText(getString(R.string.understood))
                        .setCustomImage(R.drawable.mascota3)
                        .show();
                break;
            case "-1":
                showError(getString(R.string.petition_denied));
                break;
            case "Access Denied":
                showError(getString(R.string.access_denied));
                break;
            case "Group not found":
                showError(getString(R.string.group_doesnt_exit));
                break;
            case "The child doesn't exist":
                showError(getString(R.string.child_doesnt_exist));
                break;
            case "The parent is not the tutor":
                showError(getString(R.string.parent_not_tutor));
                break;
            case "Child on group: 1":
                showError(getString(R.string.child_on_group));
                break;
        }
    }

    private void buildFinishRideDialog() {

        new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.finish_ride))
                .setContentText(getString(R.string.finish_ride_desc))
                .setConfirmText(getString(R.string.accept_button))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    private void buildCantInitRideDialog() {

        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.official_group))
                .setContentText(getString(R.string.official_group_desc))
                .setConfirmText(getString(R.string.accept_button))
                .show();
        showGroupsLayout();
    }

    private void buildHelpDialog() {
        if (groups.data.size() > 0) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText(getString(R.string.info))
                    .setContentText(getString(R.string.info_select_group))
                    .setConfirmText(getString(R.string.understood))
                    .setCustomImage(R.drawable.mascota3)
                    .show();
        } else new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_select_group_new))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @UiThread
    public void removeGroup(final String group_id) {
        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.remove_group_desc))
                .setCancelText(getString(R.string.yes))
                .setConfirmText(getString(R.string.no))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismiss();
                        RequestParams params = new RequestParams();
                        params.put("email", myPrefs.email().get());
                        params.put("pass", myPrefs.pass().get());
                        params.put("id_group", group_id);

                        RestClient.post(RestClient.URL_API + RestClient.URL_API_REMOVE_GROUP, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                refreshGroups();
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
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }

    @UiThread
    public void showInviteDialog(final String id) {

        LayoutInflater factory = LayoutInflater.from(context);
        final View customDialogView = factory.inflate(R.layout.dialog_invite,
                (ViewGroup)context.findViewById(R.id.parent_invite_dialog));

        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(customDialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        final EditText etEmail = (EditText) dialog.findViewById(R.id.etEmail);
        TextView inviteOkButton = (TextView) dialog.findViewById(R.id.inviteOkButton);

        inviteOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidEmail(etEmail.getText().toString())) {
                    dialog.dismiss();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.error_title))
                            .setContentText(getString(R.string.error_invalid_email))
                            .setConfirmText(getString(R.string.accept_button))
                            .show();
                } else {
                    dialog.cancel();
                    SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
                    pDialog.setTitleText(getString(R.string.sending));
                    pDialog.setCancelable(false);
                    sendInvite(id, etEmail.getText().toString());
                }
            }
        });

        TextView inviteCancelButton = (TextView) dialog.findViewById(R.id.inviteCancelButton);
        inviteCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Envia una invitación a un grupo
     *
     * @param id           id
     * @param email_invite email_invite
     */
    void sendInvite(String id, String email_invite) {
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("id_group", id);
        params.put("email_invite", email_invite);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_INVITE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showInviteResult();
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

    @UiThread
    void showInviteResult() {
        new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.send_invitation_ok))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    /**
     * Desvicular de un grupo
     *
     * @param id id
     */
    public void unLinkGroup(String id) {
        hideGroupsLayout();
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("id_group", id);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_DISJOIN, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                refreshGroups();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 0) {
            switch (requestCode) {
                case Constants.MONITORACTIVITY:
                    //Muestra el mensaje de fin de paseo
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buildFinishRideDialog();
                        }
                    }, 1000);
                    updatePoints();
                    showGroupsLayout();
                    break;
                case Constants.NEWGROUPACTIVITY:
                case Constants.EDITGROUPACTIVITY:
                case Constants.SEEACTIVITY:
                case Constants.SEARCHGROUPACTIVITY:
                    refreshGroups();
                    break;
            }
        } else {
            if (requestCode == Constants.MONITORACTIVITY) {
                for (Group group : groups.data) {
                    Group myGroup = Group.getGroupById(group.id);

                    if (myGroup.hasride.equals("true") && myGroup.rideCreator.equals("owner")) {
                        goActivityMonitor(myGroup);
                    }
                }
            } else if (requestCode == Constants.SEEACTIVITY) {
                refreshGroups();
            }
        }
    }

    private void updatePoints() {
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MY_POINTS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                final Type objectCPD = new TypeToken<MyPoints>() {
                }.getType();
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                MyPoints points = gson.fromJson(result, objectCPD);
                myPrefs.myPoints().put(points.data.points);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.select_group, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_disconnect:
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.are_you_sure))
                        .setContentText(getString(R.string.disconnect_desc))
                        .setCancelText(getString(R.string.yes))
                        .setConfirmText(getString(R.string.no))
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                myPrefs.email().put("");
                                myPrefs.pass().put("");
                                myPrefs.user_id().put("");
                                startActivity(new Intent(getActivity(), LoginSimpleActivity_.class));
                                sDialog.dismiss();
                                getActivity().finish();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case R.id.refresh:
                refreshGroups();
                return true;
            case R.id.new_group:
                goNewGroup();
                return true;
            case R.id.help:
                buildHelpDialog();
                return true;
            case R.id.action_search:
                Intent i = new Intent(getActivity(), SearchGroupsActivity_.class);
                i.putExtra("cities", context.cities.cities);
                startActivityForResult(i, Constants.SEARCHGROUPACTIVITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @UiThread
    public void showError(String message_error) {
        showGroupsLayout();
        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_title))
                .setContentText(message_error)
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    public void hasLocationProvider(final Group group) {

        if (group.isMonitor.equals("true")) {
            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.both_desactived))
                        .setContentText(getString(R.string.both_desactived_desc))
                        .setCancelText(getString(R.string.yes))
                        .setConfirmText(getString(R.string.no))
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                sDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).show();
            } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.network_desactived))
                        .setContentText(getString(R.string.network_desactived_desc))
                        .setCancelText(getString(R.string.yes))
                        .setConfirmText(getString(R.string.no))
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                sDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).show();
            } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.gps_desactived))
                        .setContentText(getString(R.string.gps_desactived_desc))
                        .setCancelText(getString(R.string.yes))
                        .setConfirmText(getString(R.string.no))
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                sDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).show();
            } else {
                if (group.hasride.equals("false")) {
                    createRide(group.id);
                } else if (group.hasride.equals("true") && group.rideCreator.equals("owner")) {
                    goActivityMonitor(group);
                } else if (group.hasride.equals("true") && !group.rideCreator.equals("owner")) {
                    goActivitySee(group.ride_id);
                } else {
                    showError(context.getString(R.string.has_a_ride));
                }
            }
        } else {
            if (group.hasride.equals("true") && !group.rideCreator.equals("owner")) {
                goActivitySee(group.ride_id);

            } else {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(context.getString(R.string.access_denied))
                        .setContentText(context.getString(R.string.not_permission))
                        .setConfirmText(context.getString(R.string.accept_button))
                        .show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (broadcastReceiver != null)
            context.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Receptor de mensajes
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SelectGroupFragment.this.onResume();
        }
    };

}
