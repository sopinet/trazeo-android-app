package com.sopinet.trazeo.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonitorDataFragment extends Fragment {
    // TODO: Rename and change types and number of parameters
    public static MonitorDataFragment newInstance() {
        return new MonitorDataFragment();
    }
    public MonitorDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitor_data, container, false);



        // Inflate the layout for this fragment
        return root;
    }
}