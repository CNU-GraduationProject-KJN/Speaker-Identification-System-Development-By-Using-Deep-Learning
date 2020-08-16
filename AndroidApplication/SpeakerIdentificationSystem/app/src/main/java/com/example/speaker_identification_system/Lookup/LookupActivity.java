package com.example.speaker_identification_system.Lookup;

import android.Manifest;
import android.app.ProgressDialog;
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

import com.example.speaker_identification_system.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LookupActivity extends AppCompatActivity {

    private static final String TAG = "Lookup";
    private static final String mServerUrl = "http://168.188.126.212:3000/identify/";

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

    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    private boolean mRecordStatus = false;

    private String mSelectedFilePath;
    private String mzipFilePath;
    private String mUserInfo;
    private String mUserName;

    private LookupActivity.CompressFiles mCompressFiles;

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

        //zip file name , not full path
        mzipFilePath = mUserInfo+".zip";
        // compress and transfer
        mCompressFiles = new LookupActivity.CompressFiles();
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onClick_RecordStart(View v){
        btn_record_start.setVisibility(View.INVISIBLE);
        btn_record_stop.setVisibility(View.VISIBLE);
        script.setVisibility(View.VISIBLE);
        Date currentTime = Calendar.getInstance().getTime();
        mUserInfo = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
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
        btn_record_start.setVisibility(View.VISIBLE);
        btn_record_stop.setVisibility(View.INVISIBLE);
        script.setVisibility(View.INVISIBLE);
        mRecordStatus = false;
        announcement.setText("녹음이 끝났습니다. 상단에 신원확인 버튼을 눌러주세요.");
        btn_next.setVisibility(View.VISIBLE);
        btn_record_start.setClickable(false);
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
            asyncDialog = new ProgressDialog(LookupActivity.this);
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

            if (flag) {
                Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();

                /*********LookupLoadingActivity로 이동해서 신원 정보받아올때까지 기다리기*********/
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