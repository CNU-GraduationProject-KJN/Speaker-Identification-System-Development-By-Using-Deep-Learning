package com.example.speaker_identification_system.Lookup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.speaker_identification_system.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LookupActivity extends AppCompatActivity {

    private static final String TAG = "Lookup";

    ImageView btn_record_stop;
    ImageView btn_record_start;

    Button btn_next;
    TextView script;
    TextView announcement;

    public AudioRecord mAudioRecorder;
    public AudioTrack mAudioTrack;

    private ArrayList<String> mFilePathList;
    private ArrayList<String> mFileNameList;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private boolean mRecordStatus = false;

    private String check_UserInfo;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup);

        announcement = (TextView)findViewById(R.id.announcement);
        script = (TextView)findViewById(R.id.script);

        btn_record_stop = (ImageView)findViewById(R.id.btn_record_stop);
        btn_record_start = (ImageView)findViewById(R.id.btn_record_start);
        btn_next = (Button)findViewById(R.id.btn_next);

        script.setVisibility(View.INVISIBLE);
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        btn_next.setVisibility(View.INVISIBLE);

        mFileNameList = new ArrayList<>();
        mFilePathList = new ArrayList<>();

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    public void onClick_BtnPrevious(View v){
        finish();
    }

    public void onClick_BtnNext(View v){

        Intent intent = new Intent(this, LookupLoadingActivity.class);
        intent.putExtra("check_UserInfo", check_UserInfo);
        intent.putExtra("mFilePathList", mFilePathList);
        startActivity(intent);
    }

    public void onClick_RecordStart(View v){

        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);
        Date currentTime = Calendar.getInstance().getTime();
        check_UserInfo = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        mRecordStatus = true;

        if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecorder.startRecording();

        //Record Thread
        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                int writeCheck = ContextCompat.checkSelfPermission(LookupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int readCheck = ContextCompat.checkSelfPermission(LookupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(writeCheck != PackageManager.PERMISSION_GRANTED && readCheck != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "No Privileges");
                }
                if(mFileNameList.isEmpty()) {
                    File dir = Environment.getExternalStoragePublicDirectory(
                            check_UserInfo);
                    dir.mkdir();
                    Log.d(TAG, "dir Path : "+dir.getAbsolutePath());
                    Log.d(TAG, "Dir Exist? : "+ dir.exists());
                }
                File pcmfile = Environment.getExternalStoragePublicDirectory(
                        check_UserInfo +"/"+(mFilePathList.size()+1)+".pcm");
                Log.d(TAG, "File Exist? : "+ pcmfile.exists());
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ check_UserInfo +"/"+(mFilePathList.size()+1)+".pcm";
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
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        script.setVisibility(View.INVISIBLE);
        mRecordStatus = false;
        announcement.setText("녹음이 끝났습니다. 상단에 신원확인 버튼을 눌러주세요.");
        btn_next.setVisibility(View.VISIBLE);
        btn_record_start.setClickable(false);
    }


}