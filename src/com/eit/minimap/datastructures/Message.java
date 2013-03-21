package com.eit.minimap.datastructures;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private final String message;

    private final String senderMcAddr;

    private final String recipientMcAddr;

    private final long timeMessageReceived;

    public Message(
            String message,
            String senderMcAddr,
            String recipientMcAddr,
            long timeMessageReceived){
        this.message = message;
        this.senderMcAddr = senderMcAddr;
        this.recipientMcAddr = recipientMcAddr;
        this.timeMessageReceived = timeMessageReceived;
    }

    public Message(JSONObject json) throws JSONException {
        this(json.getString("message"),
                json.getString("macAddr"),
                json.isNull("destAddr") ? null : json.getString("destAddr"),
                System.currentTimeMillis());
    }


    public String getMessage(){
        return message;
    }

    public String getSenderMacAddr() {
        return senderMcAddr;
    }

    public String getRecipientMacAddr() {
        return recipientMcAddr;
    }

    public long getTimestamp() {
        return timeMessageReceived;
    }

    public JSONObject toJson() {
        try {
            return new JSONObject()
                    .put("type", "msg")
                    .put("message",message)
                    .put("macAddr",senderMcAddr)
                    .put("destAddr",recipientMcAddr);
        } catch (JSONException ignored) {}
        return null;
    }
}
