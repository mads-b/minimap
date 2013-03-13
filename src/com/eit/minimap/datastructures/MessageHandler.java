package com.eit.minimap.datastructures;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.eit.minimap.HardwareManager;
import com.eit.minimap.datastructures.UserStore.UserStoreListener;
import com.eit.minimap.network.NetworkListener;

import android.util.Log;

public class MessageHandler implements NetworkListener {
    
    private HardwareManager manager;
    
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
    
    //Void atm, should probably return a type Message or something
    public void onPackageReceived(JSONObject pack) {
        try{
            String type = pack.getString("type");
            if(type == "msg"){
                Message newMsg = new Message(pack.getString("msg"),pack.getString("senderMacAddr"), System.currentTimeMillis());
                addMessage(newMsg);
                if(listener!=null) {
                    listener.messageReceived(this);
                }
            }
        }catch(JSONException error){
            Log.e(TAG,"Error! Certain fields missing in received pack (missing MacAddr or type?)\n"+pack.toString());
        }
    }
    
    public void registerListener(MessageHandlerListener listener) {
        this.listener=listener;
    }

    
    public interface MessageHandlerListener{
        
        void messageReceived(MessageHandler msgHandler);
        
    }
}