package com.example.speaker_identification_system.Register;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.speaker_identification_system.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterNameActivity extends AppCompatActivity {

    ImageView check_empty;
    ImageView check_full;

    EditText IdEditText1;
    EditText IdEditText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_name);

        check_empty = (ImageView)findViewById(R.id.check_empty);
        check_full = (ImageView)findViewById(R.id.check_full);

        check_empty.setVisibility(View.VISIBLE);
        check_full.setVisibility(View.INVISIBLE);

        IdEditText1 = (EditText) findViewById(R.id.edit_idNum1);
        IdEditText2 = (EditText) findViewById(R.id.edit_idNum2);

        IdEditText1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
                if (s.length() - (count-after) > 6) {
                    IdEditText1.setText(s);
                }
            }
        });
        IdEditText2.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
                if (s.length() - (count-after) > 7) {
                    IdEditText2.setText(s);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void onClick_Agreement(View v){
        if(check_empty.getVisibility() == View.VISIBLE){
            check_empty.setVisibility(View.INVISIBLE);
            check_full.setVisibility(View.VISIBLE);
        }else{
            check_empty.setVisibility(View.VISIBLE);
            check_full.setVisibility(View.INVISIBLE);
        }
    }

    public void onClick_BtnPrevious(View v){
        finish();
    }

    public void onClick_BtnNext(View v){

        EditText nameEditText = (EditText) findViewById(R.id.edit_name);

        String name = nameEditText.getText().toString();
        String id1 = IdEditText1.getText().toString();
        String id2 = IdEditText2.getText().toString();

        if (name.matches("") ){
            Toast.makeText(getApplicationContext(),"이름을 입력하세요.",Toast.LENGTH_SHORT).show();
        } else if(id1.length() !=6 || id2.length() !=7){
            Toast.makeText(getApplicationContext(),"주민등록번호를 입력하세요.",Toast.LENGTH_SHORT).show();
        } else if(check_empty.getVisibility() == View.VISIBLE){
            Toast.makeText(getApplicationContext(),"회원정보 수집에 동의해주세요.",Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, RegisterAudioActivity.class);
            intent.putExtra("UserName",name);
            intent.putExtra("UserInfo",getMD5(id1+id2+"_"+name));
            startActivity(intent);
        }


    }

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
