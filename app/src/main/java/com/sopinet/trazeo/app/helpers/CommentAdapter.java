package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.EComment;

import java.util.ArrayList;

public class CommentAdapter extends ArrayAdapter<EComment> {
    private ArrayList<EComment> ecommentList;
    private Context context;

    public CommentAdapter(Context context, int textViewResourceId,
                        ArrayList<EComment> countryList) {
        super(context, textViewResourceId, countryList);
        this.ecommentList = new ArrayList<EComment>();
        this.ecommentList.addAll(countryList);
        this.context = context;
    }

    private class ViewHolder {
        TextView author_name;
        TextView body;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.comment_item, null);

            holder = new ViewHolder();
            holder.author_name = (TextView) convertView.findViewById(R.id.comment_author);
            holder.body = (TextView) convertView.findViewById(R.id.comment_body);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EComment ecomment = ecommentList.get((ecommentList.size() - 1) - position);
        holder.author_name.setText(ecomment.author_name);
        holder.body.setText(ecomment.body);

        return convertView;

    }
}
