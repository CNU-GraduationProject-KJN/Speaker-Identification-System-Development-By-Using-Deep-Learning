package com.example.speaker_identification_system.Register;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speaker_identification_system.Dialog.ErrorMsgDialogFragment;
import com.example.speaker_identification_system.Dialog.OKMsgDialogFragment;
import com.example.speaker_identification_system.Lookup.LookupLoadingActivity;
import com.example.speaker_identification_system.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RegisterVoiceActivity extends AppCompatActivity {

    private static final String TAG = "RegisterVoice";

    ImageView btn_record_stop;
    ImageView btn_record_start;

    Button btn_next;
    TextView script;
    TextView announcement;

    ImageView check1;
    ImageView check2;
    ImageView check3;
    ImageView check4;
    ImageView check5;

    public AudioRecord mAudioRecorder;

    private ArrayList<String> mFilePathList;
    private ArrayList<String> mFileNameList;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private boolean mRecordStatus = false;

    private String mUserInfo;
    private String mUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_voice);

        Intent getIntent = getIntent();

        mUserName = getIntent.getExtras().getString("UserName");
        mUserInfo = getIntent.getExtras().getString("UserInfo");

        mFileNameList = new ArrayList<>();
        mFilePathList = new ArrayList<>();

        announcement = (TextView)findViewById(R.id.announcement);
        script = (TextView)findViewById(R.id.script);

        btn_record_stop = (ImageView)findViewById(R.id.btn_record_stop);
        btn_record_start = (ImageView)findViewById(R.id.btn_record_start);
        btn_next = (Button)findViewById(R.id.btn_next);

        script.setVisibility(View.INVISIBLE);
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        btn_next.setVisibility(View.INVISIBLE);

        check1 = (ImageView)findViewById(R.id.check1);
        check2 = (ImageView)findViewById(R.id.check2);
        check3 = (ImageView)findViewById(R.id.check3);
        check4 = (ImageView)findViewById(R.id.check4);
        check5 = (ImageView)findViewById(R.id.check5);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void onClick_BtnPrevious(View v){
        finish();
    }

    public void onClick_BtnNext(View v){
        Intent intent = new Intent(this, RegisterLoadingActivity.class);
        intent.putExtra("mUserInfo", mUserInfo);
        intent.putExtra("mUserName", mUserName);
        intent.putExtra("mFilePathList", mFilePathList);
        startActivity(intent);
    }

    public void onClick_RecordStart(View v){
        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);

        mRecordStatus = true;
        Toast.makeText(RegisterVoiceActivity.this, (mFileNameList.size()+1)+" 번째 녹음",Toast.LENGTH_SHORT).show();

        if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecorder.startRecording();

        //Record Thread
        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                int writeCheck = ContextCompat.checkSelfPermission(RegisterVoiceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int readCheck = ContextCompat.checkSelfPermission(RegisterVoiceActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
                btn_next.setVisibility(View.VISIBLE);
                btn_record_start.setClickable(false);
                check5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_full_user));
                break;
        }
    }
}
