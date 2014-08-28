package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.trazeo.app.EditGroupActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.SearchGroupsActivity;
import com.sopinet.trazeo.app.gson.Group;

import java.util.ArrayList;

/**
 * Created by david on 19/05/14.
 */
public class CityGroupAdapter extends ArrayAdapter<Group> {
    private ArrayList<Group> groupsList;
    private Context context;

    public CityGroupAdapter(Context context, int textViewResourceId,
                        ArrayList<Group> countryList) {
        super(context, textViewResourceId, countryList);
        this.groupsList = new ArrayList<Group>();
        this.groupsList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        TextView name;
        TextView route;
        TextView creator;
        TextView visibility;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.city_group_list_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.route = (TextView) convertView.findViewById(R.id.route);
            holder.creator = (TextView) convertView.findViewById(R.id.creator);
            holder.visibility = (TextView) convertView.findViewById(R.id.visibility);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = groupsList.get(position);
        holder.name.setText(group.name);
        holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

        try {
            holder.route.setText(group.route.name);
            holder.route.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            holder.creator.setText(group.route.admin_name);
            holder.creator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        } catch (Exception e) {
            holder.route.setText("");
            holder.creator.setText("");
        }

        if(group.visibility != null) {
            holder.visibility.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            if (group.visibility.equals("0")) {
                holder.visibility.setBackgroundResource(R.drawable.edit_group_selector);
                holder.visibility.setText("Vincular");
            } else if(group.visibility.equals("1")) {
                holder.visibility.setBackgroundResource(R.drawable.request_group_selector);
                holder.visibility.setText("Solicitar");
            } else if(group.visibility.equals("2")) {
                //convertView.setVisibility(View.GONE);
            }
        }

        holder.visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NetHelper.isOnline(context)) {
                    if (group.visibility.equals("0"))
                        ((SearchGroupsActivity) context).joinGroup(group.id);
                    else if (group.visibility.equals("1")) {
                        ((SearchGroupsActivity) context).requestGroup(group.id);
                    }
                } else {
                    Toast.makeText(context, "No hay conexión", Toast.LENGTH_LONG).show();
                }
            }
        });

        return convertView;

    }
}
