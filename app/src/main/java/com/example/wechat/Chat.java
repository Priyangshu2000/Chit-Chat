package com.example.wechat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
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
import com.example.wechat.ContactDetails.contact;
import com.example.wechat.FirebaseModelClass.chats;
import com.example.wechat.FirebaseModelClass.message;
import com.example.wechat.FirebaseModelClass.user;
import com.example.wechat.databinding.ActivityChatBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    private int mInterval = 500 * 60; // 1 minute default
    private Handler mHandler;
    ActivityChatBinding chatActivity;
    ChatRecycler adapter;


    DatabaseReference ref;
    SharedPreferences lastSeen;
    SharedPreferences.Editor lastSeenEditor;
    String number;
    private String TAG = "today";
    final private int REQ_CODE = 205;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatActivity = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(chatActivity.getRoot());


        number = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);

        getProfilePic();
        getFcmToken();

        ref = FirebaseDatabase.getInstance().getReference("Users").child(number).child("lastSeen");

        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        askPermission();

        myEdit.putString("phone", number);
        myEdit.apply();

        lastSeen = getSharedPreferences("LastSeen", MODE_PRIVATE);
        lastSeenEditor = lastSeen.edit();


        mHandler = new Handler();
        startRepeatingTask();


        onClickListener();


    }

    private void getProfilePic() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(number);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pic = snapshot.child("profilePic").getValue(String.class);
                Picasso.get().load(pic).into(chatActivity.profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void askPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_CODE);
        } else {
            loadChats();
        }
    }

    private void onClickListener() {
        chatActivity.hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Chat.this, Contacts.class);
                startActivity(intent);
            }
        });


        RecyclerView.OnItemTouchListener listener = new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (chatActivity.progressBar.getVisibility() == View.GONE) return false;
                return true;
            }
        };
        chatActivity.chatRecyclerView.addOnItemTouchListener(listener);

        chatActivity.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(Chat.this,UserProfile.class);
                startActivity(intent);
//                finish();


            }
        });

    }

    public void loadChats() {
        String currUserPhNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
        currUserPhNo = currUserPhNo.substring(1);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages").child(currUserPhNo);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatActivity.chatRecyclerView.setLayoutManager(linearLayoutManager);


        FirebaseRecyclerOptions<chats> options
                = new FirebaseRecyclerOptions.Builder<chats>()
                .setQuery(reference.orderByChild("msgTime"), chats.class)
                .build();


        adapter = new ChatRecycler(options, number, chatActivity);
        chatActivity.chatRecyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getIntent().getStringExtra("activity")!=null&&!getIntent().getStringExtra("activity").equals("otp_activity")){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRepeatingTask();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                ref.setValue(String.valueOf(System.currentTimeMillis()));
                lastSeenEditor.putString("last seen", String.valueOf(System.currentTimeMillis()));


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
        startRepeatingTask();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        chatActivity.progressBar.setVisibility(View.GONE);


    }

    private boolean checkPermission() {

        int res = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE) {

            if (grantResults.length > 0) {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhoneContacts();
                    loadChats();
                } else {
                    Toast.makeText(this, "Permissions are needed for the app to work", Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }

        }

    }


    private void getPhoneContacts() {

        chatActivity.progressBar.setVisibility(View.VISIBLE);

        List<contact> list = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
        if (cursor == null) return;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactNumber = contactNumber.replaceAll("\\s+", "");

                if (contactNumber.charAt(0) == '+') {
                    contactNumber = contactNumber.substring(1);
                } else if (contactNumber.length() == 10) {
                    contactNumber = "91" + contactNumber;
                } else if (contactNumber.charAt(0) == '0') {
                    contactNumber = "91" + contactNumber.substring(1);
                }


                String curUserPhnNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);

                if (contactNumber.equals(curUserPhnNumber)) {
                    contactName = "You";
                }
                contact contact = new contact();
                contact.setPhone(contactNumber);
                contact.setName(contactName);


                list.add(contact);

            }
        }
        saveToSharedPreference(list);
    }

    private void saveToSharedPreference(List<contact> list) {
        SharedPreferences sharedPreferences = getSharedPreferences("ContactDetails", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        for (contact contact : list) {
            myEdit.putString(contact.getPhone(), contact.getName());
        }
        myEdit.apply();

        list.clear();

        chatActivity.progressBar.setVisibility(View.GONE);

    }


    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (task.isSuccessful()) {
                    String token = task.getResult();
                    FirebaseDatabase.getInstance().getReference("Users").child(number).child("fcmToken").setValue(token);
                }
            }
        });

    }


}