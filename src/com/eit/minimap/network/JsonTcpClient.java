package com.eit.minimap.network;

import android.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;

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


    private static final String TAG = "com.eit.minimap.network.AbstractCommunicator";

    /**
     * Constructor for the networker.
     * @param portNum Port to initialize the networker on. 0 is "don't care".
     */
    JsonTcpClient(InetAddress address,int portNum) {
        this.address=address;
        this.port=portNum;
    }

    /**
     * Starts the communicator.
     * @throws java.io.IOException If host cannot be reached
     */
    public void start() throws IOException {
        try {
            socket = new Socket(address,port);
            //Make reader and writer
            reader = new SocketReaderThread(socket,this);
            writer = new SocketWriterThread(socket);

        } catch (SocketException e) {
            Log.e(TAG,"Failed while making a new socket",e);
        }
        reader.start();
        writer.start();
        Log.d(TAG,"Networking module running on port "+port);
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
        Log.d(TAG,"Networking module stopped. Socket closed.");
    }

    /**
     * Adds a JSON object to the send queue and sends as soon as possible.
     * @param json Json object to send
     */
    void sendData(JSONObject json) {
        Log.d(TAG,"Sending: "+json.toString());
        writer.send(json);
    }

    /**
     * Callback method from the SocketReaderThread.
     * @param json JSON object received.
     */
    void receiveData(JSONObject json) {
        Log.d(TAG,"Got packet! "+json.toString());
    }

    public int getPort() {
        return socket.getPort();
    }
}
