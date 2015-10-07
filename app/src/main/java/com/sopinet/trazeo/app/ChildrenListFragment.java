package com.sopinet.trazeo.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.androidannotations.annotations.EFragment;

@EFragment
public class ChildrenListFragment extends Fragment{

    ProgressWheel progressView;

    ListView childrenList;

    ChildrenActivity context;

    public ChildrenListFragment() {}

    public static ChildrenListFragment newInstance() {
        ChildrenListFragment fragment = new ChildrenListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = (ChildrenActivity) getActivity();
        context.toolbar.setTitle(getString(R.string.my_kids));
        context.getUserChildren(this.childrenList, this.progressView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_children_list, container, false);
        this.progressView = (ProgressWheel) root.findViewById(R.id.childrenList_progress);
        this.childrenList = (ListView) root.findViewById(R.id.childrenList);

        return root;
    }

}
