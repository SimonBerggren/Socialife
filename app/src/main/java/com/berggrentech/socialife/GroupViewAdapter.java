package com.berggrentech.socialife;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Simon on 2016-10-21.
 */
class GroupViewAdapter extends GroupViewAnimated.AnimatedExpandableListAdapter {

    private ArrayList<Group> mGroups;
    private LayoutInflater mInflater;
    private MapActivity mActivity;

    GroupViewAdapter(MapActivity _Activity, ArrayList<Group> _Groups) {
        super();
        mActivity = _Activity;
        mGroups = _Groups;
        mInflater = (LayoutInflater) _Activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.list_group, parent, false);

        Group group = mGroups.get(groupPosition);
        TextView title = (TextView) view.findViewById(R.id.group_title);
        title.setText(group.getName());
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GroupViewAnimated entries = (GroupViewAnimated) mActivity.findViewById(R.id.groups);

                if(entries.isGroupExpanded(groupPosition))
                    entries.collapseGroupWithAnimation(groupPosition);
                else
                    entries.expandGroupWithAnimation(groupPosition);
            }
        });

        return view;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public View getRealChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View view = mInflater.inflate(R.layout.list_group, parent, false);

        Group group = mGroups.get(groupPosition);
        ArrayList<Member> members = group.getMembers();
        TextView title = (TextView) view.findViewById(R.id.group_title);
        title.setText(members.get(childPosition).getName());

        return view;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).getMembers().size();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).getMembers().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}