package com.example.speaker_identification_system.HomeActivity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speaker_identification_system.R;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RegisterAudio";

    private Button mRecordButton;
    private Button mTransferButton;
    private Button mPlayButton;

    private TextView mRecordText;

    private android.support.design.widget.TextInputEditText mNameText;
    private android.support.design.widget.TextInputEditText mIdentificationNumber;

    private Button mSetInfoButton;
    private boolean mSettingStatus = false;

    public AudioRecord mAudioRecorder;
    public AudioTrack mAudioTrack;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private String mFilePath;
    private String mUserInfo;

    private Thread mRecordThread;
    private boolean mRecordStatus = false;

    private Thread mPlayThread;
    private boolean mPlayStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeactivity_main);



        mRecordButton = (Button) findViewById(R.id.record_);
        mTransferButton = (Button) findViewById(R.id.transfer_);
        mPlayButton = (Button) findViewById(R.id.play_);
        mRecordText = (TextView) findViewById(R.id.record_text);

        mNameText = (android.support.design.widget.TextInputEditText) findViewById(R.id.user_name);
        mIdentificationNumber = (android.support.design.widget.TextInputEditText) findViewById(R.id.identification_number);
        mSetInfoButton = (Button) findViewById(R.id.setID);



        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mUserInfo+".pcm";

                Log.d(TAG, "mFilePath :  "+mFilePath);
                Log.d(TAG, "File State : "+Environment.getExternalStorageState());

                try (FileOutputStream fos = new FileOutputStream(mFilePath)){

                    while(mRecordStatus){
                        int readByte = mAudioRecorder.read(readData, 0, mBufferSize);

                        fos.write(readData, 0, mBufferSize);
                        Log.d(TAG, "Read "+readByte+" bytes");
                    }

                    mAudioRecorder.stop();
                    mAudioRecorder.release();
                    mAudioRecorder = null;

                } catch (FileNotFoundException fileNotFoundErr) {
                    fileNotFoundErr.printStackTrace();
                    Log.d(TAG, "Error : "+fileNotFoundErr);
                }catch (IOException ioErr) {
                    ioErr.printStackTrace();
                    Log.d(TAG, "Error : "+ioErr);
                }
            }
        });
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM); // AudioTrack 생성
        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] writeData = new byte[mBufferSize];

                try (FileInputStream fis = new FileInputStream(mFilePath);
                     DataInputStream dis = new DataInputStream(fis);
                ){
                    mAudioTrack.play();

                    while(mPlayStatus) {
                        try {
                            int ret = dis.read(writeData, 0, mBufferSize);
                            if (ret <= 0) {
                                (MainActivity.this).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPlayStatus = false;
                                        mPlayButton.setText("Play");
                                    }
                                });
                                break;
                            }
                            mAudioTrack.write(writeData, 0, ret);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    mAudioTrack.stop();
                    mAudioTrack.release();
                    mAudioTrack = null;

                } catch (FileNotFoundException fileNotFoundErr) {
                    fileNotFoundErr.printStackTrace();
                    Log.d(TAG, "Error : "+fileNotFoundErr);
                } catch (IOException ioErr) {
                    ioErr.printStackTrace();
                    Log.d(TAG, "Error : "+ioErr);
                }

            }
        });

        mRecordButton.setOnClickListener(recordVoice);
        mTransferButton.setOnClickListener(transferVoice);
        mPlayButton.setOnClickListener(playAudio);
        mSetInfoButton.setOnClickListener(settingInfo);

    }

    View.OnClickListener recordVoice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!mRecordStatus){
                mRecordButton.setText("Done");
                mRecordStatus = true;

                mRecordText.setText("\n 총체주의는 특정 가설에 대해 제기되는 반박이 결정적인 것처럼 보이더라도\n" +
                        "그 가설이 실용적으로 필요하다고 인정되면\n" +
                        "언제든 그와 같은 반박을 피하는 방법을 강구하여 그 가설을 받아들일 수 있다. 그러나 총체주의는 \"A이면서 동시에 A가 아닐 수는 없다.\"와 같은 논리학의 법칙처럼\n" +
                        "아무도 의심하지 않는 지식은 분석 명제로 분류해야 하는 것이 아니냐는 비판에\n" +
                        "답해야 하는 어려움이 있다.\n");


                if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                mAudioRecorder.startRecording();
                mRecordThread.start();
            }else {
                mRecordButton.setText("Record");
                mRecordStatus = false;

                mRecordText.setText("");
                Toast.makeText(MainActivity.this, "File Path : "+mFilePath, Toast.LENGTH_SHORT).show();
            }
        }
    };
    View.OnClickListener transferVoice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };
    View.OnClickListener settingInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUserInfo = mNameText.getText().toString()+"_"+getMD5(mIdentificationNumber.getText().toString());
            Toast.makeText(MainActivity.this, "Setting User Info", Toast.LENGTH_SHORT).show();
        }
    };
    View.OnClickListener playAudio = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mPlayStatus){
                mPlayButton.setText("Stop");
                mPlayStatus = true;

                if(mAudioTrack == null) mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
                mPlayThread.start();

            }else {
                mPlayButton.setText("Play");
                mPlayStatus = false;
            }
        }
    };

    public String getMD5(String str){
        String MD5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(str.getBytes());
            byte byteData[] = md.digest();

            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            MD5 = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            MD5 = null;
        }
        return MD5;
    }
}