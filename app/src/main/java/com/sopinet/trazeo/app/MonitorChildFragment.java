package com.sopinet.trazeo.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.extractors.StringExtractor;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.helpers.ChildAdapter;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

@EFragment(R.layout.fragment_monitor_child)
public class MonitorChildFragment extends Fragment {
    private static final String Adata = "mdata";
    private String mdata;
    ListView listChildren;
    View root;

    // TODO: Rename and change types and number of parameters
    public static MonitorChildFragment newInstance(String data) {
        MonitorChildFragment fragment = new MonitorChildFragment();
        Bundle args = new Bundle();
        args.putString(Adata, data);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorChildFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mdata = getArguments().getString(Adata);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_monitor_child, container, false);

        // Creamos diccionario
        BindDictionary<EChild> dict = new BindDictionary<EChild>();
        dict.addStringField(R.id.titleCHILD,
                new StringExtractor<EChild>() {
                    @Override
                    public String getStringValue(EChild item, int position) {
                        return item.nick;
                    }
                }
        );
        /*dict.addStringField(R.id.descriptionCHILD,
            new StringExtractor<EChild>() {
                @Override
                public String getStringValue(EChild item, int position) {
                    return item.gender + " - " + item.date_birth;
                }
            }); */

        // Creamos adaptador
        //FunDapter<EChild> adapter = new FunDapter<EChild>(getActivity(),
        //        MonitorActivity.ride.data.group.childs, R.layout.child_item, dict);

        // Creamos adaptador
        ChildAdapter adapter = new ChildAdapter(getActivity(),
                R.layout.child_item, MonitorActivity.ride.data.group.childs, (MonitorActivity) this.getActivity());

        // Asignamos el adaptador a la vista
        listChildren = (ListView) root.findViewById(R.id.listChildren);

        listChildren.setAdapter(adapter);

        listChildren.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final EChild echild = (EChild) adapterView.getItemAtPosition(position);
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkCHILD);
                checkbox.setChecked(!checkbox.isChecked());
                echild.setSelected(checkbox.isChecked());
                changeChild(echild);
            }
        });

        // Devolvemos la vista del Fragment
        return root;
    }

    @Background
    public void changeChild(EChild echild) {
        String url = "";
        if (echild.isSelected()) {
            echild.setSelected(true);
            url = ((MonitorActivity)getActivity()).myPrefs.url_api().get() + Var.URL_API_CHILDIN;
        } else {
            echild.setSelected(false);
            url = ((MonitorActivity)getActivity()).myPrefs.url_api().get() + Var.URL_API_CHILDOUT;
        }

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        String data = mdata;
        data += "&id_child=" + echild.id;
        data += "&latitude=" + lat;
        data += "&longitude=" + lon;

        String result = "";
        SimpleContent sc = new SimpleContent(this.getActivity(), "trazeo", 3);
        try {
            result = sc.postUrlContent(url, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        changeChildShow(echild);
        //Log.d("TEMA", result);
    }

    @UiThread
    void changeChildShow(EChild echild) {
        String msg = "";
        if (echild.isSelected()) {
            msg = "(" + echild.nick + ")" + " Niño registrado en este Paseo";
        } else {
            msg = "(" + echild.nick + ")" + " Niño desvinculado del Paseo";
        }
        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}