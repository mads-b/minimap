package com.eit.minimap.datastructures;

public class Message {
    private String message;
    
    private String senderMcAddr;
    
    private long timeMessageReceived;
    
    public Message(String message, String senderMcAddr, long timeMessageReceived){
        this.message = message;
        this.senderMcAddr = senderMcAddr;
        this. timeMessageReceived = timeMessageReceived;
    }
}