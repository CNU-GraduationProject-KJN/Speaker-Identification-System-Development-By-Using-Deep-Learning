package com.example.speaker_identification_system.Modify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.speaker_identification_system.MainActivity;
import com.example.speaker_identification_system.R;
import com.example.speaker_identification_system.Register.RegisterLoadingActivity;
import com.example.speaker_identification_system.Registration.RegistrationActivity;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ModifyVoiceLoadingActivity extends AppCompatActivity {

    private static final String TAG = "ModifyVoice";
    private static final String mServerUrl = "http://168.188.126.212:3000/modifyVoice/";

    private ModifyVoiceLoadingActivity.CompressFiles mCompressFiles;

    private String mUserInfo;
    private ArrayList<String> mFilePathList;

    private String mzipFilePath;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_loading_voice);

        Intent get_intent = getIntent();
        mUserInfo = (String)get_intent.getStringExtra("mUserInfo");
        mFilePathList = (ArrayList<String>)get_intent.getSerializableExtra("mFilePathList");

        //zip file name , not full path
        mzipFilePath = mUserInfo+".zip";
        // compress and transfer
        mCompressFiles = new ModifyVoiceLoadingActivity.CompressFiles();
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void modify_voice_result(){
        if(result.equals("OK")){
            Toast.makeText(getApplicationContext(), "음성수정완료", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "오류: 음성수정실패", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    // AsyncTask의 파라미터 3개는 각각 onPreExecute,onProgressUpdate,onPostExecute
    private class CompressFiles extends AsyncTask<Void, Integer, Boolean> {

        ProgressDialog asyncDialog;
        File file;
        @Override
        protected void onPreExecute() {
            asyncDialog = new ProgressDialog(ModifyVoiceLoadingActivity.this);
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
                    result = (String) responseJSON.get("result");

                    if(result.equals("OK")){
                        success[0] = true;
                    }

                    Log.i(TAG, "DATA response = " + result);
                }
                Log.d(TAG, "status : "+ status);

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

            modify_voice_result();
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
                Toast.makeText(getApplication(), "업로드 완료", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //for progress bar
    public void setProgress(int progressRate, int taskFlag) {
        mCompressFiles.publish(progressRate, taskFlag);
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


    private static final int BUFFER = 2048;

    //compress files into path : zipFilePath
    public void zip(String zipFilePath) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFilePath);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];
            for (int i = 0; i < mFilePathList.size(); i++) {

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
    private String convertFileToString(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(Base64.encode(bytes, Base64.DEFAULT));
    }

}
