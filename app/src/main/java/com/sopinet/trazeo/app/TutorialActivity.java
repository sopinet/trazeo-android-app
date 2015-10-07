package com.sopinet.trazeo.app;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.MyProfile;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.PagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_tutorial)
public class TutorialActivity extends ActionBarActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    ViewPager pager;

    @ViewById
    LinearLayout goMySettingsTutorial;

    @ViewById
    TextView tutorialButton;

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


    @AfterViews
    void init() {
        if (myPrefs.new_user().get() == -1 || myPrefs.new_user().get() == 3) {
            goMySettingsTutorial.setVisibility(View.GONE);
        }

        // Create an adapter with the fragments we show on the ViewPager
        PagerAdapter adapter = new PagerAdapter(
                getSupportFragmentManager(), null, null);
        adapter.addFragment(TutorialFragment.newInstance(0));
        adapter.addFragment(TutorialFragment.newInstance(1));
        adapter.addFragment(TutorialFragment.newInstance(2));
        adapter.addFragment(TutorialFragment.newInstance(3));
        adapter.addFragment(TutorialFragment.newInstance(4));
        pager.setAdapter(adapter);
        //Bind the circle indicator to the adapter
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        pager.setCurrentItem(0);
    }

    @Click(R.id.goMySettingsTutorial)
    public void goMySettingsTutorialClick(){
        myPrefs.new_user().put(1);
        Intent  i = new Intent(this, MySettingsActivity_.class);
        i.putExtra("groups", groups);
        i.putExtra("myProfile", myProfile);
        i.putExtra("timestampData", timestampData);
        i.putExtra("cities", cities);
        i.putExtra("catalogCities", catalogCities);
        startActivity(i);
        finish();
    }

}
