package com.example.speaker_identification_system.HomeActivity;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.speaker_identification_system.Identification.IdentificationActivity;
import com.example.speaker_identification_system.R;
import com.example.speaker_identification_system.Registration.RegistrationActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SelectActivity";

    private Button mGotoRecordingButton;
    private Button mGotoIdentifyingButton;

    private Intent recordingActivityIntent;
    private Intent identificationActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeactivity_main);

        mGotoRecordingButton = (Button) findViewById(R.id.register_user_);
        mGotoIdentifyingButton = (Button) findViewById(R.id.identify_user_);

        mGotoRecordingButton.setOnClickListener(goToRecordActivity);
        mGotoIdentifyingButton.setOnClickListener(goToIdentifyActivity);

    }
    View.OnClickListener goToRecordActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            recordingActivityIntent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(recordingActivityIntent);
        }
    };
    View.OnClickListener goToIdentifyActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            identificationActivityIntent = new Intent(MainActivity.this, IdentificationActivity.class);
            startActivity(identificationActivityIntent);
        }
    };
}