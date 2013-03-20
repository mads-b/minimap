package com.eit.minimap.network;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple thread module that
 */
class SocketWriterThread extends Thread {
    private boolean running;
    private BufferedWriter outputWriter;
    private final Socket socket;
    private final BlockingQueue<JSONObject> packets = new LinkedBlockingQueue<JSONObject>();
    private static final String TAG = "com.eit.minimap.network.SocketWriterThread";

    public SocketWriterThread(Socket socket) {
        this.socket=socket;
    }

    @Override
    public void run() {
        try {
            outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            Log.e(TAG,"Failed setting up output stream! No connection?",e);
        }
        while(running) {
            String pack = "";
            try {
                pack = packets.take().toString();
                // Write a line, return, and flush.
                outputWriter.write(pack,0,pack.length());
                outputWriter.newLine();
                outputWriter.flush();
            } catch (IOException e) {
                Log.e(TAG,"Error occured while writing package with data: "+pack,e);
            } catch (InterruptedException ignored) {}
        }
    }

    public void send(JSONObject packet) {
        packets.add(packet);
    }

    @Override
    public void start() {
        running=true;
        super.start();
    }

    public void finishAndStop() {
        int waits =0;
        while(packets.size()!=0 && waits<10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            waits++;
        }
        running=false;
        super.interrupt();
    }
}
