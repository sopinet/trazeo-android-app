package com.sopinet.trazeo.app.helpers;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.Children;
import com.sopinet.trazeo.app.gson.EChild;

import java.util.ArrayList;

public class ChildSpinnerAdapter extends ArrayAdapter<EChild> {
    private Context context;
    private ArrayList<EChild> children;

    public ChildSpinnerAdapter(Context context,  int textViewResourceId, Children children) {
        super(context, textViewResourceId, 0);
        this.context = context;
        this.children = children.data;
    }

    private class ViewHolder {
        TextView titleCHILD;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View view = convertView;

        final EChild child = getItem(position);

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.child_spinner_row, parent, false);
            holder = new ViewHolder();
            holder.titleCHILD = (TextView) view.findViewById(R.id.titleCHILD);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.titleCHILD.setText(child.nick);

        return view;
    }

    @Override
    public EChild getItem(int position) {
        return children.get(position);
    }

    @Override
    public int getCount() {
        return children.size();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View view = convertView;
        // Get the data item
        final EChild child = getItem(position);

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.child_spinner_row, parent, false);
            holder = new ViewHolder();
            holder.titleCHILD = (TextView) view.findViewById(R.id.titleCHILD);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.titleCHILD.setText(child.nick);

        return view;
    }
}
