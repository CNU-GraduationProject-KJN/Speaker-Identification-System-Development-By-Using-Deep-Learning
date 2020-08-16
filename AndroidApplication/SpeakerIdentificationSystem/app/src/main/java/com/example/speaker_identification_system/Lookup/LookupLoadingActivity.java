package com.example.speaker_identification_system.Lookup;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.speaker_identification_system.Dialog.UnknownDialogFagment;
import com.example.speaker_identification_system.R;

public class LookupLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup_loading);
        startLoading();
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    private void startLoading(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UnknownDialogFagment dialog = UnknownDialogFagment.newInstance();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        },2000);
    }
}
