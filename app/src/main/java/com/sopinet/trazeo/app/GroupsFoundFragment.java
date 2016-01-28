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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

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

    ListView groupsList;
    LinearLayout search_group_layout;
    LinearLayout filterSpinner;
    Spinner spinnerToolbar;

    Groups groups;
    ArrayList<Group> groupsFiltered;
    ArrayList<String> schools;

    //Animations
    Animation slide_down;
    Animation slide_up;

    boolean isSetupSpinnerFirstTime = true;


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
        schools = new ArrayList<>();
        obtainGroups(fromCity, city);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_groups_found, container, false);

        groupsList = (ListView) root.findViewById(R.id.groupsList);
        search_group_layout = (LinearLayout) root.findViewById(R.id.search_group_layout);
        filterSpinner = (LinearLayout) root.findViewById(R.id.filterSpinner);
        spinnerToolbar = (Spinner) root.findViewById(R.id.spinnerToolbar);

        setupAnimations();
        progressDialog(true);

        return root;
    }

    void setupAnimations() {
        //Load animation
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.groups_found, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                buildHelpDialog();
                return true;
            case R.id.filter:
                if (schools.size() > 0) {
                    if (filterSpinner.getVisibility() == View.VISIBLE) {
                        setFilterLayout(false);
                        setGroupFilter(context.getString(R.string.school_filter));
                        spinnerToolbar.setSelection(0);
                    } else {
                        setFilterLayout(true);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setupSpinner() {
        schools.add(context.getString(R.string.school_filter));
        for (Group group : groups.data) {
            if (!group.school.trim().equals("")) {
               if (!schoolExists(group.school)) {
                   schools.add(group.school);
               }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.simple_spinner_row, schools);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToolbar.setAdapter(dataAdapter);
        spinnerToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setGroupFilter(spinnerToolbar.getSelectedItem().toString());
                if (!isSetupSpinnerFirstTime)
                    setFilterLayout(false);
                isSetupSpinnerFirstTime = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean schoolExists(String school) {
        for (String sch : schools) {
            if (school.equals(sch)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Shows / hides filter bar
     * @param visible visible
     */
    public void setFilterLayout(boolean visible) {
        if (visible) {
            filterSpinner.setVisibility(View.VISIBLE);
            filterSpinner.startAnimation(slide_down);
        } else {
            filterSpinner.startAnimation(slide_up);
            filterSpinner.setVisibility(View.GONE);
        }
    }

    private void setGroupFilter(String school) {
        groupsFiltered.clear();
        if (!school.equals(context.getString(R.string.school_filter))) {
            for (Group group : groups.data) {
                if (group.school.equals(school)) {
                    groupsFiltered.add(group);
                }
            }
        } else {
            groupsFiltered.addAll(groups.data);
        }
        showGroups();
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
                groupsFiltered = new ArrayList<>();
                groupsFiltered.addAll(groups.data);

                showGroups();
                setupSpinner();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showErrorBackDialog();
            }
        });
    }

    void showGroups() {
        if (groupsFiltered != null) {

            ArrayList<Group> cityGroups = new ArrayList<>();
            for (Group group : groupsFiltered) {
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
            groupsList.setVisibility(View.GONE);
            search_group_layout.setVisibility(View.VISIBLE);
        } else {
            groupsList.setVisibility(View.VISIBLE);
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
