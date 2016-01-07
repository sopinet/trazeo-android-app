package com.sopinet.trazeo.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.MyProfile;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.Constants;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.NavAdapter;
import com.sopinet.trazeo.app.helpers.PagerAdapter;
import com.sopinet.trazeo.app.helpers.SlidingTabLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import cn.pedant.SweetAlert.SweetAlertDialog;

@EActivity(R.layout.activity_select_group)
public class SelectGroupActivity extends AppCompatActivity {

    @Pref
    public MyPrefs_ myPrefs;

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

    @InstanceState
    boolean isDataLoaded;

    @ViewById
    Toolbar toolbar;

    @ViewById
    public ViewPager group_pager;

    @ViewById
    SlidingTabLayout sliding_tabs;

    @ViewById
    LinearLayout select_group_layout;

    @ViewById
    LinearLayout fl_select_group;

    PagerAdapter adapter;

    public ActionBarDrawerToggle mDrawerToggle;

    @ViewById
    public DrawerLayout drawer_layout;

    @ViewById
    public ListView left_drawer;

    public View drawer_header;

    @StringRes
    String error_loading;

    @StringRes
    String error_connection;

    @StringRes
    String server_error;

    TextView tvMyName;

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        setNavigationDrawer();
        setViews();

        TextView tvMyAccount = (TextView) drawer_header.findViewById(R.id.my_account);
        tvMyName = (TextView) drawer_header.findViewById(R.id.my_name);
        tvMyAccount.setText(myPrefs.email().get());
        tvMyName.setText(myProfile.data.name);
    }

    private void setViews() {
        invalidateOptionsMenu();
        showSelectGroupLayout();
        // Create an adapter with the fragments we show on the ViewPager
        adapter = new PagerAdapter(getSupportFragmentManager(), "Mis grupos", "Catálogo puntos");
        adapter.addFragment(SelectGroupFragment.newInstance());
        adapter.addFragment(SearchCatalogFragment.newInstance());
        group_pager.setAdapter(adapter);
        sliding_tabs.setSelectedIndicatorColors(getResources().getColor(R.color.green_trazeo_3),
                getResources().getColor(R.color.green_trazeo_3));
        sliding_tabs.setBackgroundColor(getResources().getColor(R.color.green_trazeo_6));
        sliding_tabs.setCustomTabView(R.layout.custom_tab, 0);
        sliding_tabs.setViewPager(group_pager);
        group_pager.setCurrentItem(0);
    }

    public void showSelectGroupLayout() {
        fl_select_group.setVisibility(View.VISIBLE);
        select_group_layout.setVisibility(View.GONE);
    }

    private void setNavigationDrawer() {
        drawer_header = getLayoutInflater().inflate(R.layout.profile_drawer_header,
                (ViewGroup) findViewById(R.id.drawer_header));

        left_drawer.addHeaderView(drawer_header);

        NavAdapter navAdapter = new NavAdapter(this, getResources().getStringArray(R.array.my_profile));
        left_drawer.setAdapter(navAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                toolbar.setTitle(getString(R.string.app_name));
                selectItem(left_drawer.getCheckedItemPosition());
                left_drawer.setItemChecked(-1, true);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.setTitle(getString(R.string.my_profile_title));
            }
        };
        // Set the drawer toggle as the DrawerListener
        drawer_layout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @UiThread
    public void showError(String messageError) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(error_connection)
                .setContentText(messageError)
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 0) {
            if (requestCode == Constants.SETTINGSACTIVITY) {
                myProfile = data.getParcelableExtra("myprofile");
                tvMyName.setText(myProfile.data.name);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.refresh_only:
                startActivity(new Intent(this, LoginSimpleActivity_.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        if (isDataLoaded)
            showSelectGroupLayout();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void selectItem(int position) {
         switch (position) {
            case 0:
                startActivity(new Intent(this, ChildrenActivity_.class));
                break;
            case 1:
                startActivity(new Intent(this, PointsActivity_.class));
                break;
            case 2:
                Intent intentSettings = new Intent(this, MySettingsActivity_.class);
                intentSettings.putExtra("myProfile", myProfile);
                startActivityForResult(intentSettings, Constants.SETTINGSACTIVITY);
                break;
            case 3:
                Intent intentNotifications = new Intent(this, MyNotificationsActivity_.class);
                intentNotifications.putExtra("myProfile", myProfile);
                startActivity(intentNotifications);
                break;
            case 4:
                startActivity(new Intent(this, OtherServicesActivity_.class));
                break;
            case 5:
                startActivity(new Intent(this, TutorialActivity_.class));
                break;
        }
    }
}