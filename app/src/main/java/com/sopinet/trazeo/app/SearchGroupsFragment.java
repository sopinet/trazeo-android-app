package com.sopinet.trazeo.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.sopinet.trazeo.app.helpers.MyPrefs_;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchGroupsFragment extends Fragment {
    private static final String CITIES = "cities";

    MyPrefs_ myPrefs;

    Spinner citiesSpinner;

    SearchGroupsActivity context;

    Button btnSearchGroups;
    Button btnSeeAll;

    ArrayList<String> cities;

    public static SearchGroupsFragment newInstance(ArrayList<String> cities) {
        SearchGroupsFragment fragment = new SearchGroupsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(CITIES, cities);
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    public SearchGroupsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cities = getArguments().getStringArrayList(CITIES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_groups, container, false);

        citiesSpinner = (Spinner) root.findViewById(R.id.search_cities_spinner);
        btnSearchGroups = (Button) root.findViewById(R.id.btnSearchGroups);
        btnSeeAll = (Button) root.findViewById(R.id.btnSeeAll);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, cities);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesSpinner.setAdapter(dataAdapter);

        btnSearchGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.showGroupsFoundFragment(true, citiesSpinner.getSelectedItem().toString());
            }
        });

        btnSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.showGroupsFoundFragment(false, citiesSpinner.getSelectedItem().toString());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = (SearchGroupsActivity) getActivity();
        this.myPrefs = context.myPrefs;
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_search_group))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_groups, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help:
                buildHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
