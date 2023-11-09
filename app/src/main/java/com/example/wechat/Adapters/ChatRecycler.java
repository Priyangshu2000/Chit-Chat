package com.example.wechat.Adapters;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.Chat;
import com.example.wechat.Constants;
import com.example.wechat.FirebaseModelClass.chats;
import com.example.wechat.FirebaseModelClass.message;
import com.example.wechat.FirebaseModelClass.user;
import com.example.wechat.R;
import com.example.wechat.databinding.ActivityChatBinding;
import com.example.wechat.databinding.SinglechatBinding;
import com.example.wechat.personalChat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecycler extends FirebaseRecyclerAdapter<chats, ChatRecycler.MyViewHolder> {
    Context context;
    static String CurrentUser;
    ActivityChatBinding chatBinding;

    ValueEventListener readStatus;

    public ChatRecycler(@NonNull FirebaseRecyclerOptions<chats> options, String number, ActivityChatBinding chatBinding) {
        super(options);
//        CurrentUser = number;
        CurrentUser = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);
        this.chatBinding = chatBinding;

    }


    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull chats model) {

        chatBinding.emptyText.setVisibility(View.GONE);

        holder.message.setVisibility(View.VISIBLE);
        String name = model.getMsgReceiver();

        if (CurrentUser.equals(model.getMsgReceiver())) {
            name = model.getMsgSender();
        }

//        Log.d("nothinggg", "Sender: " + model.getMsgSender());
//        Log.d("nothinggg", "Receiver:" + model.getMsgReceiver());
//        Log.d("nothinggg", "Current User:" + CurrentUser);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(model.getMsgSender() + model.getMsgReceiver()).child(model.getMsgId());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats chats = snapshot.getValue(chats.class);
                holder.status.setText(chats.getHasSeen());
                if (chats.getHasSeen().equals("Seen"))
                    holder.status.setTextColor(Color.parseColor("#6A706D"));
                else {
                    holder.status.setTextColor(Color.parseColor("#94A89F"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (!model.getMsgSender().equals(CurrentUser)) {

            holder.status.setVisibility(View.GONE);

            if (!model.getHasSeen().equals("Seen")) {
                holder.notification.setVisibility(View.VISIBLE);
                holder.message.setTextColor(Color.parseColor("#292a60"));
            } else {
                holder.notification.setVisibility(View.INVISIBLE);
                holder.message.setTextColor(Color.parseColor("#94A89F"));
            }
        } else {
            holder.notification.setVisibility(View.INVISIBLE);
            holder.status.setVisibility(View.VISIBLE);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(name);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pic = snapshot.child("profilePic").getValue(String.class);
                Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(holder.profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        String contactName;
        SharedPreferences sharedPreferences = context.getSharedPreferences("ContactDetails", Context.MODE_PRIVATE);


        contactName = sharedPreferences.getString(name, name);

        String pattern = "hh:mm a";

        if (Math.abs(System.currentTimeMillis() - Long.valueOf(model.getMsgTime())) > 86400000) {
            pattern = "dd/MM/yyyy";
        }

        String time = new java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(new java.util.Date(Long.valueOf(model.getMsgTime())));

//        Toast.makeText(context,time,Toast.LENGTH_SHORT).show();

        holder.userName.setText(contactName);
        if (model.getHasSeen().equals("false")) {
            holder.message.setTypeface(null, Typeface.BOLD);
        }
        holder.message.setText(model.getMsgValue());
        holder.time.setText(time);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = model.getMsgReceiver();

                if (CurrentUser.equals(model.getMsgReceiver())) {
                    name = model.getMsgSender();
                }
                Intent intent = new Intent(context, personalChat.class);


                intent.putExtra("contactNumber", name);
                context.startActivity(intent);
            }
        };

        holder.layout.setOnClickListener(listener);
        holder.message.setOnClickListener(listener);


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlechat, parent, false);
        return new ChatRecycler.MyViewHolder(view);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView userName;
        EmojiEditText message;
        TextView time;
        ConstraintLayout layout;

        TextView notification, status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            userName = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            layout = itemView.findViewById(R.id.layout);
            notification = itemView.findViewById(R.id.notif);
            status = itemView.findViewById(R.id.status);
        }
    }

    @Override
    public void onDataChanged() {

        super.onDataChanged();
        chatBinding.progressBar.setVisibility(View.GONE);

    }
}

