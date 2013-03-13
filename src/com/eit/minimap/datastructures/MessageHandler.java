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
    public JSONObject convertToJSON(Message msg, String myMac, String destMac){
        //if you want to broadcast, send destMac as null
        try{
            JSONObject msgPacket = new JSONObject();
            msgPacket.put("type", "msg");
            msgPacket.put("msg", msg.getMessage());
            msgPacket.put("macAddr", myMac);
            msgPacket.put("destAddr", destMac);
            return msgPacket;
        }catch(JSONException error){
            Log.e(TAG,"Error parsing JSON.",error);
            return null;

        }
    }
    public void sendMessage(Message msg, String myMac, String destMac){
        JSONObject packet = convertToJSON(msg,myMac, destMac);
        manager.sendPackage(packet);

    }
}
