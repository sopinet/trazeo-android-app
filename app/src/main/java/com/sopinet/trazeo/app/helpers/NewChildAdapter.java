package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.EChild;

import java.util.ArrayList;

public class NewChildAdapter extends ArrayAdapter<EChild> {
    private ArrayList<EChild> childrenList;
    private Context context;

    public NewChildAdapter(Context context, int textViewResourceId,
                            ArrayList<EChild> countryList) {
        super(context, textViewResourceId, countryList);
        this.childrenList = new ArrayList<EChild>();
        this.childrenList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        TextView nick;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.new_child_list_item, null);

            holder = new ViewHolder();
            holder.nick = (TextView) convertView.findViewById(R.id.nick);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EChild child = childrenList.get(position);
        holder.nick.setText(child.nick);
        holder.nick.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

        return convertView;
    }

}
