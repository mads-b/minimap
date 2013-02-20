package com.eit.minimap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Properties;

/**
 * Activity defining the main menu. Just a few buttons and such.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private EditText groupName;
    private EditText yourName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        Button joinButton = (Button) findViewById(R.id.join_group);
        joinButton.setOnClickListener(this);

        groupName = (EditText) findViewById(R.id.group_name);
        yourName = (EditText) findViewById(R.id.your_name);
        // Set fields to old values, if they exist.
        Properties properties = System.getProperties();
        if(properties.contains("groupName"))
            groupName.setText(properties.getProperty("groupName"));
        if(properties.contains("yourName"))
            groupName.setText(properties.getProperty("yourName"));
    }

    @Override
    public void onClick(View v) {
        String groupNameStr = groupName.getText().toString();
        String yourNameStr = yourName.getText().toString();
        // Error if some fields are empty
        if(groupNameStr.isEmpty() || yourNameStr.isEmpty()) {
            Toast.makeText(this,R.string.error_fields_empty,Toast.LENGTH_LONG).show();
            return;
        }
        // Store these strings for later use in the app-wide properties.
        Properties properties = System.getProperties();
        properties.put("groupName",groupNameStr);
        properties.put("yourName",yourNameStr);

        // Go to next activity!
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivity(myIntent);
    }
}
