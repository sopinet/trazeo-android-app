package com.sopinet.trazeo.app.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.chat.model.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatAdapter extends ArrayAdapter<Message>{

    Context context;
    public ArrayList<Message> messages;

    public ChatAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = new ArrayList<>();
        this.messages.addAll(messages);
    }

    private class ViewHolder {
        TextView name;
        TextView time;
        TextView text;

        // Check del mensaje
        ImageView messageState;

        //Date
        LinearLayout dateLy;
        TextView date;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        Message message = messages.get(position);

        int type = getItemViewType(position);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.fragment_chat, parent, false);
            switch(type) {
                case Message.RECEIVED:
                    convertView = inflater.inflate(R.layout.box_chat_received, parent, false);
                    break;
                case Message.SENT:
                    convertView = inflater.inflate(R.layout.box_chat_sent, parent, false);
                    break;
                case Message.NOTIFICATION:
                    convertView = inflater.inflate(R.layout.box_chat_notification, parent, false);
                    break;
            }
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.messageState = (ImageView) convertView.findViewById(R.id.messageState);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.dateLy = (LinearLayout) convertView.findViewById(R.id.dateLy);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(type == Message.RECEIVED) {
            holder.name.setText(message.username);
            holder.time.setText(ChatUtils.formatTime(message.time));
        } else {
            holder.time.setText(ChatUtils.formatTime(message.time));
        }

        holder.text.setText(message.text);

        //Date Sectioner
        if(position == 0) {
            holder.date.setText(ChatUtils.formatDateNormalWithoutTime(message.time).toUpperCase());
            holder.dateLy.setVisibility(View.VISIBLE);
        } else {
            //Anterior
            Calendar calendarBefore = Calendar.getInstance();
            Date beforeMessageDate = null;
            try {
                beforeMessageDate = new Date(Long.parseLong(messages.get(position - 1).time));
                calendarBefore.setTime(beforeMessageDate);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            //Actual
            Calendar calendarActual = Calendar.getInstance();
            Date messageDate = null;
            try {
                messageDate = new Date(Long.parseLong(message.time));
                calendarActual.setTime(messageDate);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if(beforeMessageDate != null && messageDate != null &&
                    calendarBefore.get(Calendar.DAY_OF_MONTH) != calendarActual.get(Calendar.DAY_OF_MONTH)){

                holder.date.setText(ChatUtils.formatDateNormalWithoutTime(message.time).toUpperCase());
                holder.dateLy.setVisibility(View.VISIBLE);
            } else {
                holder.dateLy.setVisibility(View.GONE);
            }
        }

        // Check
        if(type == Message.SENT) {

            holder.messageState = (ImageView) convertView.findViewById(R.id.messageState);
            switch(message.messageState) {
                case Message.NOT_SENT:
                    holder.messageState.setImageResource(R.drawable.check_icon_not_sended);
                    break;
                case Message.SERVER_RECEIVED:
                    holder.messageState.setImageResource(R.drawable.check_icon_sended);
                    break;
                case Message.USER_RECEIVED:
                    //holder.messageState.setImageResource(R.drawable.check_icon_sended);
                    break;
                case Message.ERROR_RECEIVED:
                    holder.messageState.setImageResource(R.drawable.check_icon_error);
                    break;
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    public void addItem(final Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void updateStateOfMessage(Message message) {

        for(Message msg : this.messages) {
            if (msg.getId().equals(message.getId())) {
                msg.messageState = message.messageState;
            }
        }
        notifyDataSetChanged();
    }
}
