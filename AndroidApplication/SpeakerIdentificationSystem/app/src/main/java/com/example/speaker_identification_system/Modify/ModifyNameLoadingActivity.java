package com.example.speaker_identification_system.Modify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.speaker_identification_system.Dialog.UnknownDialogFagment;
import com.example.speaker_identification_system.HomeActivity;
import com.example.speaker_identification_system.MainActivity;
import com.example.speaker_identification_system.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ModifyNameLoadingActivity extends AppCompatActivity {

    private static final String TAG = "ModifyName";
    private static final String mServerUrl = "http://xxx.xxx.xxx.xxx:xxxx/modifyName/";

    private ModifyNameLoadingActivity.CompressFiles mCompressFiles;

    private String mUserInfo;
    private String mUserName;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_loading_name);

        Intent get_intent = getIntent();
        mUserInfo = (String)get_intent.getStringExtra("mUserInfo");
        mUserName = (String)get_intent.getStringExtra("mUserName");

        // compress and transfer
        mCompressFiles = new ModifyNameLoadingActivity.CompressFiles();
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void modify_name_result(){
        if(result.equals("OK")){
//            Toast.makeText(getApplicationContext(), "이름수정완료", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
//            Toast.makeText(getApplicationContext(), "오류: 이름수정실패", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // AsyncTask의 파라미터 3개는 각각 onPreExecute,onProgressUpdate,onPostExecute
    private class CompressFiles extends AsyncTask<Void, Integer, Boolean> {

        ProgressDialog asyncDialog;
//        File file;
        @Override
        protected void onPreExecute() {
            asyncDialog = new ProgressDialog(ModifyNameLoadingActivity.this);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            asyncDialog.setMessage("파일 준비 중...");
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

//            Log.d(TAG, "fileName : "+file.toString());

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
                jsonObject.put("changedName", mUserName);

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

//            file.deleteOnExit(); // delete zip file

            modify_name_result();
            return success[0];
        }

        public void publish(int progressRate, int taskFlag) {
            publishProgress(progressRate, 100, taskFlag);
        }

        protected void onProgressUpdate(Integer... progress) {
            try {
                Log.d(TAG, "progress[0] : "+progress[0]);
                Log.d(TAG, "progress[1] : "+progress[1]);
                Log.d(TAG, "progress[2] : "+progress[2]);

                asyncDialog.setProgress(progress[0]);
                asyncDialog.setMax(progress[1]);
                String msg = (progress[2] == 0) ? "파일 준비 중" :
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
}
