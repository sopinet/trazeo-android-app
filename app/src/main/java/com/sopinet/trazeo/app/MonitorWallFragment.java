package com.sopinet.trazeo.app;

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
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.gson.EComment;
import com.sopinet.trazeo.app.helpers.CommentAdapter;

import java.util.Collections;

/**
 * Created by david on 19/05/14.
 */
public class MonitorWallFragment extends Fragment {

    private static final String Adata = "mdata";
    private String mdata;
    ListView listComments;

    public static MonitorWallFragment newInstance(String data) {
        MonitorWallFragment fragment = new MonitorWallFragment();
        Bundle args = new Bundle();
        args.putString(Adata, data);
        fragment.setArguments(args);

        return new MonitorWallFragment();
    }

    public MonitorWallFragment() {
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
        View root = inflater.inflate(R.layout.fragment_monitor_wall, container, false);

        // Creamos diccionario
        BindDictionary<EComment> dict = new BindDictionary<EComment>();
        dict.addStringField(R.id.comment_body,
                new StringExtractor<EComment>() {
                    @Override
                    public String getStringValue(EComment item, int position) {
                        return item.body;
                    }
                }
        );

        // Creamos adaptador
        CommentAdapter adapter = new CommentAdapter(root.getContext(),
                R.layout.comment_item, MonitorActivity.wall.data);

        // Asignamos el adaptador a la vista
        listComments = (ListView) root.findViewById(R.id.listComments);
        listComments.setAdapter(adapter);

        // Inflate the layout for this fragment
        return root;
    }
}
