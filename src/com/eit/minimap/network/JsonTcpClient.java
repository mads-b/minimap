package com.eit.minimap.network;

import android.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for client networking. Sends and receives Json objects through string serializations over TCP.
 * Supports asynchronous reading and writing.
 *
 */
public class JsonTcpClient {
    private final InetAddress address;
    private final int port;
    private Socket socket;
    private SocketReaderThread reader;
    private SocketWriterThread writer;
    private final List<NetworkListener> listeners = new ArrayList<NetworkListener>();


    private static final String TAG = "com.eit.minimap.network.AbstractCommunicator";

    /**
     * Constructor for the networker.
     * Package-private to ensure it's being instantiated by the ClientConnectThread.
     * @param portNum Port to initialize the networker on. 0 is "don't care".
     */
    JsonTcpClient(InetAddress address,int portNum) {
        this.address=address;
        this.port=portNum;
        sendConnectionStateChanged(NetworkListener.Change.DISCONNECTED);
    }

    /**
     * Starts the communicator.
     * @throws java.io.IOException If host cannot be reached
     */
    public void start() throws IOException {
        sendConnectionStateChanged(NetworkListener.Change.CONNECTING);
        socket = new Socket(address,port);
        //Make reader and writer
        reader = new SocketReaderThread(socket,this);
        writer = new SocketWriterThread(socket);

        reader.start();
        writer.start();
        Log.d(TAG,"Networking module running on port "+port);
        sendConnectionStateChanged(NetworkListener.Change.CONNECTED);
    }

    public void stop() {
        if(reader!=null)
            reader.finishAndStop();
        if(writer!=null)
            writer.finishAndStop();
        if(socket!=null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG,"Failed shutting down socket",e);
            }
        }
        sendConnectionStateChanged(NetworkListener.Change.DISCONNECTED);
        Log.d(TAG,"Networking module stopped. Socket closed.");
    }

    /**
     * Adds a JSON object to the send queue and sends as soon as possible.
     * @param json Json object to send
     */
    public void sendData(JSONObject json) {
        Log.d(TAG,"Sending: "+json.toString());
        writer.send(json);
    }

    public void addListener(NetworkListener listener){
        listeners.add(listener);
    }

    /**
     * Callback method from the SocketReaderThread.
     * @param json JSON object received.
     */
    void receiveData(JSONObject json) {
        Log.d(TAG,"Got packet! "+json.toString());
        for(NetworkListener listener : listeners) {
            listener.onPackageReceived(json);
        }
    }

    void sendConnectionStateChanged(NetworkListener.Change c) {
        for(NetworkListener listener : listeners) {
            listener.onConnectionChanged(c);
        }
    }

    public int getPort() {
        return socket.getPort();
    }
}
