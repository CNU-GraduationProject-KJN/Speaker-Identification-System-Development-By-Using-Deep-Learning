package com.example.speaker_identification_system.Modify;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.speaker_identification_system.R;

public class ModifyAudioActivity extends AppCompatActivity {


    ImageView btn_record_stop;
    ImageView btn_record_start;
    Integer idx=0;

    Button btn_modify;
    TextView script;
    TextView announcement;

    ImageView check1;
    ImageView check2;
    ImageView check3;
    ImageView check4;
    ImageView check5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_audio);

        announcement = (TextView)findViewById(R.id.announcement);
        script = (TextView)findViewById(R.id.script);

        btn_record_stop = (ImageView)findViewById(R.id.btn_record_stop);
        btn_record_start = (ImageView)findViewById(R.id.btn_record_start);
        btn_modify = (Button)findViewById(R.id.btn_modify);

        script.setVisibility(View.INVISIBLE);
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        btn_modify.setVisibility(View.INVISIBLE);

        check1 = (ImageView)findViewById(R.id.check1);
        check2 = (ImageView)findViewById(R.id.check2);
        check3 = (ImageView)findViewById(R.id.check3);
        check4 = (ImageView)findViewById(R.id.check4);
        check5 = (ImageView)findViewById(R.id.check5);

        idx=0;

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    public void onClick_BtnModify(View v){
        Intent intent = new Intent(this, ModifyLoadingActivity.class);
        startActivity(intent);
    }

    public void onClick_RecordStart(View v){
        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);
        announcement.setText("녹음이 끝나면 위 아이콘을 눌러주세요.");
    }

    public void onClick_RecordStop(View v){
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        announcement.setText("위 아이콘을 눌러 녹음을 시작해 주세요.");
        script.setVisibility(View.INVISIBLE);

        switch(idx){
            case 0:
                check1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                idx++;
                break;
            case 1:
                check2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                idx++;
                break;
            case 2:
                check3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                idx++;
                break;
            case 3:
                check4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                idx++;
                break;
            case 4:
                announcement.setText("녹음이 끝났습니다. 하단에 재등록 버튼을 눌러주세요.");
                btn_modify.setVisibility(View.VISIBLE);
                btn_record_start.setClickable(false);
                check5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                idx++;
                break;
        }
    }
}
