package com.example.wechat.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.Chat;
import com.example.wechat.FirebaseModelClass.user;
import com.example.wechat.databinding.ActivityOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class otp_activity extends AppCompatActivity {

    ActivityOtpBinding otpActivity;
    String phone,countryCode,fullNumber, OTPId;
    FirebaseAuth mAuth;


    private String DEFAULT_PROFILE_PIC="https://firebasestorage.googleapis.com/v0/b/we-chat-f2ff5.appspot.com/o/Basic_Ui_(186).jpg?alt=media&token=c5c09d68-8db5-4b7c-ad06-bec4f9f6f3a7&_gl=1*1uryhfc*_ga*MTExMDcxMTcwLjE2OTgyOTYxMTE.*_ga_CW55HF8NVT*MTY5ODM4NDIxMy41LjEuMTY5ODM4NjU2My40My4wLjA.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otpActivity=ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(otpActivity.getRoot());

        getDataFromPrevActivity();
        setData();
        onClickListener();
        recieveOTP();

    }

    private void recieveOTP() {
        mAuth= FirebaseAuth.getInstance();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+"+fullNumber)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks( new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                OTPId=verificationId;
                                Toast.makeText(otp_activity.this, "Code sent", Toast.LENGTH_SHORT).show();

                            }
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                Toast.makeText(otp_activity.this, "Verification Completed", Toast.LENGTH_SHORT).show();
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(otp_activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                        FirebaseUser user=task.getResult().getUser();
                        long creationTime=user.getMetadata().getCreationTimestamp();
                        long lastLoginTime=user.getMetadata().getLastSignInTimestamp();
                        if(creationTime==lastLoginTime)
                            sendDataToFirebase();
                        else{
                            startActivity(new Intent(otp_activity.this, Chat.class));
                            finish();
                        }


                }else{
                    Toast.makeText(otp_activity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendDataToFirebase() {

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference("Users").child(fullNumber);

        Toast.makeText(this, reference.toString(), Toast.LENGTH_LONG).show();

        user u=new user(phone,phone, DEFAULT_PROFILE_PIC,String.valueOf(System.currentTimeMillis()),false);

        reference.setValue(u);
        startActivity(new Intent(otp_activity.this, Chat.class));
        finish();

    }

    private void setData() {
        String text="Please enter the verification code send to +"+fullNumber.substring(0,4)+"xxxxxx"+fullNumber.charAt(10)+fullNumber.charAt(11);
        otpActivity.phoneNumberText.setText(text);
    }

    void onClickListener(){
        otpActivity.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String OTP=otpActivity.otpView.getOtp();
                if(OTP.isEmpty()){
                    Toast.makeText(otp_activity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }else{
                PhoneAuthCredential credential=PhoneAuthProvider.getCredential(OTPId,OTP);
                signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    void getDataFromPrevActivity(){
        Intent otpIntent=getIntent();
        phone=otpIntent.getStringExtra("phoneNumber");
        countryCode=otpIntent.getStringExtra("countryCode");
        fullNumber=countryCode+phone;
    }


}

/*FLOW
1.get the phone number from previous activity.
2.set the phone number on the text view
3.set on click listener on the submit button
4.do all the work related to OTP
    a.get FirebaseAuth instance.
    b.use PhoneAuthOptions function(set activity ,set phone number ,set call back methods)
    c.call verifyPhoneNumber functions on PhoneAuthOptions.
    d.In the onCLickListener of Submit button pass the OTP id and OTP code to the function signInWithCredentials.
 */

/*
LEFT

1.Sending user details to firebase.

 */