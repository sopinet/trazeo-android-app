package com.sopinet.trazeo.app;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.sopinet.trazeo.app.gson.Member;
import com.sopinet.trazeo.app.helpers.MembersAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;

import junit.framework.Assert;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;

@EActivity(R.layout.activity_members)
public class MembersActivity extends AppCompatActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ListView lvMembers;

    @ViewById
    TextView tvMembersSize;

    @Extra
    ArrayList<Member> members;

    MembersAdapter adapter;

    @AfterViews
    void init() {
        configureToolBar();
        String memberSize = members.size() + "";
        tvMembersSize.setText(memberSize);
        adapter = new MembersAdapter(this, R.layout.member_item, members);
        lvMembers.setAdapter(adapter);
    }

    private void configureToolBar() {
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void onBack() {
        finish();
        overridePendingTransition(R.anim.activity_close_translate, R.anim.activity_close_scale);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
