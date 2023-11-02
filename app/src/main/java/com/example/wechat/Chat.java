package com.example.wechat;

import static android.app.ProgressDialog.show;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.wechat.Adapters.ChatRecycler;
import com.example.wechat.FirebaseModelClass.chats;
import com.example.wechat.FirebaseModelClass.message;
import com.example.wechat.FirebaseModelClass.user;
import com.example.wechat.databinding.ActivityChatBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    private int mInterval = 500*60; // 1 minute default
    private Handler mHandler;
    ActivityChatBinding chatActivity;
    ChatRecycler adapter;



    DatabaseReference ref;
    SharedPreferences lastSeen;
    SharedPreferences.Editor lastSeenEditor;
    String number;
    private String TAG="today";
    final private int REQ_CODE=205;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatActivity=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(chatActivity.getRoot());
        number=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);

        getProfilePic();

        ref = FirebaseDatabase.getInstance().getReference("Users").child(number).child("lastSeen");

        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        askPermission();

        myEdit.putString("phone",number);
        myEdit.apply();

        lastSeen=getSharedPreferences("LastSeen",MODE_PRIVATE);
        lastSeenEditor=lastSeen.edit();


        mHandler = new Handler();
        startRepeatingTask();


        loadChats();
        onClickListener();

    }

    private void getProfilePic() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(number);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               String pic= snapshot.child("profilePic").getValue(String.class);
                Picasso.get().load(pic).into(chatActivity.profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void askPermission() {
        if(!checkPermission()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},REQ_CODE);
        }
    }

    private void onClickListener() {
        chatActivity.hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Chat.this,Contacts.class);
                startActivity(intent);
            }
        });


        RecyclerView.OnItemTouchListener listener= new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if(chatActivity.progressBar.getVisibility()==View.GONE)return false;
                return true;
            }
        };
        chatActivity.chatRecyclerView.addOnItemTouchListener(listener);


    }

    public void loadChats() {
        String currUserPhNo=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
        currUserPhNo=currUserPhNo.substring(1);

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Messages").child(currUserPhNo);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatActivity.chatRecyclerView.setLayoutManager(linearLayoutManager);



        FirebaseRecyclerOptions<chats> options
                = new FirebaseRecyclerOptions.Builder<chats>()
                .setQuery(reference.orderByChild("msgTime"), chats.class)
                .build();

        adapter = new ChatRecycler(options,number,chatActivity);
        chatActivity.chatRecyclerView.setAdapter(adapter);





    }

    @Override
    public void onStart()
    {
        super.onStart();
        adapter.startListening();

    }
    @Override public void onStop()
    {   super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ref.setValue(false);
        stopRepeatingTask();
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                ref.setValue(String.valueOf(System.currentTimeMillis()));
                lastSeenEditor.putString("last seen",String.valueOf(System.currentTimeMillis()));


            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        chatActivity.progressBar.setVisibility(View.GONE);
    }

    private boolean checkPermission() {

        int res=ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return res==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQ_CODE){

            if(grantResults.length>0){

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //permission granted
                }
                else{
                    Toast.makeText(this, "Permissions are needed for the app to work", Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }

        }

    }



}