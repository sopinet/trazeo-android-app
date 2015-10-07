package com.sopinet.trazeo.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;

@EFragment
public class TutorialFragment extends Fragment {

    /**
     * Key to insert the index page into the mapping of a Bundle.
     */
    private static final String INDEX = "index";

    private int index;

    /**
     * Instances a new fragment with a background color and an index page.
     *
     * @param index
     *            index page
     * @return a new page
     */
    public static TutorialFragment newInstance(int index) {

        // Instantiate a new fragment
        TutorialFragment fragment = new TutorialFragment();

        // Save the parameters
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);

        return fragment;
    }

    public TutorialFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load parameters when the initial creation of the fragment is done
        this.index = (getArguments() != null) ? getArguments().getInt(INDEX) : -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_tutorial, container, false);

        // Show the current page index in the view
        TextView title = (TextView) rootView.findViewById(R.id.tutorial_title);
        TextView desc = (TextView) rootView.findViewById(R.id.tutorial_desc);
        ImageView image = (ImageView) rootView.findViewById(R.id.tutorial_image);

        switch (index) {
            case 0:
                title.setText(getString(R.string.welcome));
                desc.setText(getString(R.string.tutorial_desc_0));
                image.setImageResource(R.drawable.welcome);
                break;
            case 1:
                title.setText(getString(R.string.tutorial_title_1));
                title.setTextSize(23);
                desc.setText(getString(R.string.tutorial_desc_1));
                image.setImageResource(R.drawable.tutorial_1);
                break;
            case 2:
                title.setText(getString(R.string.tutorial_title_2));
                title.setTextSize(23);
                desc.setText(getString(R.string.tutorial_desc_2));
                image.setImageResource(R.drawable.tutorial_2);
                break;
            case 3:
                title.setText(getString(R.string.tutorial_title_3));
                title.setTextSize(23);
                desc.setText(getString(R.string.tutorial_desc_3));
                image.setImageResource(R.drawable.tutorial_3);
                break;
            case 4:
                title.setText(getString(R.string.tutorial_title_4));
                title.setTextSize(23);
                desc.setText(getString(R.string.tutorial_desc_4));
                image.setImageResource(R.drawable.tutorial_4);
                break;
        }

        return rootView;

    }

}
