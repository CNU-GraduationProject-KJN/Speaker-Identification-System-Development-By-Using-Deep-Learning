package com.example.speaker_identification_system;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainLoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_loading);

        ImageView bomb = (ImageView)findViewById(R.id.bomb);
        ImageView user = (ImageView)findViewById(R.id.user);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha);

        bomb.setVisibility(View.INVISIBLE);
        user.setVisibility(View.INVISIBLE);
        bomb.startAnimation(animation);
        iconAni();
        startLoading();
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    private void iconAni(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha2);
                ImageView user = (ImageView)findViewById(R.id.user);
                ImageView unknown_user = (ImageView)findViewById(R.id.unknown_user);
                ImageView bomb = (ImageView)findViewById(R.id.bomb);

                unknown_user.setVisibility(View.INVISIBLE);
                user.setVisibility(View.VISIBLE);
                bomb.startAnimation(animation2);
                bomb.setVisibility(View.INVISIBLE);
            }
        },2000);
    }

    private void startLoading(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },4000);
    }
}