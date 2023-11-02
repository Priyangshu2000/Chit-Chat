package com.example.wechat;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.Adapters.PersonalChatAdapter;
import com.example.wechat.FirebaseModelClass.chats;
import com.example.wechat.databinding.ActivityPersonalChatBinding;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class personalChat extends AppCompatActivity {

    ActivityPersonalChatBinding personalChatBinding;
    final private int mInterval = 1000*60;
    String ReceiverName, ReceiverPhone,profilePic;
    PersonalChatAdapter adapter;

    String SenderPhone;
    ArrayList<chats>allChats;

    SharedPreferences sharedPreferences;

    Handler mHandler = new Handler();
    EmojiPopup popup;

    DatabaseReference ref;


   private ValueEventListener valueEventListener;
    DatabaseReference seenRef;

    Uri imageuri;
    ActivityResultLauncher<String> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        personalChatBinding=ActivityPersonalChatBinding.inflate(getLayoutInflater());
        setContentView(personalChatBinding.getRoot());

        getWindow().setStatusBarColor(Color.parseColor("#edefe3"));
        getWindow().setNavigationBarColor(Color.parseColor("#edefe3"));

        sharedPreferences=getSharedPreferences("LastSeen",MODE_PRIVATE);
        allChats=new ArrayList<>();


//        popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root)).build(personalChatBinding.emojiButton);

        popup = new EmojiPopup(personalChatBinding.root,personalChatBinding.messageBox);

        loadData();
        setData();
        onClickListener();

        seenRef=FirebaseDatabase.getInstance().getReference("Chats").child(ReceiverPhone+SenderPhone);
        valueEventListener=new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                personalChatBinding.progressBar.setVisibility(View.VISIBLE);

                allChats.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){

                    chats c=dataSnapshot.getValue(chats.class);
                    String val=dataSnapshot.getKey();
                    if(c.getMsgSender().equals(ReceiverPhone))
                        seenRef.child(val).child("hasSeen").setValue("Seen");
                    allChats.add(c);

                }
                personalChatBinding.progressBar.setVisibility(View.GONE);
                Collections.reverse(allChats);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        loadChats();
        addToRecentChats();


    }

    private void addToRecentChats() {

        DatabaseReference msg=FirebaseDatabase.getInstance().getReference("Messages");

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats").child(SenderPhone+ReceiverPhone);

        Log.d("today",ref.toString());
       reference.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                   chats ch=snapshot.getValue(chats.class);
                   if(!SenderPhone.equals(ch.getMsgReceiver())){
                       msg.child(SenderPhone).child(ch.getMsgReceiver()).setValue(ch);}
                   if(!ReceiverPhone.equals(ch.getMsgSender())){
                       msg.child(ReceiverPhone).child(ch.getMsgSender()).setValue(ch);
                   }

           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot snapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });


    }



    private EmojiTextView getEmojiTextView() {
        EmojiTextView tvEmoji = (EmojiTextView) LayoutInflater
                .from(getApplicationContext())
                .inflate(R.layout.emoji_text_view,personalChatBinding.root,false);
        tvEmoji.setText(personalChatBinding.messageBox.getText().toString());
        return tvEmoji;
    }

    private void onClickListener() {

        personalChatBinding.emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.toggle();
            }
        });

        personalChatBinding.messageBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        personalChatBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(personalChatBinding.messageBox.getText().toString().length()==0){
                    Toast.makeText(personalChat.this, "Please type some message", Toast.LENGTH_SHORT).show();
                    return;
                }

                //collect Message
                chats chat=getChats();

                personalChatBinding.messageBox.setText("");

                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Chats");
                ref.child(SenderPhone+ReceiverPhone).child(chat.getMsgId()).setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        if(!SenderPhone.equals(ReceiverPhone)){
                            ref.child(ReceiverPhone+SenderPhone).child(chat.getMsgId()).setValue(chat);

                        }

                    }
                });

            }
        });

        personalChatBinding.attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
                activityResultLauncher.launch("image/*");
                personalChatBinding.messageBox.setText(imageuri.toString());
            }
        });

    }

    private void pickImage(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.GetContent(),new ActivityResultCallback<Uri>(){
            @Override
            public void onActivityResult(Uri result) {
                imageuri=result;
            }
        });
    }




    private chats getChats() {
        String msgValue=personalChatBinding.messageBox.getText().toString();

//        convertToUnicode();

        String currTime=String.valueOf(System.currentTimeMillis());
        String msgId = String.valueOf(System.currentTimeMillis());
        String msgReceiver=ReceiverPhone;
        String msgSender=SenderPhone;
        String msgType="text";
        chats chat=new chats(msgId,msgReceiver,msgSender,currTime,msgType,msgValue,"Delivered");
        return chat;
    }

    private void convertToUnicode() {
       String ans= "\\u" + Integer.toHexString('÷' | 0x10000).substring(1);
    }

    private void loadChats() {

        LinearLayoutManager manager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        adapter=new PersonalChatAdapter(allChats,this, SenderPhone);
        personalChatBinding.recyclerView.setAdapter(adapter);
        personalChatBinding.recyclerView.setLayoutManager(manager);



        Log.d("testing",ReceiverPhone+SenderPhone);

        ref= FirebaseDatabase.getInstance().getReference("Chats").child(SenderPhone+ReceiverPhone);


        ref.addValueEventListener(valueEventListener);

    }

    private void setData() {
        personalChatBinding.name.setText(ReceiverName);
    }

    private void loadData() {
        Intent intent=getIntent();
        ReceiverPhone =intent.getStringExtra("contactNumber");
        SharedPreferences sp=getSharedPreferences("ContactDetails",MODE_PRIVATE);

        SharedPreferences ssp=getSharedPreferences("CurrentUser",MODE_PRIVATE);
        SenderPhone=ssp.getString("phone","");

        ReceiverName =sp.getString(ReceiverPhone, ReceiverPhone);
        getStatus();

    }

    private void getStatus() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(ReceiverPhone);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long time = Long.valueOf(snapshot.child("lastSeen").getValue(String.class));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-dd-MM");
                LocalDate now = LocalDate.now();
                String today=dtf.format(now);

                LocalDateTime dateTime=LocalDateTime.of(now,LocalTime.parse("00:00:00"));

                long todayTime=dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();

//                try {
//                    todayTime=new java.text.SimpleDateFormat("MM/dd/YYYY HH:mm:ss").parse(today+ " 23:59:00").getTime();
//
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }

                Log.d("Today",String.valueOf(todayTime));
//                Toast.makeText(context, String.valueOf(time), Toast.LENGTH_SHORT).show();
                if(-time+System.currentTimeMillis()<=60000){
                    personalChatBinding.status.setText("online");
                }else{

                    String pattern="hh:mm a";
                    if(time-todayTime*1000<0){
                        pattern="dd/MM/YYYY hh:mm a";
                    }


                    String lastSeen = new java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(new java.util.Date(Long.valueOf(time)));

                    personalChatBinding.status.setText("Last Seen : "+lastSeen);



                }

                String pic = snapshot.child("profilePic").getValue(String.class);
                profilePic=pic;
                Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(personalChatBinding.profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                getStatus();
            } finally {
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
    protected void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ref.removeEventListener(valueEventListener);
        stopRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}