package com.sopinet.trazeo.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.MinimalJSON;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.helpers.CityGroupAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

public class GroupsFoundFragment extends Fragment {
    private static final String FROMCITY = "fromCity";
    private static final String CITY = "city";

    MyPrefs_ myPrefs;

    private boolean fromCity;
    private String city;
    private SearchGroupsActivity context;

    LinearLayout groupsListLayout;
    ListView groupsList;
    LinearLayout search_group_layout;

    Groups groups;

    public static GroupsFoundFragment newInstance(boolean fromCity, String city) {
        GroupsFoundFragment fragment = new GroupsFoundFragment();
        Bundle args = new Bundle();
        args.putBoolean(FROMCITY, fromCity);
        args.putString(CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupsFoundFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromCity = getArguments().getBoolean(FROMCITY);
            city = getArguments().getString(CITY);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        context = (SearchGroupsActivity) getActivity();
        myPrefs = context.myPrefs;
        obtainGroups(fromCity, city);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_groups_found, container, false);

        groupsListLayout = (LinearLayout) root.findViewById(R.id.groupsListLayout);
        groupsList = (ListView) root.findViewById(R.id.groupsList);
        search_group_layout = (LinearLayout) root.findViewById(R.id.search_group_layout);

        progressDialog(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_groups, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                buildHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void obtainGroups(boolean fromCity, String city) {

        if(fromCity) {
            city = city.toLowerCase();
            city = Normalizer.normalize(city, Normalizer.Form.NFD);
            city = city.replaceAll("[^\\p{ASCII}]", "");
        } else {
            city = "all";
        }

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("city", city);
        params.put("object", "true");

        RestClient.get(RestClient.URL_API + RestClient.URL_API_SEARCH_GROUPS, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("GROUPS", "GROUPS: " + response.toString());
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                Type objectCPD = new TypeToken<Groups>() {}.getType();
                groups = gson.fromJson(response.toString(), objectCPD);
                showGroups();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showErrorBackDialog();
            }

        });
    }

    void showGroups() {
        if (groups != null && groups.data != null) {

            ArrayList<Group> cityGroups = new ArrayList<>();
            for (Group group : groups.data) {
                if(group.visibility == null || !group.visibility.equals("2"))
                    cityGroups.add(group);
            }

            CityGroupAdapter adapter = new CityGroupAdapter(getActivity(), this, R.layout.city_group_list_item, cityGroups);
            groupsList.setAdapter(adapter);
            progressDialog(false);
        } else {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(getString(R.string.server_error))
                    .setConfirmText(getString(R.string.accept_button))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    })
                    .show();
        }

    }

    void progressDialog(boolean enabled) {
        if (enabled) {
            groupsListLayout.setVisibility(View.GONE);
            search_group_layout.setVisibility(View.VISIBLE);
        } else {
            groupsListLayout.setVisibility(View.VISIBLE);
            search_group_layout.setVisibility(View.GONE);
        }
    }


    public void joinGroup(String id_group) {
        progressDialog(true);
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("id_group", id_group);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_JOIN_GROUP, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("JOIN", "JOIN: " + response.toString());
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                Type objectCPD = new TypeToken<MinimalJSON>() {}.getType();
                MinimalJSON state = gson.fromJson(response.toString(), objectCPD);
                GroupsFoundFragment.this.showJoinResult(state);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                GroupsFoundFragment.this.showErrorDialog();
            }

        });
    }

    void showJoinResult(MinimalJSON state){
        progressDialog(false);
        if (state.msg.equals("User it's already on group")){
            showHasJoinedDialog();
        } else if (state.state.equals("-1")) {
            showErrorDialog();
        } else {
            Intent i = new Intent(context, SelectGroupActivity_.class);
            getActivity().setResult(Activity.RESULT_OK, i);
            getActivity().finish();
        }
    }

    public void requestGroup(String id_group) {
        progressDialog(true);
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("id_group", id_group);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_REQUEST_GROUP, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("REQUEST", "REQUEST: " + response.toString());
                final Type objectCPD = new TypeToken<MinimalJSON>() {}.getType();
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                MinimalJSON state = gson.fromJson(response.toString(), objectCPD);
                GroupsFoundFragment.this.showRequestResult(state);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                GroupsFoundFragment.this.showErrorDialog();
            }

        });
    }


    private void showHasJoinedDialog() {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.attention))
                .setContentText(getString(R.string.has_joined))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    void showErrorDialog() {
        progressDialog(false);
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_connection))
                .setContentText(getString(R.string.error_connection_desc))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    void showErrorBackDialog() {
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_connection))
                .setContentText(getString(R.string.error_connection_desc))
                .setConfirmText(getString(R.string.accept_button))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        getActivity().finish();
                    }
                })
                .show();
    }

    void showRequestResult(MinimalJSON state){
        progressDialog(false);
        if (state.msg.equals("User it's already on group")) {
            showHasJoinedDialog();
        } else if (state.msg.equals("Join to group request has been did before")) {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.attention))
                    .setContentText(getString(R.string.has_succesful))
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        } else if(state.msg.equals("Group doesn't exist")) {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.attention))
                    .setContentText(getString(R.string.group_not_exists))
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        } else if (state.state.equals("-1")) {
            showErrorDialog();
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.petition_sended))
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        }
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_search_group))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }
}
