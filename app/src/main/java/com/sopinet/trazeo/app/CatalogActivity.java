package com.sopinet.trazeo.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.gson.Catalog;
import com.sopinet.trazeo.app.gson.CityCatalog;
import com.sopinet.trazeo.app.helpers.CatalogAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.segment.android.Analytics;
import io.segment.android.models.Props;

@EActivity(R.layout.activity_catalog)
public class CatalogActivity extends ActionBarActivity {


    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @ViewById
    LinearLayout catalogListLayout;

    @ViewById
    ListView catalogList;

    @ViewById
    LinearLayout search_catalog_layout;

    @Extra
    String city;

    CityCatalog cityCalalog;

    boolean isOnline;


    @AfterViews
    void init() {
        toolbar.setTitle(getString(R.string.catalog_title) + city);
        setSupportActionBar(toolbar);
        configureBar();
        catalogListLayout.setVisibility(View.GONE);
        search_catalog_layout.setVisibility(View.VISIBLE);
        Analytics.onCreate(this);
        Analytics.track("enter.searchCatalog.Android", new Props("email", myPrefs.email().get()));
        obtainGroups(city);
    }

    void obtainGroups(String city) {
        //Pido el catálogo de cualquier ciudad, ya que me devuelve también el online.
        if (city.equals("Online"))
            isOnline = true;
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("city", city);

        RestClient.get(RestClient.URL_API + RestClient.URL_API_CATALOG, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("GROUPS", "GROUPS: " + response.toString());
                Type objectCPD = new TypeToken<CityCatalog>() {}.getType();
                cityCalalog = new Gson().fromJson(response.toString(), objectCPD);
                showCatalog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showErrorBackDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showErrorBackDialog();
            }
        });
    }

    @UiThread
    void showCatalog() {
        CatalogAdapter adapter;
        if (isOnline) {
            for (int i=0; i < cityCalalog.data.internet.size() ; i++) {
                Catalog catalog = cityCalalog.data.internet.get(i);
                if (catalog.title.trim().equals("")) {
                    cityCalalog.data.internet.remove(i);
                }
            }
            adapter = new CatalogAdapter(this, R.layout.catalog_list_item, cityCalalog.data.internet);
        } else {
            adapter = new CatalogAdapter(this, R.layout.catalog_list_item, cityCalalog.data.local);
        }

        catalogList.setAdapter(adapter);
        catalogListLayout.setVisibility(View.VISIBLE);
        search_catalog_layout.setVisibility(View.GONE);
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @UiThread
    void showErrorBackDialog() {
        if (!isFinishing()) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_connection))
                    .setContentText(getString(R.string.error_connection_desc))
                    .setConfirmText(getString(R.string.accept_button))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_found_catalog))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    public void goLink(String URL) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        startActivity(browserIntent);
    }

    public void changePoints(final String catalog) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.catalog_petition))
                .setCancelText(getString(R.string.yes))
                .setConfirmText(getString(R.string.no))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismiss();
                        RequestParams params = new RequestParams();
                        params.put("email",myPrefs.email().get());
                        params.put("pass", myPrefs.pass().get());
                        params.put("id_catalog_item", catalog);

                        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHANGE_POINTS, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                String msg = null;
                                try {
                                    msg = response.getString("msg");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                petitionSended(msg);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                showError();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
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

    @UiThread
    public void showError() {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_connection))
                    .setContentText(getString(R.string.error_connection_desc))
                    .setConfirmText("Aceptar")
                    .show();
    }

    @UiThread
    public void petitionSended(String response) {
        switch (response) {
            case "Don't have enougth points":
                showErrorMessage("No tienes suficientes puntos");
                break;
            case "-1":
                showErrorMessage("No se ha podido enviar la petición");
                break;
            case "Catalog item not found":
                showErrorMessage("La oferta ya no se encuentra disponible");
                break;
            case "Access Denied":
                showErrorMessage("El usuario no es válido");
                break;
            default:
                new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText(getString(R.string.info))
                        .setContentText(getString(R.string.points_changed))
                        .setConfirmText(getString(R.string.understood))
                        .setCustomImage(R.drawable.mascota3)
                        .show();
        }
    }

    @UiThread
    public void showErrorMessage(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_title))
                .setContentText(message)
                .setConfirmText("Aceptar")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.help:
                buildHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onPause() {
        Analytics.activityPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.activityResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }
}
