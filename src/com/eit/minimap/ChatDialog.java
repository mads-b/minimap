package com.eit.minimap;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.eit.minimap.datastructures.Message;
import com.eit.minimap.datastructures.MessageHandler;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Instantiating this class brings up the chat panel.
 */
public class ChatDialog implements
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        MessageHandler.MessageHandlerListener {
    private final View dialog;
    private final MessageHandler messageHandler;
    private final UserStore userStore;

    /*
     * Here, we're remembering the views we're using..
     */
    private final Spinner userSelector;
    private final ListView messagesList;
    private final EditText messageField;


    public ChatDialog(Context context, UserStore users, MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.userStore = users;

        dialog = LayoutInflater.from(context).inflate(R.layout.chat_dialog,null);
        userSelector = (Spinner) dialog.findViewById(R.id.chat_user_selector);
        messagesList = (ListView) dialog.findViewById(R.id.messages_list);
        messageField = (EditText) dialog.findViewById(R.id.your_message);
        Button sendButton = (Button) dialog.findViewById(R.id.message_send_button);

        userSelector.setAdapter(new UserAdapter());
        //Listen to click changes to user selection.
        userSelector.setOnItemSelectedListener(this);

        // Listen to clicks on "send" button.
        sendButton.setOnClickListener(this);

        new AlertDialog.Builder(context).setView(dialog).create().show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Get messages from selected user
        User selected = (User) parent.getAdapter().getItem(position);
        Log.d("com.eit.minimap.ChatDialog","Selected item number "+position+" user selected is: "+selected.getScreenName());
        List<Message> messages;
        if(selected.getScreenName().equals("Everyone"))
            messages = messageHandler.getMessagesFrom(null);
        else
            messages = messageHandler.getMessagesFrom(selected);

        // Populate message list
        messagesList.setAdapter(new MessageAdapter(messages));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Make bogus click at "Everyone".
        parent.performItemClick(parent.getChildAt(0),0,0);
    }

    @Override
    public void onClick(View v) {
        // Send button clicked. Extract message and recipient
        String msg = messageField.getText().toString();
        User selectedUser = (User) userSelector.getSelectedItem();

        // Make new message, and send it!
        messageHandler.sendMessage(new Message(
                msg,
                userStore.getMyUser().getMacAddr(),
                selectedUser.getMacAddr().equals("everyone") ? null : selectedUser.getMacAddr(),
                System.currentTimeMillis()));
        // Clear field so user doesn't spam.
        messageField.setText("");
    }

    @Override
    public void messageReceived(MessageHandler msgHandler) {
        // Message received. Refresh message list (by artificially clicking at the element we're viewing.)
        onItemSelected(messagesList,
                userSelector.getSelectedView(),
                userSelector.getSelectedItemPosition(),
                userSelector.getSelectedItemId());
    }


    private class MessageAdapter extends BaseAdapter {
        private final List<Message> messages;
        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View message = LayoutInflater.from(dialog.getContext()).inflate(R.layout.chat_message,null);
            Message msg = (Message) getItem(position);

            // Get fields.
            TextView timestamp = (TextView) message.findViewById(R.id.chat_message_timestamp);
            TextView name = (TextView) message.findViewById(R.id.chat_message_name);
            TextView content = (TextView) message.findViewById(R.id.chat_message_content);

            // Set timestamp:
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:ss - ");
            String time = simpleDateFormat.format(new Date(msg.getTimestamp()));
            timestamp.setText(time);

            //Set user name and content
            name.setText(userStore.getUserWithMac(msg.getSenderMacAddr()).getScreenName());
            content.setText(msg.getMessage());

            return message;
        }
    }

    /**
     * Adapter containing a list of all the users,
     * each wrapped in a view defined by spinner_user_name.xml
     */
    private class UserAdapter extends BaseAdapter {
        private final List<User> users;

        private UserAdapter() {
            users = new ArrayList<User>();
            users.add(new User("everyone","Everyone"));
            users.addAll(userStore.getUsers());
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(dialog.getContext()).inflate(R.layout.spinner_user_name,null);
            TextView text = (TextView) view.findViewById(R.id.screen_name);
            text.setText(users.get(position).getScreenName());
            return view;
        }
    }
}
