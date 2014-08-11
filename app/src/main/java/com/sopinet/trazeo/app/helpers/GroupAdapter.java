package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sopinet.trazeo.app.EditGroupActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.Group;

import java.util.ArrayList;

/**
 * Created by david on 19/05/14.
 */
public class GroupAdapter extends ArrayAdapter<Group> {
    private ArrayList<Group> groupsList;
    private Context context;

    public GroupAdapter(Context context, int textViewResourceId,
                          ArrayList<Group> countryList) {
        super(context, textViewResourceId, countryList);
        this.groupsList = new ArrayList<Group>();
        this.groupsList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        TextView name;
        TextView description;
        ImageView edit;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_list_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.edit = (ImageView) convertView.findViewById(R.id.edit);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = groupsList.get(position);
        holder.name.setText(group.name);
        holder.name.setTextSize(13 * context.getResources().getDisplayMetrics().density);

        String description = "";
        if (group.hasride.equals("true")) {
            description = "...Paseo en curso...";
        } else {
            description =  "Iniciar";
        }

        holder.description.setText(description);
        holder.description.setTextSize(9 * context.getResources().getDisplayMetrics().density);

        if(group.admin != null) {
            if (group.admin.equals("true")) {
                holder.edit.setVisibility(View.VISIBLE);
            } else {
                holder.edit.setVisibility(View.GONE);
            }
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, EditGroupActivity_.class);
                i.putExtra("id_group", group.id);
                context.startActivity(i);
            }
        });

        return convertView;

    }
}
