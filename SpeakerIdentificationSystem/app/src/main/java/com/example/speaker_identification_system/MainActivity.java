package com.example.speaker_identification_system;

import android.media.AudioFormat;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button mRecordButton;
    private Button mTransferButton;
    private TextView mRecordText;

    public AudioRecord mAudioRecorder;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private String mFilePath;
    private String mUserInfo;

    private Thread mRecordThread;
    private boolean mRecordStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecordButton = (Button) findViewById(R.id.record_);
        mTransferButton = (Button) findViewById(R.id.transfer_);
        mRecordText = (TextView) findViewById(R.id.record_text);

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mUserInfo+".pcm";
                FileOutputStream fos = null;

                try{
                    fos = new FileOutputStream(mFilePath);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                }

                while(mRecordStatus){
                    int readByte = mAudioRecorder.read(readData, 0, mBufferSize);
                    Log.d(TAG, "Read "+readByte+" bytes");
                }

                mAudioRecorder.stop();
                mAudioRecorder.release();
                mAudioRecorder = null;

                try {
                    fos.close();

                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        });

        mRecordButton.setOnClickListener(recordVoice);
        mTransferButton.setOnClickListener(transferVoice);

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
                Log.d(TAG, "AudioRecorder is "+mAudioRecorder);
                mAudioRecorder.startRecording();
                mRecordThread.start();
            }else {
                mRecordButton.setText("Record");
                mRecordStatus = false;

                mRecordText.setText("");
            }
        }
    };
    View.OnClickListener transferVoice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}