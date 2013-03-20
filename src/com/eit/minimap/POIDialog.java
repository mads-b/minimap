package com.eit.minimap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.eit.minimap.datastructures.POI;
import com.eit.minimap.datastructures.POIHandler;
import com.google.android.gms.maps.model.LatLng;

/**
 * Dialog for adding, viewing and deleting Points Of Interest.
 */
public class POIDialog implements
        View.OnClickListener,
        DialogInterface.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private final Context context;
    private final POIHandler handler;
    // Cached vars set when dialogs are shown, to be used when dialogs are dismissed.
    private POI curPoi;
    private LatLng lastPos;
    private View newPoiView;
    private AlertDialog newPoiDialog;

    public POIDialog(POIHandler handler,Context context) {
        this.handler = handler;
        this.context=context;
    }

    public void makeNewPoiDialog(LatLng pos) {
        newPoiView = LayoutInflater.from(context).inflate(R.layout.poi_dialog,null);
        lastPos = pos;
        Button makeButton = (Button) newPoiView.findViewById(R.id.make);
        makeButton.setOnClickListener(this);
        // Set up the seeker bar.
        SeekBar seekRadius = (SeekBar) newPoiView.findViewById(R.id.poi_radius_seeker);
        seekRadius.setOnSeekBarChangeListener(this);
        onProgressChanged(seekRadius,20,false);

        newPoiDialog = new AlertDialog.Builder(context).setView(newPoiView).create();
        newPoiDialog.show();
    }

    public void makeViewPoiDialog(POI poi) {
        curPoi = poi;
        new AlertDialog.Builder(context)
                .setMessage(poi.getDescription())
                .setTitle("Point Of Interest")
        .setPositiveButton(R.string.delete,this)
        .setNegativeButton(R.string.exit, this)
        .create().show();
    }

    @Override
    public void onClick(View v) {
        // Button on the "new POI" dialog pushed. Make new POI!
        EditText description = (EditText) newPoiView.findViewById(R.id.add_poi_msg);
        SeekBar seekRadius = (SeekBar) newPoiView.findViewById(R.id.poi_radius_seeker);

        handler.addPOI(new POI(
                lastPos,
                seekRadius.getProgress(),
                description.getText().toString(),
                "noMac"));
        newPoiDialog.dismiss();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //Clicked button on the dialog showing POI. which==-1 is delete button, -2 is exit.
        Log.d("com.eit.minimap.POIDialog","Selected button "+which);
        if(which == -1) handler.removePOI(curPoi);
        dialog.dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        TextView seekRadiusText = (TextView) newPoiView.findViewById(R.id.poi_radius_text);
        String text = context.getString(R.string.poi_radius,seekBar.getProgress());
        seekRadiusText.setText(text);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
