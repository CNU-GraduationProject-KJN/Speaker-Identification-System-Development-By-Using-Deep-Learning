package com.example.speaker_identification_system;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.content.Intent;

import com.example.speaker_identification_system.Lookup.LookupActivity;
import com.example.speaker_identification_system.Registration.RegistrationActivity;

public class MainActivity extends AppCompatActivity {

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, MainLoadingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }
    public void onClick_BtnRegister(View v){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }


    public void onClick_BtnLookup(View v){
        Intent intent = new Intent(this, LookupActivity.class);
        startActivity(intent);
    }

}