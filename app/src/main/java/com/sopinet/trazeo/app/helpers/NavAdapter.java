package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.SelectGroupActivity;

import java.util.ArrayList;
import java.util.Collections;


public class NavAdapter extends BaseAdapter{

    private SelectGroupActivity context;
    private ArrayList<String> myProfile;

    public NavAdapter(Context context, String[] myProfile) {
        this.context = (SelectGroupActivity) context;
        this.myProfile = new ArrayList<>();
        Collections.addAll(this.myProfile, myProfile);
    }

    @Override
    public int getCount() {
        return myProfile.size();
    }

    @Override
    public String getItem(int positions) {
        return myProfile.get(positions);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        final String myProfile = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.profile_drawer_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.myprofile_title);
            viewHolder.imageIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.lvMyProfileItem = (LinearLayout) convertView.findViewById(R.id.lvMyProfileItem);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(myProfile);

        viewHolder.lvMyProfileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.left_drawer.setItemChecked(position, true);
                context.drawer_layout.closeDrawer(context.left_drawer);
            }
        });
        return convertView;
    }

    private static class ViewHolder {

        public TextView title;
        public ImageView imageIcon;
        public LinearLayout lvMyProfileItem;
    }
}
