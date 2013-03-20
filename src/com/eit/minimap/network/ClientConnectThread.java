package com.eit.minimap.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import com.eit.minimap.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * AsyncTask showing a progressdialog as it tries to connect to the host,
 * showing an error dialog on failure, with retry button and a return button transferring the user to the
 * MainActivity. On completion, the NetworkListener is given a readily configured JsonTcpClient.
 * This should be the only way a connection is established. If the connection is broken,
 * one should instantiate and run this thread again to reconnect.
 */
public class ClientConnectThread extends AsyncTask<Void,Void,String> {
    private final TcpClientRecipient recipient;
    private final Resources res;
    private JsonTcpClient client;
    private final Context c;
    private final int delayMs;

    private final static int CONNECTION_CHECK_TIMEOUT_MS = 10000;
    private final static String TAG = "com.eit.minimap.network.ClientConnectThread";

    public ClientConnectThread(Context c, TcpClientRecipient recipient,int delayMs) {
        this.recipient = recipient;
        this.res=c.getResources();
        this.c = c;
        this.delayMs = delayMs;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(Void... params) {
            /* Here we try to see if we can open a TCP socket to our host. */
        try {
            Thread.sleep(delayMs);
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);

            String serverUri = preferences.getString("serverAdr", "spoon.orakel.ntnu.no");
            int serverPort = preferences.getInt("serverPort", 1337);
            InetAddress serverAddr = InetAddress.getByName(serverUri);
            if(!serverAddr.isReachable(CONNECTION_CHECK_TIMEOUT_MS)) {
                return res.getString(R.string.failed_not_reachable);
            }
            client = new JsonTcpClient(serverAddr,serverPort);
            client.start();
        } catch (UnknownHostException e) {
            return res.getString(R.string.failed_no_host);
        } catch (IOException e) {
            return res.getString(R.string.failed_io_err);
        } catch (InterruptedException ignored) {}
        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        if(error!=null) {
            Log.e(TAG,"Connection failed with error: "+error);
            recipient.receiveTcpClient(null,error);
            if(client!=null) client.stop(false);
            //TODO: Error dialog containing above string. Force user to exit activity.
        } else { //No error to show.
            recipient.receiveTcpClient(client,null);
        }
    }

    public interface TcpClientRecipient {
        /**
         * Called by ClientConnectThread when a Tcp Client is set up correctly.
         * @param client A JsonTcpClient, or null on failure.
         * @param err Error string detailing what went wrong.
         */
        void receiveTcpClient(JsonTcpClient client,String err);
    }
}
