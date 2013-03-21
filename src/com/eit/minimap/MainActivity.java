package com.eit.minimap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * Activity defining the main menu. Just a few buttons and such.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.main_menu);
        Button joinButton = (Button) findViewById(R.id.join_group);
        joinButton.setOnClickListener(this);
        Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        Button quitButton = (Button) findViewById(R.id.quit_button);
        quitButton.setOnClickListener(this);
    }

    /**
     * Logic for what happens when you press the "Join group" button
     * @param v Join group button view.
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.quit_button) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
        // Starts settings menu
        if(v.getId() == R.id.settings_button){
            Intent SettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(SettingsIntent);
            return;
        }
        // Go to next activity!
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivity(myIntent);
    }
}
