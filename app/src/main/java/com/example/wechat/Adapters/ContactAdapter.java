package com.example.wechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ContactDetails.contact;
import com.example.wechat.R;
import com.example.wechat.personalChat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    ArrayList<contact> Contacts;
    Context context;

    public ContactAdapter(ArrayList<contact> contacts) {
        Contacts = contacts;
    }

    @NonNull
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlechat, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.MyViewHolder holder, int position) {
        contact model = Contacts.get(position);


        holder.status.setVisibility(View.GONE);

        Log.d("statuss", model.getName());


        holder.message.setText("Hii there I am using Chit Chat");
        holder.message.setVisibility(View.VISIBLE);

        holder.userName.setText(model.getName());
        holder.time.setVisibility(View.GONE);


        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, personalChat.class);
                intent.putExtra("contactNumber",model.getPhone());
                context.startActivity(intent);
            }
        };
        holder.message.setClickable(true);

        holder.relativeLayout.setOnClickListener(listener);
        holder.message.setOnClickListener(listener);

        setOnline(model,holder);

    }

    private void setOnline(contact model,MyViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(model.getPhone());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long time = Long.valueOf(snapshot.child("lastSeen").getValue(String.class));

                if (-time + System.currentTimeMillis() <= 60000) {
                    holder.notification.setVisibility(View.VISIBLE);
                } else {
                    holder.notification.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {

        return Contacts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView userName, time;
        EmojiEditText message;
        ConstraintLayout layout;
        RelativeLayout relativeLayout;

        LinearLayout nameLayout;
        TextView notification,status;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            userName = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            layout = itemView.findViewById(R.id.layout);
            relativeLayout=itemView.findViewById(R.id.chatRelLayout);
            nameLayout=itemView.findViewById(R.id.nameLayout);
            status=itemView.findViewById(R.id.status);
            notification=itemView.findViewById(R.id.notif);

        }
    }


}
