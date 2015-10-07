package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sopinet.trazeo.app.ChatActivity_;
import com.sopinet.trazeo.app.EditGroupActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.SelectGroupActivity;
import com.sopinet.trazeo.app.SelectGroupFragment;
import com.sopinet.trazeo.app.chat.model.Group;

import java.util.ArrayList;

public class GroupAdapter extends ArrayAdapter<Group> {
    private ArrayList<Group> groupsList;
    private Context context;
    private Fragment fragment;

    public GroupAdapter(Context context, Fragment fragment, int textViewResourceId,
                          ArrayList<Group> groupsList) {
        super(context, textViewResourceId, groupsList);
        this.fragment = fragment;
        this.groupsList = new ArrayList<>();
        this.groupsList.addAll(groupsList);
        this.context = context;
    }

    private class ViewHolder {
        TextView name;
        LinearLayout spinBtn;
        Spinner groupOptions;
        TextView initRideButton;
        LinearLayout initRideParent;
        LinearLayout chatButton;
        ImageView initRideImage;
        ImageView chatImage;
    }

    @Override
    public int getCount() {
        return groupsList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_list_item, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.spinBtn = (LinearLayout) convertView.findViewById(R.id.spinBtn);
            holder.groupOptions = (Spinner) convertView.findViewById(R.id.groupOptions);
            holder.initRideButton = (TextView) convertView.findViewById(R.id.initRideButton);
            holder.chatButton = (LinearLayout) convertView.findViewById(R.id.llChat);
            holder.initRideParent = (LinearLayout) convertView.findViewById(R.id.llInitRide);
            holder.initRideImage = (ImageView) convertView.findViewById(R.id.initRideImage);
            holder.chatImage = (ImageView) convertView.findViewById(R.id.chatImage);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = groupsList.get(position);
        final Group groupDb = Group.getGroupById(group.id);
        holder.name.setText(group.name);

        if (groupDb != null ) {
            if (groupDb.hasride.equals("true")) {
                holder.initRideButton.setText(context.getString(R.string.ride_started));
                holder.initRideImage.setImageDrawable(context.getResources().getDrawable(R.drawable.init_ride_icon_red));
            } else {
                holder.initRideButton.setText(context.getString(R.string.init_ride));
                holder.initRideImage.setImageDrawable(context.getResources().getDrawable(R.drawable.init_ride_icon));
            }

            if (groupDb.hasMessage == 1) {
                holder.chatImage.setImageDrawable(context.getResources().getDrawable(R.drawable.chat_icon_2));
            } else {
                holder.chatImage.setImageDrawable(context.getResources().getDrawable(R.drawable.chat_icon));
            }
        }

        if(group.admin != null) {
            ArrayList<String> list = new ArrayList<>();
            list.add("");
            list.add(context.getString(R.string.invite));
            if (group.admin.equals("false"))
                list.add(context.getString(R.string.desvicule_group));
            if (group.admin.equals("true")) {
                list.add(context.getString(R.string.remove_group));
                list.add(context.getString(R.string.config));
            }
            list.add(context.getString(R.string.join_child));
            list.add(context.getString(R.string.disjoin_child));

            SpinnerAdapter dataAdapter = new SpinnerAdapter(context,
                    android.R.layout.simple_spinner_item, list, 0);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.groupOptions.setAdapter(dataAdapter);
        }

        final ViewHolder finalHolder = holder;
        holder.spinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalHolder.groupOptions.performClick();
            }
        });


        holder.groupOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 1) {
                    ((SelectGroupFragment)fragment).showInviteDialog(group.id);
                } else if(adapterView.getSelectedItem().equals(context.getString(R.string.config))) {
                    Intent in = new Intent(context, EditGroupActivity_.class);
                    in.putExtra("group", group);
                    fragment.startActivityForResult(in, Constants.EDITGROUPACTIVITY);
                }else if(adapterView.getSelectedItem().equals(context.getString(R.string.desvicule_group))) {
                    ((SelectGroupFragment) fragment).buildDisjoinDialog(group.id);
                } else if(adapterView.getSelectedItem().equals(context.getString(R.string.remove_group))) {
                    ((SelectGroupFragment)fragment).removeGroup(group.id);
                } else if(adapterView.getSelectedItem().equals(context.getString(R.string.join_child))) {
                    ((SelectGroupFragment)fragment).buildJoinDisJoinChildDialog(group.id, true);
                } else if(adapterView.getSelectedItem().equals(context.getString(R.string.disjoin_child))) {
                    ((SelectGroupFragment)fragment).buildJoinDisJoinChildDialog(group.id, false);
                }
                finalHolder.groupOptions.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.initRideParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SelectGroupFragment) fragment).hasLocationProvider(groupDb);
            }
        });

        holder.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ChatActivity_.class);
                i.putExtra("username", ((SelectGroupActivity) context).myProfile.data.name);
                i.putExtra("groupId", group.id);
                context.startActivity(i);
            }
        });

        return convertView;

    }


}
