package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.EChild;

import java.util.ArrayList;

public class ChildAdapter extends ArrayAdapter<EChild> {

    private ArrayList<EChild> echildList;
    private Context context;

    public ChildAdapter(Context context, int textViewResourceId,
                           ArrayList<EChild> countryList) {
        super(context, textViewResourceId, countryList);
        this.echildList = new ArrayList<>();
        this.echildList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        TextView title;
        CheckBox check;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.child_item, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.titleCHILD);
            holder.check = (CheckBox) convertView.findViewById(R.id.checkCHILD);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        EChild echild = echildList.get(position);
        holder.title.setText(echild.nick);
        holder.check.setChecked(echild.isSelected());
        holder.check.setTag(echild);

        return convertView;
    }
}