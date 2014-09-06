package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sopinet.trazeo.app.EditGroupActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.SelectGroupActivity;
import com.sopinet.trazeo.app.gson.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 19/05/14.
 */
public class GroupAdapter extends ArrayAdapter<Group> {
    private ArrayList<Group> groupsList;
    private Context context;
    int spinSelectCount = 0;

    public GroupAdapter(Context context, int textViewResourceId,
                          ArrayList<Group> countryList) {
        super(context, textViewResourceId, countryList);
        this.groupsList = new ArrayList<Group>();
        this.groupsList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        LinearLayout groupItem;
        TextView name;
        TextView description;
        ImageView spinBtn;
        Spinner groupOptions;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_list_item, null);

            holder = new ViewHolder();
            holder.groupItem = (LinearLayout) convertView.findViewById(R.id.groupItem);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.spinBtn = (ImageView) convertView.findViewById(R.id.spinBtn);
            holder.groupOptions = (Spinner) convertView.findViewById(R.id.groupOptions);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = groupsList.get(position);
        holder.name.setText(group.name);
        holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

        String description = "";
        if (group.hasride.equals("true")) {
            description = "...Paseo en curso...";
        } else {
            description =  "Iniciar";
        }

        holder.description.setText(description);
        holder.description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        if(group.admin != null) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("");
            list.add("Invitar");
            if (group.admin.equals("true"))
                list.add("Configuración");

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
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {
                    ((SelectGroupActivity) context).showInviteDialog(group.id);
                    finalHolder.groupOptions.setSelection(0);
                }else if(i == 2) {
                    Intent in = new Intent(context, EditGroupActivity_.class);
                    in.putExtra("id_group", group.id);
                    context.startActivity(in);
                    finalHolder.groupOptions.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.groupItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SelectGroupActivity) context).createRide(group.id, group.hasride);
            }
        });

        return convertView;

    }
}
