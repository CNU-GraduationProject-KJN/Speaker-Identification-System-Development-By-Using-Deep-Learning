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

public class RegisterAudioActivity extends AppCompatActivity {

    private static final String TAG = "RegisterAudio";
    private static final String mServerUrl = "http://168.188.126.212:3000/upload/";

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
    public AudioTrack mAudioTrack;

    private ArrayList<String> mFilePathList;
    private ArrayList<String> mFileNameList;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private boolean mRecordStatus = false;

    private String mSelectedFilePath;
    private String mzipFilePath;
    private String mUserInfo;
    private String mUserName;

    private RegisterAudioActivity.CompressFiles mCompressFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_audio);

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
        if(mFileNameList.size() < 5){
            Toast.makeText(RegisterAudioActivity.this, "Please Record Voice 5 times.", Toast.LENGTH_SHORT).show();
            return;
        }
        //zip file name , not full path
        mzipFilePath = mUserInfo+".zip";
        // compress and transfer
        mCompressFiles = new RegisterAudioActivity.CompressFiles();
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onClick_RecordStart(View v){
        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);

        mRecordStatus = true;
        Toast.makeText(RegisterAudioActivity.this, (mFileNameList.size()+1)+" 번째 녹음",Toast.LENGTH_SHORT).show();

        if(mAudioRecorder == null) mAudioRecorder = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecorder.startRecording();

        //Record Thread
        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                int writeCheck = ContextCompat.checkSelfPermission(RegisterAudioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int readCheck = ContextCompat.checkSelfPermission(RegisterAudioActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
            asyncDialog = new ProgressDialog(RegisterAudioActivity.this);
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
                InputStream is = null;
                ByteArrayOutputStream baos =null;
                if(status == HttpURLConnection.HTTP_OK) {

                    is = connection.getInputStream();
                    baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();

                    String response = new String(byteData);

                    JSONObject responseJSON = new JSONObject(response);
                    String result = (String) responseJSON.get("result");

                    if(result.equals("OK")){
                        success[0] = true;
                    }
                    Log.i(TAG, "DATA response = " + result);
                }
                Log.d(TAG, "status : "+ status);

                mFileNameList = new ArrayList<>();
                mFilePathList = new ArrayList<>();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(connection != null) connection.disconnect();
                try {
                    if(reader != null) reader.close();
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

            if (flag) {
//                Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                OKMsgDialogFragment dialog = OKMsgDialogFragment.newInstance();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "dialog");
            }else{
                ErrorMsgDialogFragment dialog = ErrorMsgDialogFragment.newInstance();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        }
    }
    //for progress bar
    public void setProgress(int progressRate, int taskFlag) {
        mCompressFiles.publish(progressRate, taskFlag);
    }


    private static final int BUFFER = 2048;

    //compress files into path : zipFilePath
    public void zip(String zipFilePath) {
        try {
            BufferedInputStream origin;
            File info = Environment.getExternalStoragePublicDirectory(mUserInfo+"/info.txt");
            FileWriter fileWriter = new FileWriter(info);
            fileWriter.write(mUserName);
            fileWriter.close();

            mFilePathList.add(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mUserInfo+"/info.txt");

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
