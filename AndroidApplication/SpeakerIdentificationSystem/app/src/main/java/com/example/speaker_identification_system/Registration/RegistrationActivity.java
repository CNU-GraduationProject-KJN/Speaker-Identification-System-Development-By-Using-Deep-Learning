package com.example.speaker_identification_system.Registration;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speaker_identification_system.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegisterAudio";
    private static final String mServerUrl = "http://168.188.126.212:3000/upload/";

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

    private String mSelectedFilePath;
    private String mzipFilePath;
    private String mUserInfo;
    private String mUserName;

    private boolean mRecordStatus = false;

    private boolean mPlayStatus = false;

    private ArrayList<String> mFilePathList;
    private ArrayList<String> mFileNameList;

    private CompressFiles mCompressFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_registeractivity);

        mFileNameList = new ArrayList<>();
        mFilePathList = new ArrayList<>();

        mRecordButton = (Button) findViewById(R.id.record_);
        mTransferButton = (Button) findViewById(R.id.transfer_);
        mPlayButton = (Button) findViewById(R.id.play_);
        mRecordText = (TextView) findViewById(R.id.record_text_);

        mNameText = (android.support.design.widget.TextInputEditText) findViewById(R.id.user_name_);
        mIdentificationNumber = (android.support.design.widget.TextInputEditText) findViewById(R.id.identification_number_);
        mSetInfoButton = (Button) findViewById(R.id.setID_);



        //for Checking User Voice
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM); // AudioTrack 생성

        mRecordButton.setOnClickListener(recordVoice);
        mTransferButton.setOnClickListener(transferVoice);
        mPlayButton.setOnClickListener(playAudio);
        mSetInfoButton.setOnClickListener(settingInfo);

    }
    View.OnClickListener recordVoice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!mRecordStatus){
                Toast.makeText(RegistrationActivity.this, (mFileNameList.size()+1)+" 번째 녹음",Toast.LENGTH_SHORT).show();
                mRecordButton.setText("Done");
                mRecordStatus = true;

                mRecordText.setText("\n 총체주의는 특정 가설에 대해 제기되는 반박이 결정적인 것처럼 보이더라도\n" +
                        "그 가설이 실용적으로 필요하다고 인정되면\n" +
                        "언제든 그와 같은 반박을 피하는 방법을 강구하여 그 가설을 받아들일 수 있다. 그러나 총체주의는 \"A이면서 동시에 A가 아닐 수는 없다.\"와 같은 논리학의 법칙처럼\n" +
                        "아무도 의심하지 않는 지식은 분석 명제로 분류해야 하는 것이 아니냐는 비판에\n" +
                        "답해야 하는 어려움이 있다.\n");


                if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                mAudioRecorder.startRecording();

                //Record Thread
                Thread recordThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] readData = new byte[mBufferSize];
                        int writeCheck = ContextCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        int readCheck = ContextCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
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

            }else {
                mRecordButton.setText("Record");
                mRecordStatus = false;

                mRecordText.setText("");
            }
        }
    };
    //
    public static File getOutputZipFile(String fileName) {

        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        Log.d(TAG, "PATH : "+mediaStorageDir.getPath() + File.separator + fileName);
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
    // AsyncTask의 파라미터 3개는 각각 onPreExecute,onProgressUpdate,onPostExecute
    private class CompressFiles extends AsyncTask<Void, Integer, Boolean> {

        ProgressDialog asyncDialog;
        File file;
        @Override
        protected void onPreExecute() {
            asyncDialog = new ProgressDialog(RegistrationActivity.this);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            asyncDialog.setMessage("압축중...");
            asyncDialog.setMax(100);
            asyncDialog.setCancelable(false);
            asyncDialog.setCanceledOnTouchOutside(false);

            asyncDialog.show();

            try {
                Log.i(TAG, "0% Completed");
            } catch (Exception ignored) {
            }
        }

        protected Boolean doInBackground(Void... urls) {

            file = getOutputZipFile(mzipFilePath);
            Log.d(TAG, "fileName : "+file.toString());

            String zipFileName;
            if (file != null) {
                zipFileName = file.getAbsolutePath();
                if (mFilePathList.size() > 0) {
                    zip(zipFileName);
                }
            }

            publishProgress(0, 100, 1);
            final boolean[] success = {false};

            HttpURLConnection connection = null;
            OutputStream outputStream = null;

            JSONObject jsonObject = null;
            StringBuffer buffer = null;
            URL url = null;
            InputStream stream = null;
            BufferedReader reader = null;

            try{
                String urlServer = mServerUrl + mUserInfo;
                mCompressFiles.publish(30, 30);
                jsonObject = new JSONObject();
                jsonObject.put("fileName", mUserInfo);
                jsonObject.put("userName", mUserName);
                jsonObject.put("file", convertFileToString(file));
                Log.d(TAG, convertFileToString(file).substring(0, 100));

                url = new URL(urlServer);
                //Connection
                mCompressFiles.publish(50, 50);
                connection = (HttpURLConnection) url.openConnection();

                //Connection in & output setting
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //Connecion header setting
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                Log.d(TAG, "try connect");
                //연결
                mCompressFiles.publish(70, 70);
                connection.connect();
                Log.d(TAG, "connect");

                mCompressFiles.publish(90, 90);
                //write JSON file
                outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();

                mCompressFiles.publish(100, 100);
                // get server response , not complete !
                int status = connection.getResponseCode();

                Log.d(TAG, "status : "+ status);

                mFileNameList = new ArrayList<>();
                mFilePathList = new ArrayList<>();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(connection != null) connection.disconnect();
                try {
                    if(reader != null) reader.close();
                    success[0] = true;
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }


            file.deleteOnExit(); // delete zip file
            return success[0];
        }

        public void publish(int progressRate, int taskFlag) {
            if(taskFlag == 0){
                int totalNumberOfFiles = mFilePathList.size();
                publishProgress(progressRate, totalNumberOfFiles, taskFlag);
            }else{
                publishProgress(progressRate, 100, taskFlag);
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            try {
                Log.d(TAG, "progress[0] : "+progress[0]);
                Log.d(TAG, "progress[1] : "+progress[1]);
                Log.d(TAG, "progress[2] : "+progress[2]);

                asyncDialog.setProgress(progress[0]);
                asyncDialog.setMax(progress[1]);
                String msg = (progress[2] == 0) ? "압축 중" :
                        (progress[2] == 30) ? "파일 준비 중" :
                                (30 < progress[2] && progress[2] <= 70) ? "연결 중" :
                                        (70 < progress[2] && progress[2] < 100) ? "전송 중" : "전송 완료";

                asyncDialog.setMessage(msg);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        protected void onPostExecute(Boolean flag) {
            Log.d(TAG, "COMPLETED : "+ flag);
            asyncDialog.dismiss();

            if (flag) Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
        }
    }
    //for progress bar
    public void setProgress(int progressRate, int taskFlag) {
        mCompressFiles.publish(progressRate, taskFlag);
    }

    View.OnClickListener transferVoice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(mFileNameList.size() < 5){
                Toast.makeText(RegistrationActivity.this, "Please Record Voice 5 times.", Toast.LENGTH_SHORT).show();
                return;
            }
            //zip file name , not full path
            mzipFilePath = mUserInfo+".zip";
            // compress and transfer
            mCompressFiles = new CompressFiles();
            mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
    };
    //save user info
    View.OnClickListener settingInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUserName = mNameText.getText().toString();
            mUserInfo = getMD5(mNameText.getText().toString()+"_"+mIdentificationNumber.getText().toString());
            Toast.makeText(RegistrationActivity.this, "Setting User Info", Toast.LENGTH_SHORT).show();

            mNameText.setText("");
            mIdentificationNumber.setText("");
        }
    };
    View.OnClickListener playAudio = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mPlayStatus){
                mPlayButton.setText("Stop");
                mPlayStatus = true;

                final String[] items = new String[mFileNameList.size()];
                for(int i=0;i<mFileNameList.size();i++) items[i] = mFileNameList.get(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle("파일을 선택하세요.")
                        .setItems(items
                                , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                mSelectedFilePath = mFilePathList.get(which);
                            }
                        });
                builder.create();

                if(mAudioTrack == null) mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
                Thread playThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] writeData = new byte[mBufferSize];

                        try (FileInputStream fis = new FileInputStream(mSelectedFilePath);
                             DataInputStream dis = new DataInputStream(fis);
                        ){
                            mAudioTrack.play();

                            while(mPlayStatus) {
                                try {
                                    int ret = dis.read(writeData, 0, mBufferSize);
                                    if (ret <= 0) {
                                        (RegistrationActivity.this).runOnUiThread(new Runnable() {
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

                playThread.start();

            }else {
                mPlayButton.setText("Play");
                mPlayStatus = false;
            }
        }
    };
    //get md5
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
    private static final int BUFFER = 2048;

    //compress files into path : zipFilePath
    public void zip(String zipFilePath) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFilePath);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];
            for (int i = 0; i < mFilePathList.size(); i++) {
                setProgress(i + 1, 0);

                FileInputStream fis = new FileInputStream(mFilePathList.get(i));
                origin = new BufferedInputStream(fis, BUFFER);
                ZipEntry entry = new ZipEntry(mFilePathList.get(i).substring(mFilePathList.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //covert file to base64 encoding
    private String convertFileToString(File file) throws IOException{
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(Base64.encode(bytes, Base64.DEFAULT));
    }
}