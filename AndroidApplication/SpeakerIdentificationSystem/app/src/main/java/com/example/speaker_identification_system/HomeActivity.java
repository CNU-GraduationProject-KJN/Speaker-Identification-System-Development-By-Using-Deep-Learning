package com.example.speaker_identification_system;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speaker_identification_system.Dialog.ChooseDialogFragment;
import com.example.speaker_identification_system.Dialog.RemoveDialogFragment;

public class HomeActivity extends AppCompatActivity {


    private long backKeyPressedTime = 0;
    private Toast toast;
    TextView member_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        member_name = (TextView)findViewById(R.id.member_name);
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

    public void onClick_BtnModify(View v){
        ChooseDialogFragment dialog = ChooseDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    public void onClick_BtnRemove(View v){
        RemoveDialogFragment dialog = RemoveDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), "dialog");
    }
}