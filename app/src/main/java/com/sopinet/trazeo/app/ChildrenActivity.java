package com.sopinet.trazeo.app;


import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sopinet.trazeo.app.gson.Children;
import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.MyProfile;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.ListedChildAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_children)
public class ChildrenActivity extends AppCompatActivity{

    @ViewById
    Toolbar toolbar;

    @Pref
    MyPrefs_ myPrefs;

    @Extra
    public Groups groups;

    @Extra
    public MyProfile myProfile;

    @Extra
    public TimestampData timestampData;

    @Extra
    public Cities cities;

    @Extra
    public Cities catalogCities;


    ActionBar actionBar;
    Children children;

    FragmentManager fragmentManager;
    ChildrenListFragment childrenListFragment;
    ManageChildFragment manageChildFragment;

    ListedChildAdapter adapter;

    SweetAlertDialog pDialog;

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        configureBar();
        this.fragmentManager = getSupportFragmentManager();
        if (myPrefs.new_user().get() != 2) {
            showChildrenListFragment(true);
        } else {
            showNewChildFragment("", "", "", "", "");
            buildHelpDialog();
        }

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText(getString(R.string.wait));
        pDialog.setCancelable(false);
    }

    public void showChildrenListFragment(boolean addToBackStack) {
        childrenListFragment = ChildrenListFragment.newInstance();
        if (addToBackStack) {
            this.fragmentManager.beginTransaction()
                    .replace(R.id.fragment, childrenListFragment)
                    .addToBackStack("childrenListFragment")
                    .commit();
        } else {
            this.fragmentManager.beginTransaction()
                    .replace(R.id.fragment, childrenListFragment)
                    .commit();
        }
    }

    public void showNewChildFragment(String childId, String childName, String scholl, String date_birth, String gender) {
        manageChildFragment = ManageChildFragment.newInstance(childId, childName, scholl, date_birth, gender);
        FragmentTransaction ft = this.fragmentManager.beginTransaction();
        if (myPrefs.new_user().get() != 2) {
            ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        }
        ft.replace(R.id.fragment, manageChildFragment)
                .addToBackStack("newChildFragment")
                .commit();

        toolbar.getMenu().clear();

        if(childId.equals("")) {
            this.toolbar.setTitle(getString(R.string.register_child));
        }
        else {
            this.toolbar.setTitle(getString(R.string.edit_child));
        }
    }

    public void removeChild(final String child_id) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.remove_child_desc))
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
                        params.put("id_child", child_id);

                        RestClient.post(RestClient.URL_API + RestClient.URL_API_REMOVE_CHILD, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                showChildrenListFragment(false);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                showError();
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

    private void configureBar() {
        this.actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void getUserChildren(final ListView childrenList, final ProgressWheel progressView) {
        RequestParams params = new RequestParams();
        params.add("email", myPrefs.email().get());
        params.add("pass", myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_GET_CHILDREN, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                final Type objectCPD = new TypeToken<Children>() {}.getType();
                children = gson.fromJson(response.toString(), objectCPD);
                showUserChildren(childrenList, progressView);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    @UiThread
    public void showUserChildren(ListView childrenList, ProgressWheel progressView) {
        this.adapter = new ListedChildAdapter(this, R.layout.listed_child_item, this.children.data);
        childrenList.setAdapter(adapter);
        progressView.setVisibility(View.GONE);
        childrenList.setVisibility(View.VISIBLE);
    }

    public void manageChilds(final String idChild, final String name, final String school, final String date, final String gender, final boolean goBack) {
        pDialog.show();
        final RequestParams params = new RequestParams();
        params.add("email", myPrefs.email().get());
        params.add("pass", myPrefs.pass().get());
        params.add("id_child", idChild);
        params.add("name", name);
        params.add("school", school);
        params.add("date", date);
        params.add("gender", gender);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MANAGE_CHILD, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //final Type objectCPD = new TypeToken<MinimalJSON>() {}.getType();
                //MinimalJSON minimal = new Gson().fromJson(response.toString(), objectCPD);
                pDialog.dismiss();
                if (goBack)
                    goBack();
                else {
                    new SweetAlertDialog(ChildrenActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.add_children))
                            .setCancelText(getString(R.string.yes))
                            .setConfirmText(getString(R.string.no))
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    manageChildFragment.childName.setText("");
                                    manageChildFragment.school.setText("");
                                    manageChildFragment.date.setText("");
                                    manageChildFragment.genderSpinner.setSelection(0);
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    myPrefs.new_user().put(3);
                                    Intent i = new Intent(ChildrenActivity.this, SelectGroupActivity_.class);
                                    i.putExtra("groups", groups);
                                    i.putExtra("myProfile", myProfile);
                                    i.putExtra("timestampData", timestampData);
                                    i.putExtra("cities", cities);
                                    i.putExtra("catalogCities", catalogCities);
                                    startActivity(i);
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                pDialog.dismiss();
                showError();
            }
        });
    }

    public void showError() {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_title))
                .setContentText(getString(R.string.error_connection))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    private void goBack() {
        if (myPrefs.new_user().get() == 2) {
            myPrefs.new_user().put(1);
            Intent i = new Intent(this, MySettingsActivity_.class);
            i.putExtra("groups", groups);
            i.putExtra("myProfile", myProfile);
            i.putExtra("timestampData", timestampData);
            i.putExtra("cities", cities);
            i.putExtra("catalogCities", catalogCities);
            startActivity(i);
            finish();
        } else {
            if(fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStack();
                toolbar.inflateMenu(R.menu.children);
            } else {
                finish();
            }
        }
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.children_tutorial_help))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (myPrefs.new_user().get() != 2) {
            getMenuInflater().inflate(R.menu.children, menu);
        } else {
            getMenuInflater().inflate(R.menu.children_tutorial, menu);
            this.toolbar.setTitle(getString(R.string.register_child));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_child:
                showNewChildFragment("", "", "", "", "");
                return true;
            case android.R.id.home:
                goBack();
                return true;
            case R.id.children_help:
                buildHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

}
