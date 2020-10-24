package com.example.speaker_identification_system.Modify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speaker_identification_system.R;
import com.example.speaker_identification_system.Register.RegisterVoiceActivity;
import com.example.speaker_identification_system.Registration.RegistrationActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ModifyVoiceActivity extends AppCompatActivity {

    private static final String TAG = "ModifyVoice";
    private String mUserInfo;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private ImageView btn_record_stop;
    private ImageView btn_record_start;
    private Integer idx=0;

    private Button btn_modify;
    private TextView script;
    private TextView announcement;

    private ArrayList<String> mFilePathList;
    private ArrayList<String> mFileNameList;

    private ImageView check1;
    private ImageView check2;
    private ImageView check3;
    private ImageView check4;
    private ImageView check5;

    public AudioRecord mAudioRecorder;
    public AudioTrack mAudioTrack;

    private boolean mRecordStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_voice);

        Intent get_intent = getIntent();
        mUserInfo = (String)get_intent.getStringExtra("mUserInfo");

        mFileNameList = new ArrayList<>();
        mFilePathList = new ArrayList<>();

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

        //for Checking User Voice
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM); // AudioTrack 생성

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    public void onClick_BtnModify(View v){

        Intent intent = new Intent(this, ModifyVoiceLoadingActivity.class);
        intent.putExtra("mFilePathList",mFilePathList);
        intent.putExtra("mUserInfo",mUserInfo);
        startActivity(intent);
    }

    public void onClick_RecordStart(View v){
        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);

        mRecordStatus = true;
        Toast.makeText(this, (mFileNameList.size()+1)+" 번째 녹음",Toast.LENGTH_SHORT).show();

        if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecorder.startRecording();

        //Record Thread
        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                int writeCheck = ContextCompat.checkSelfPermission(ModifyVoiceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int readCheck = ContextCompat.checkSelfPermission(ModifyVoiceActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(writeCheck != PackageManager.PERMISSION_GRANTED && readCheck != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "No Privileges");
                }
                if(mFileNameList.isEmpty()) {
                    File dir = Environment.getExternalStoragePublicDirectory(
                            mUserInfo);
                    dir.mkdir();
                    Log.d(TAG, "dir Path : "+dir.getAbsolutePath());
                    Log.d(TAG, "Dir Exist? : "+ dir.exists());
                }
                File pcmfile = Environment.getExternalStoragePublicDirectory(
                        mUserInfo+"/"+(mFilePathList.size()+1)+".pcm");
                Log.d(TAG, "File Exist? : "+ pcmfile.exists());
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mUserInfo+"/"+(mFilePathList.size()+1)+".pcm";
                String fileName = (mFilePathList.size()+1)+".pcm";

                Log.d(TAG, "filePath :  "+filePath);
                Log.d(TAG, "File State : "+Environment.getExternalStorageState());

                try (FileOutputStream fos = new FileOutputStream(filePath)){

                    while(mRecordStatus){
                        int readByte = mAudioRecorder.read(readData, 0, mBufferSize);

                        fos.write(readData, 0, mBufferSize);
                        Log.d(TAG, "Read "+readByte+" bytes");
                    }

                    mAudioRecorder.stop();
                    mAudioRecorder.release();
                    mAudioRecorder = null;

                    mFilePathList.add(filePath);
                    mFileNameList.add(fileName);

                } catch (FileNotFoundException fileNotFoundErr) {
                    fileNotFoundErr.printStackTrace();
                    Log.d(TAG, "Error : "+fileNotFoundErr);
                }catch (IOException ioErr) {
                    ioErr.printStackTrace();
                    Log.d(TAG, "Error : "+ioErr);
                }
            }
        });
        recordThread.start();

        announcement.setText("녹음이 끝나면 위 아이콘을 눌러주세요.");
    }

    public void onClick_RecordStop(View v){
        mRecordStatus = false;
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        announcement.setText("위 아이콘을 눌러 녹음을 시작해 주세요.");
        script.setVisibility(View.INVISIBLE);

        switch(mFileNameList.size()){
            case 0:
                check1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
            case 1:
                check2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
            case 2:
                check3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
            case 3:
                check4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
            case 4:
                announcement.setText("녹음이 끝났습니다. 상단에 신원등록 버튼을 눌러주세요.");
                btn_modify.setVisibility(View.VISIBLE);
                btn_record_start.setClickable(false);
                check5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
        }
    }
}
