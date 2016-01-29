package com.sopinet.trazeo.app;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.StringHelper;
import com.sopinet.trazeo.app.gson.Login;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;


@EActivity(R.layout.activity_register)
public class RegisterActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    EditText email;

    @ViewById
    EditText password;

    @ViewById
    EditText confirmPassword;

    @Pref
    MyPrefs_ myPrefs;

    SweetAlertDialog pDialog;

    @AfterViews
    void init(){
        setSupportActionBar(toolbar);
        configureBar();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.green_trazeo_5));
        pDialog.setTitleText(getString(R.string.registering_user));
        pDialog.setCancelable(false);
    }

    private void configureBar() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Click(R.id.email_register_button)
    public void btnRegisterClick(){
        int check = checkCorrectInput();

        switch(check){
            case 0:
                openTermsDialog();
                break;
            case 1:
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(getString(R.string.fill_all))
                        .setConfirmText(getString(R.string.accept_button))
                        .show();
                break;
            case 2:
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(getString(R.string.pass_error))
                        .setConfirmText(getString(R.string.accept_button))
                        .show();
                break;
            case 3:
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(getString(R.string.error_invalid_email))
                        .setConfirmText(getString(R.string.accept_button))
                        .show();
                break;
        }
    }

    private int checkCorrectInput(){
        int check = 0;
        if (!isValidEmail(email.getText().toString().trim()))
            check = 3;
        else if (password.getText().toString().trim().equals("") ||
                confirmPassword.getText().toString().trim().equals(""))
            check = 1;
        else if(!password.getText().toString().trim().equals(confirmPassword.getText().toString().trim()))
            check = 2;

        return check;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Background
    void sendRegistration(){
        if (NetHelper.isOnline(this)) {
            RequestParams params = new RequestParams();
            params.put("username", email.getText().toString().trim());
            params.put("password", StringHelper.md5(password.getText().toString().trim()));

            RestClient.syncPost(RestClient.URL_API + RestClient.URL_API_REGISTER, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("REGISTER", "REGISTER: " + response.toString());
                    final Type objectCPD = new TypeToken<Login>() {}.getType();
                    Gson gson = new GsonBuilder()
                            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                    Login register = gson.fromJson(response.toString(), objectCPD);
                    showResult(register);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    pDialog.dismiss();
                    showError();
                    Log.d("ERROR", "REGISTER ERROR: " + errorResponse.toString());
                    //com.github.snowdream.android.util.Log.d("ERROR", "Sin conexión de datos.");
                }
            });
        }
    }

    @UiThread
    void showError() {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_connection))
                .setContentText(getString(R.string.error_connection_desc))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    @UiThread
    void showResult(Login register){
        pDialog.dismiss();
        if (register.state.equals("1")) {
            myPrefs.user_id().put(register.data.id);
            myPrefs.email().put(email.getText().toString());
            myPrefs.pass().put(StringHelper.md5(password.getText().toString()));
            myPrefs.new_user().put(0);
            myPrefs.rideTutorial().put(true);
            Intent i = new Intent(this, LoginSimpleActivity_.class);
            i.putExtra("autologin", true);
            startActivity(i);
            finish();
        } else {
            String error = getString(R.string.email_used);
            if (register.msg.equals(getString(R.string.ce_email_used))) {
                error = getString(R.string.email_used);
            }
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(error)
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        }
    }

    @UiThread
    public void openTermsDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View customDialogView = factory.inflate(R.layout.dialog_terms,
                (ViewGroup) findViewById(R.id.parent_dialog_terms));

        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog);
        dialog.setContentView(customDialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView btnAccept = (TextView) dialog.getWindow().findViewById(R.id.acceptTermsButton);
        TextView btnCancel = (TextView) dialog.getWindow().findViewById(R.id.cancelTermsButton);
        TextView tvUseTermsLink =(TextView) dialog.getWindow().findViewById(R.id.tvUseTermsLink);

        tvUseTermsLink.setMovementMethod(LinkMovementMethod.getInstance());

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pDialog.show();
                sendRegistration();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }});

        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, LoginSimpleActivity_.class));
                overridePendingTransition(R.anim.activity_close_translate, R.anim.activity_close_scale);
                finish();
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
