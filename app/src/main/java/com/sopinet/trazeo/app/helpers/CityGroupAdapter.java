package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sopinet.trazeo.app.GroupsFoundFragment;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.chat.model.Group;

import java.util.ArrayList;


public class CityGroupAdapter extends ArrayAdapter<Group> {
    private ArrayList<Group> groupsList;
    private Context context;
    private Fragment fragment;
    private int lastPosition = -1;


    public CityGroupAdapter(Context context, Fragment fragment, int textViewResourceId,
                        ArrayList<Group> countryList) {
        super(context, textViewResourceId, countryList);
        this.groupsList = new ArrayList<>();
        this.groupsList.addAll(countryList);
        this.context = context;
        this.fragment = fragment;
    }

    private class ViewHolder {
        TextView name;
        TextView route;
        TextView creator;
        TextView visibility;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.city_group_list_item, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.route = (TextView) view.findViewById(R.id.route);
            holder.creator = (TextView) view.findViewById(R.id.creator);
            holder.visibility = (TextView) view.findViewById(R.id.visibility);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        final Group group = groupsList.get(position);
        holder.name.setText(group.name);

        try {
            holder.route.setText(group.route.name);
            holder.creator.setText(group.route.admin_name);
        } catch (Exception e) {
            holder.route.setText("");
            holder.creator.setText("");
        }

        if(group.visibility != null) {
            switch (group.visibility) {
                case "0":
                    holder.visibility.setBackgroundResource(R.drawable.edit_group_selector);
                    holder.visibility.setText(context.getString(R.string.link_group));
                    break;
                case "1":
                    holder.visibility.setBackgroundResource(R.drawable.request_group_selector);
                    holder.visibility.setText(context.getString(R.string.send_group_petition));
                    break;
            }
        }

        holder.visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (group.visibility) {
                    case "0":
                        ((GroupsFoundFragment) fragment).joinGroup(group.id);
                        break;
                    case "1":
                        ((GroupsFoundFragment) fragment).requestGroup(group.id);
                        break;
                }
            }
        });

        if(position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.up_from_bottom);
            animation.setDuration(500);
            view.startAnimation(animation);
        }
        lastPosition = position;

        return view;

    }
}
