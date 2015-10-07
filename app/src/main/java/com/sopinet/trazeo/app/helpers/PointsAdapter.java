package com.sopinet.trazeo.app.helpers;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;


public class PointsAdapter extends BaseExpandableListAdapter{

    private final SparseArray<ItemsGroup> groups;
    public Activity activity;

    public PointsAdapter (Activity activity) {
        this.activity = activity;
        this.groups = new SparseArray<>();

        groups.append(0, new ItemsGroup(activity.getResources().getString(R.string.get_points),
                activity.getResources().getString(R.string.get_points_description)));
        groups.append(1, new ItemsGroup(activity.getResources().getString(R.string.where_exchange),
                activity.getResources().getString(R.string.where_exchange_description)));
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).description;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolder v;

        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.points_subitems, parent, false);
            v = new ViewHolder();
            v.child = (TextView) convertView.findViewById(R.id.tv_child);
            convertView.setTag(v);
        } else {
            v = (ViewHolder) convertView.getTag();
        }
        v.child.setText("hola");
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.points_item, parent, false);
        }
        ItemsGroup group = (ItemsGroup) getGroup(groupPosition);
        ((CheckedTextView) convertView).setText(group.title);
        ((CheckedTextView) convertView).setChecked(isExpanded);
        return convertView;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private static class ViewHolder {
        public TextView child;
    }

    private class ItemsGroup {
        public String title;
        public String description;

        public ItemsGroup(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
