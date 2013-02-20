package com.eit.minimap.network;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import com.eit.minimap.MainActivity;
import com.eit.minimap.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * AsyncTask showing a progressdialog as it tries to connect to the host,
 * showing an error dialog on failure, with retry button and a return button transferring the user to the
 * MainActivity. On completion, the TcpClientRecipient is given a readily configured JsonTcpClient.
 * This should be the only way a connection is established. If the connection is broken,
 * one should instantiate and run this thread again to reconnect.
 */
class ClientConnectThread extends AsyncTask<Void,Void,String> implements DialogInterface.OnClickListener {
    private final Context context;
    private final TcpClientRecipient recipient;
    private final Resources res;
    private ProgressDialog dialog;
    private JsonTcpClient client;

    private final static int CONNECTION_CHECK_TIMEOUT_MS = 10000;

    private ClientConnectThread(Context c, TcpClientRecipient recipient) {
        this.context = c;
        this.recipient = recipient;
        this.res=c.getResources();
    }

    @Override
    protected void onPreExecute() {
        //Make & show process dialog while attempting to connect..
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getResources().getString(R.string.connecting_attempt));
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
            /* Here we try to see if we can open a TCP socket to our host. */
        try {
            String serverUri = res.getString(R.string.server_uri);
            int serverPort = res.getInteger(R.integer.server_port);

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
        }
        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        if(dialog.isShowing())
            dialog.dismiss();
        if(error!=null) {
            client.stop();
            Dialog d = new AlertDialog.Builder(context)
                    .setMessage(error)
                    .setPositiveButton(R.string.retry_button, this)
                    .setNeutralButton(R.string.return_button, this).create();
            d.show();
        } else { //No error to show.
            recipient.receiveTcpClient(client);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which==Dialog.BUTTON_POSITIVE) {
            //Make new thread and try again.
            new ClientConnectThread(context,recipient).execute();
        }
        else if(which==Dialog.BUTTON_NEUTRAL) {
            Intent i = new Intent(context,MainActivity.class);
            context.startActivity(i);
        }
    }
}
