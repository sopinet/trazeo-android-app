package com.sopinet.trazeo.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.sopinet.trazeo.app.helpers.MyPrefs_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import cn.pedant.SweetAlert.SweetAlertDialog;


@EActivity(R.layout.activity_points)
public class PointsActivity extends ActionBarActivity{

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    TextView where_exchange_desc;

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void init() {
        toolbar.setTitle(getString(R.string.my_points2) + myPrefs.myPoints().get());
        setSupportActionBar(toolbar);
        configureBar();
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @UiThread
    void showError() {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_connection))
                .setContentText(getString(R.string.error_connection_desc))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
