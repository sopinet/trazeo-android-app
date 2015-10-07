package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sopinet.trazeo.app.ChildrenActivity;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.EChild;

import java.util.ArrayList;

public class ListedChildAdapter extends ArrayAdapter<EChild> {
    private ArrayList<EChild> childrenList;
    private ArrayList<String> spinnerList;
    private Context context;
    private int layoutResourceId;

    public ListedChildAdapter(Context context, int layoutResourceId,
                           ArrayList<EChild> countryList) {
        super(context, layoutResourceId, countryList);
        this.childrenList = new ArrayList<>();
        this.childrenList.addAll(countryList);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        spinnerList = new ArrayList<>();
        spinnerList.add("");
        spinnerList.add(context.getString(R.string.edit));
        spinnerList.add(context.getString(R.string.remove));
    }

    private class ViewHolder {
        TextView name;
        LinearLayout spinChildBtn;
        Spinner childOptions;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.spinChildBtn = (LinearLayout) convertView.findViewById(R.id.spinChildBtn);
            holder.childOptions = (Spinner) convertView.findViewById(R.id.childOptions);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EChild child = childrenList.get(position);
        holder.name.setText(child.nick);
        holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

        SpinnerAdapter dataAdapter = new SpinnerAdapter(context,
                android.R.layout.simple_spinner_item, spinnerList, 0);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.childOptions.setAdapter(dataAdapter);


        final ViewHolder finalHolder = holder;
        holder.spinChildBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalHolder.childOptions.performClick();
            }
        });

        holder.childOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 1) {
                    ((ChildrenActivity)context).showNewChildFragment(child.id, child.nick, child.scholl, child.date_birth, child.gender);
                }else if(position == 2) {
                    ((ChildrenActivity)context).removeChild(child.id);
                }
                finalHolder.childOptions.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return convertView;
    }
}
