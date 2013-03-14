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

    public List<Message> getMessagesFrom(User user) {
        if(user == null) return Collections.unmodifiableList(messages);
        List<Message> tempMessages = new ArrayList<Message>();
        for(Message msg : messages) {
            if(msg.getSenderMacAddr().equals(user.getMacAddr())) {
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
                String dest = pack.getString("destAddr");
                // Is it meant for us?
                if(!dest.equals(manager.getMacAddress())) return;

                addMessage(new Message(pack));
                if(listener!=null) {
                    listener.messageReceived(this);
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

        void messageReceived(MessageHandler msgHandler);
    }

    public void sendMessage(Message msg) {
        manager.sendPackage(msg.toJson());
    }
}
