package com.eit.minimap.network;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 */
class SocketReaderThread extends Thread {
    private boolean running;
    private BufferedReader inputReader;
    private final Socket socket;
    private final JsonTcpClient receiver;
    private static final String TAG = "com.eit.minimap.network.SocketReaderThread";

    public SocketReaderThread(Socket socket,JsonTcpClient receiver) {
        this.socket=socket;
        this.receiver=receiver;
    }

    @Override
    public void run() {
        try {
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            Log.e(TAG,"Failed setting up reader stream! Connection failure?",e);
        }
        String data="";
        while(running) {
            try {
                data = inputReader.readLine();
                JSONObject json = new JSONObject(data);
                receiver.receiveData(json);
            } catch (IOException e) {
                if(running) {
                    Log.e(TAG,"Error occured while receiving packages",e);
                    // Fatal error. Give up. Socket probably closed.
                    receiver.stop();
                }
            } catch (JSONException e) {
                Log.e(TAG,"Got mangled JSON from host! Data: "+data,e);
            }
        }
    }

    @Override
    public void start() {
        running=true;
        super.start();
    }

    public void finishAndStop() {
        running=false;
    }
}
