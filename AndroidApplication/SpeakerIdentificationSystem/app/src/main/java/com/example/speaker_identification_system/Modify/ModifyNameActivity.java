package com.example.speaker_identification_system.Modify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.speaker_identification_system.R;

public class ModifyNameActivity extends AppCompatActivity {

    private static final String TAG = "ModifyName";
    private static final String mServerUrl = "http://xxx.xxx.xxx.xxx:xxxx/modifyName/";

    private String mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_name);

        Intent get_intent = getIntent();
        mUserInfo = (String)get_intent.getStringExtra("mUserInfo");
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void onClick_BtnModify(View v){
        EditText nameEdit = (EditText)findViewById(R.id.edit_name);
        String name = nameEdit.getText().toString();
        if (!name.matches("")) {
            Intent intent = new Intent(this, ModifyNameLoadingActivity.class);
            intent.putExtra("mUserName",name);
            intent.putExtra("mUserInfo",mUserInfo);
            startActivity(intent);
        }else {
            Toast.makeText(getApplicationContext(),"이름을 입력하세요.",Toast.LENGTH_SHORT).show();
        }
    }


}
