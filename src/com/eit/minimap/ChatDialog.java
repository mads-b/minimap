package com.eit.minimap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import com.eit.minimap.datastructures.MessageHandler;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Javadoc!
 */
public class ChatDialog extends DialogFragment implements AdapterView.OnItemClickListener{
    private final View dialog;
    private final Map<String,String> nameToMac = new HashMap<String,String>();

    public ChatDialog(Context context, UserStore users, MessageHandler messageHandler) {
        dialog = LayoutInflater.from(context).inflate(R.layout.chat_dialog,null);
        Spinner userSelector = (Spinner) dialog.findViewById(R.id.chat_user_selector);
        // Fill top spinner with users.
        nameToMac.put("Everyone", "everyone");
        for(User user : users.getUsers()) {
            nameToMac.put(user.getScreenName(),user.getMacAddr());
        }
        userSelector.setAdapter(new ArrayAdapter<String>(
                context,
                R.layout.spinner_user_name,
                (String[]) nameToMac.keySet().toArray()));
        //Listen to click changes
        userSelector.setOnItemClickListener(this);
        // Make bogus click at "Everyone".
        userSelector.getChildAt(0).performClick();



        new AlertDialog.Builder(context).setView(dialog).create().show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = (String) parent.getAdapter().getItem(position);
        // Populate message list
        ListView messageListView = (ListView) dialog.findViewById(R.id.messages_list);

    }


    private class MessageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return nameToMac.size();
        }

        @Override
        public Object getItem(int position) {
            return null;  //TODO: Not implemented!
        }

        @Override
        public long getItemId(int position) {
            return 0;  //TODO: Not implemented!
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;  //TODO: Not implemented!
        }
    }
}
