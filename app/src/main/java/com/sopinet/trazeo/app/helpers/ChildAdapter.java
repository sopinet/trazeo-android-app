package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sopinet.trazeo.app.MonitorActivity;
import com.sopinet.trazeo.app.MonitorChildFragment;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.EChild;

import java.util.ArrayList;

public class ChildAdapter extends ArrayAdapter<EChild> {

    private ArrayList<EChild> echildList;
    private Context context;
    private MonitorChildFragment childFragment;

    public ChildAdapter(Context context, int textViewResourceId,
                           ArrayList<EChild> countryList, MonitorChildFragment childFragment) {
        super(context, textViewResourceId, countryList);
        this.echildList = new ArrayList<EChild>();
        this.echildList.addAll(countryList);
        this.context = context;
        this.childFragment = childFragment;
    }

    private class ViewHolder {
        TextView title;
        TextView description;
        CheckBox check;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.child_item, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.titleCHILD);
            //holder.description = (TextView) convertView.findViewById(R.id.descriptionCHILD);
            holder.check = (CheckBox) convertView.findViewById(R.id.checkCHILD);
            convertView.setTag(holder);

            /**
            holder.check.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v ;
                    EChild echild = (EChild) cb.getTag();
                    Toast.makeText(context.getApplicationContext(),
                            "Clicked on Checkbox: " + cb.getText() +
                                    " is " + cb.isChecked(),
                            Toast.LENGTH_LONG
                    ).show();
                    echild.setSelected(cb.isChecked());
                }
            });
            **/
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EChild echild = echildList.get(position);
        holder.title.setText(echild.nick);
        //holder.description.setText(echild.date_birth);
        holder.check.setChecked(echild.isSelected());
        holder.check.setTag(echild);

        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(echild.isSelected())
                    echild.setSelected(false);
                else
                    echild.setSelected(true);
                childFragment.changeChild(echild);
            }
        });

        return convertView;

    }
}