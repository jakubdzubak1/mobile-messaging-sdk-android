package org.infobip.mobile.messaging.demo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import org.infobip.mobile.messaging.Message;
import org.infobip.mobile.messaging.MobileMessaging;
import org.infobip.mobile.messaging.storage.MessageStore;

import java.util.ArrayList;
import java.util.List;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Message> messages; // header titles
    private OnMessageExpandedListener onMessageExpandedListener;

    public interface OnMessageExpandedListener {
        void onMessageExpanded(Message message);
    }

    ExpandableListAdapter(Context context, OnMessageExpandedListener onMessageExpandedListener) {
        this.context = context;
        this.onMessageExpandedListener = onMessageExpandedListener;
        MessageStore messageStore = MobileMessaging.getInstance(context).getMessageStore();
        if (null != messageStore) {
            this.messages = messageStore.bind(context);
        } else {
            this.messages = new ArrayList<>();
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.messages.get(groupPosition).getBody();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Message getGroup(int groupPosition) {
        return this.messages.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.messages.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Message message = getGroup(groupPosition);
        String headerTitle = message.getBody(); //TODO trim to some max char count
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        onMessageExpandedListener.onMessageExpanded(this.messages.get(groupPosition));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}