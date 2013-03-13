package com.eit.minimap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.eit.minimap.datastructures.MessageHandler;
import com.eit.minimap.datastructures.User;
import com.eit.minimap.datastructures.UserStore;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Javadoc!
 */
public class ChatDialog extends DialogFragment {
    private final View dialog;

    public ChatDialog(Context context, UserStore users, MessageHandler messageHandler) {
        dialog = LayoutInflater.from(context).inflate(R.layout.chat_dialog,null);
        Spinner userSelector = (Spinner) dialog.findViewById(R.id.chat_user_selector);
        // Fill top spinner with users.
        List<String> userMacs = new ArrayList<String>();
        List<String> userNames = new ArrayList<String>();
        userNames.add("Everyone");
        for(User user : users.getUsers()) {
            userNames.add(user.getScreenName());
            userMacs.add(user.getMacAddr());
        }
        userSelector.setAdapter(new ArrayAdapter<String>(
                context,
                R.layout.spinner_user_name,
                (String[]) userNames.toArray()));




        new AlertDialog.Builder(context).setView(dialog).create().show();
    }
}
