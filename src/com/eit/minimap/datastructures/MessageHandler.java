package com.eit.minimap.datastructures;

import android.util.Log;
import com.eit.minimap.HardwareManager;
import com.eit.minimap.network.NetworkListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageHandler implements NetworkListener {

    private final HardwareManager manager;

    private MessageHandlerListener listener;

    private final List<Message> messages = new ArrayList<Message>();

    private final static String TAG = "com.eit.minimap.network.MessageHandler";

    //Need this function? Have to implement it
    public MessageHandler(HardwareManager manager){
        this.manager = manager;
        manager.subscribeToNetworkUpdates(this);
    }

    public void addMessage(Message msg){
        messages.add(msg);
    }

    public List<Message> getMessagesRelatedTo(User user) {
        if(user == null) return Collections.unmodifiableList(messages);
        List<Message> tempMessages = new ArrayList<Message>();
        for(Message msg : messages) {
            //Add all messages where user provided is sender or recipient.
            if(        user.getMacAddr().equals(msg.getSenderMacAddr())
                    || user.getMacAddr().equals(msg.getRecipientMacAddr())) {
                tempMessages.add(msg);
            }
        }
        return tempMessages;
    }

    //Void atm, should probably return a type Message or something
    public void onPackageReceived(JSONObject pack) {
        try{
            String type = pack.getString("type");

            // Check if this is a message.
            if(type.equals("msg")){
                if(!pack.isNull("destAddr")) {
                    String dest = pack.getString("destAddr");
                    // Is it meant for us?
                    if(!dest.equals(manager.getMacAddress())) return;
                }
                Message msg = new Message(pack);
                addMessage(msg);
                if(listener!=null) {
                    listener.messageReceived(this, msg);
                }
            }
        }catch(JSONException error){
            Log.e(TAG,"Error! Certain fields missing in received pack (missing MacAddr or type?)\n"+pack.toString(),error);
        }
    }

    public void registerListener(MessageHandlerListener listener) {
        this.listener=listener;
    }


    public interface MessageHandlerListener{
        void messageReceived(MessageHandler msgHandler, Message msg);
    }

    public void sendMessage(Message msg) {
        manager.sendPackage(msg.toJson());
        //Add it to our list also, to show in dialog.
        addMessage(msg);
    }
}
