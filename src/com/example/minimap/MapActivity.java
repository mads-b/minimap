package com.example.minimap;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.MapFragment;

public class MapActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
