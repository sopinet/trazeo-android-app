package com.sopinet.trazeo.app;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sopinet.trazeo.app.helpers.MyPrefs_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_search_groups)
public class SearchGroupsActivity extends AppCompatActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @Extra @InstanceState
    ArrayList<String> cities;

    FragmentManager fragmentManager;



    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        configureBar();

        Collections.sort(cities);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.groups_fragment, SearchGroupsFragment.newInstance(cities))
                .commit();
    }

    private void configureBar() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void showGroupsFoundFragment(boolean fromCity, String city) {
        GroupsFoundFragment groupsFoundFragment = GroupsFoundFragment.newInstance(fromCity, city);
        fragmentManager.beginTransaction()
                .replace(R.id.groups_fragment, groupsFoundFragment)
                .commit();
    }

    private void goBack() {
        Intent i = new Intent(this, SelectGroupActivity_.class);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
