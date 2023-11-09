package com.example.wechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.Login.otp_activity;
import com.example.wechat.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainActivity;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainActivity.getRoot());



        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this, Chat.class));
            finish();
        }

//       KeyHelper.get(this,"SHA1");

        onClickListener();
    }

    void onClickListener(){
        mainActivity.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getPhone number and pass to otpActivity
                String phone=mainActivity.phoneNumber.getText().toString();
                String countryCode=mainActivity.countryCode.getSelectedCountryCode();


                if(isValidMobile(phone)){
                    Intent otpIntent=new Intent(MainActivity.this,otp_activity.class);
                    otpIntent.putExtra("phoneNumber",phone);
                    otpIntent.putExtra("countryCode",countryCode);
                    startActivity(otpIntent);
                    finish();

                }else{
                    Toast.makeText(MainActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mainActivity.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

    }

    //Phone number validator
    private boolean isValidMobile(String phone) {
        if(phone.isEmpty())return false;
        if(phone.length()!=10)return false;
        if(phone.charAt(0)==0)return false;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

}

/*
FLOW:
1.set onclick listener on Submit button
2.get the phone number from phone number text and country code picker
3.validate it
4.if correct send it to Otp Activity.
 */