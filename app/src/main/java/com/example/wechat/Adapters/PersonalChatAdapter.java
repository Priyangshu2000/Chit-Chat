package com.example.wechat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.FirebaseModelClass.chats;
import com.example.wechat.FirebaseModelClass.message;
import com.example.wechat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.Emoji;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.Locale;

public class PersonalChatAdapter extends RecyclerView.Adapter {

    ArrayList<chats> messages;
    Context context;
    final int SENDER_VIEW_TYPE=1;
    final int RECEIVER_VIEW_TYPE=0;
    String currentUser;

    public PersonalChatAdapter(ArrayList<chats> messages, Context context, String currentUser) {
        this.messages = messages;
        this.context = context;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==SENDER_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.sender_container,parent,false);
            return new SenderViewHolder(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.recieve_container,parent,false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        chats model=messages.get(position);


       String pattern="hh:mm a";
       if(Math.abs(System.currentTimeMillis()-Long.valueOf(model.getMsgTime()))>86400000){
           pattern="dd/MM/YYYY hh:mm a";
       }
       String time = new java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(new java.util.Date(Long.valueOf(model.getMsgTime())));




        if (holder.getClass() == SenderViewHolder.class) {
            if (model.getMsgType().equals("text")) {

                ((SenderViewHolder) holder).senderMsg.setText(model.getMsgValue());
                ((SenderViewHolder) holder).timestamp.setText(time);

                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chats").child(model.getMsgSender()+model.getMsgReceiver()).child(model.getMsgId()).child("hasSeen");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ((SenderViewHolder) holder).delivered.setText(snapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                //load image
            }
        } else {
            ((RecieverViewHolder) holder).receiverMsg.setText(model.getMsgValue());
            ((RecieverViewHolder) holder).timestamp.setText(time);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if(messages.get(position).getMsgSender().equals(currentUser)){
                return SENDER_VIEW_TYPE;
        }else
            return RECEIVER_VIEW_TYPE;

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class RecieverViewHolder extends RecyclerView.ViewHolder{
        TextView timestamp,date;
        EmojiEditText receiverMsg;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg=itemView.findViewById(R.id.receive_textmesssage);
            timestamp=itemView.findViewById(R.id.receive_time);
//            date=itemView.findViewById(R.id.time);
        }
    }
    public static class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView timestamp,delivered;
        EmojiEditText senderMsg;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.send_textmesssage);
            timestamp=itemView.findViewById(R.id.send_time);
            delivered=itemView.findViewById(R.id.delivered);
        }
    }

}
