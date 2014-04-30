package com.sopinet.trazeo.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.sopinet.trazeo.app.gson.EChild;

public class MonitorChildFragment extends Fragment {
    // TODO: Rename and change types and number of parameters
    public static MonitorChildFragment newInstance() {
        return new MonitorChildFragment();
    }
    public MonitorChildFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitor_child, container, false);

        // Creamos diccionario
        BindDictionary<EChild> dict = new BindDictionary<EChild>();
        dict.addStringField(R.id.titleCHILD,
            new StringExtractor<EChild>() {
                @Override
                public String getStringValue(EChild item, int position) {
                    return item.nick;
                }
            });
        dict.addStringField(R.id.descriptionCHILD,
            new StringExtractor<EChild>() {
                @Override
                public String getStringValue(EChild item, int position) {
                    return item.gender + " - " + item.date_birth;
                }
            });

        // Creamos adaptador
        FunDapter<EChild> adapter = new FunDapter<EChild>(getActivity(),
                MonitorActivity.ride.data.group.childs, R.layout.child_item, dict);

        // Asignamos el adaptador a la vista
        SwipeListView listChildren = (SwipeListView)root.findViewById(R.id.listChildren);

        //listChildren.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listChildren.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }
        });

        listChildren.setAdapter(adapter);

        // Devolvemos la vista del Fragment
        return root;
    }
}