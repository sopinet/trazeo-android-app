package com.sopinet.trazeo.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.helpers.Constants;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;


public class SearchCatalogFragment extends Fragment {

    MyPrefs_ myPrefs;

    SelectGroupActivity context;

    Spinner citiesSpinner;

    Groups groups;

    ArrayList<String> catalogCities;

    public static SearchCatalogFragment newInstance() {
        // Instantiate a new fragment
        SearchCatalogFragment fragment = new SearchCatalogFragment();
        // Save the parameters
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    public SearchCatalogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = (SelectGroupActivity) getActivity();
        this.catalogCities = context.catalogCities.data;
        this.myPrefs = context.myPrefs;
        this.groups = context.groups;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != 0) {
            switch (requestCode) {
                case Constants.CATALOGACTIVITY:
                    updatePoints();
                    break;
            }
        }
    }

    private void updatePoints() {
        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MY_POINTS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    myPrefs.myPoints().put(response.getString("points"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);

        citiesSpinner = (Spinner) root.findViewById(R.id.citiesSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, catalogCities);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesSpinner.setAdapter(dataAdapter);

        Button btnSearchCalatog = (Button) root.findViewById(R.id.btnSearchCalatog);
        btnSearchCalatog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CatalogActivity_.class);
                i.putExtra("city", citiesSpinner.getSelectedItem().toString());
                startActivityForResult(i, Constants.CATALOGACTIVITY);
            }
        });

        return root;
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_search_catalog))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_catalog, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                buildHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
