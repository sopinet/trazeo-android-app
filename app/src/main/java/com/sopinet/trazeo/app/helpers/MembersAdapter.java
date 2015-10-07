package com.sopinet.trazeo.app.helpers;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.Member;

import java.util.ArrayList;
import java.util.Collections;

public class MembersAdapter extends ArrayAdapter<Member> {

    private ArrayList<Member> members;
    private Context context;

    public MembersAdapter(Context context, int resource, ArrayList<Member> members) {
        super(context, resource, members);
        this.context = context;
        this.members = new ArrayList<>();
        this.members.addAll(members);
        Collections.sort(this.members, Member.nameComparator);
    }

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.member_item, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tvMemberName);
            holder.phone = (LinearLayout) convertView.findViewById(R.id.llPhone);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(getItem(position).name);
        holder.phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + getItem(position).mobile));
                context.startActivity(intent);
            }
        });


        return convertView;
    }

    private class ViewHolder {
        TextView name;
        LinearLayout phone;
    }
}
