package com.example.wechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.wechat.Adapters.mediaAdapter;
import com.example.wechat.databinding.ActivityFriendProfileBinding;

public class FriendProfile extends AppCompatActivity {

    ActivityFriendProfileBinding friendProfile;
    mediaAdapter adapter;

    String ReceiverName ,ReceiverPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendProfile = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        setContentView(friendProfile.getRoot());

        loadData();
        setData();

        loadMedia();

        onClickListener();

    }

    private void onClickListener() {
        friendProfile.msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        friendProfile.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setData() {
        friendProfile.userName.setText(ReceiverName);
        friendProfile.phoneNumber.setText("+"+ReceiverPhone);
    }

    private void loadData() {
        Intent intent = getIntent();
        ReceiverName = intent.getStringExtra("name");
        ReceiverPhone = intent.getStringExtra("phone");
    }

    private void loadMedia() {
        adapter = new mediaAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        manager.setSmoothScrollbarEnabled(false);


        friendProfile.mediaRecycler.setLayoutManager(manager);
        friendProfile.mediaRecycler.setAdapter(adapter);

    }


}