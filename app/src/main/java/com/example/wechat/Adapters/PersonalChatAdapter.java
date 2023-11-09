package com.example.wechat.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PersonalChatAdapter extends RecyclerView.Adapter {

    ArrayList<chats> messages;
    Context context;
    final int SENDER_VIEW_TYPE=1;
    final int RECEIVER_VIEW_TYPE=0;
    String currentUser;
    static long previousTs=0;

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

//        Toast.makeText(context, model.getMsgValue()+" at position : "+position, Toast.LENGTH_SHORT).show();



       String pattern="hh:mm a";
//       if(Math.abs(System.currentTimeMillis()-Long.valueOf(model.getMsgTime()))>86400000){
//           pattern="dd/MM/YYYY hh:mm a";
//       }
       String time = new java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(new java.util.Date(Long.valueOf(model.getMsgTime())));




        if (holder.getClass() == SenderViewHolder.class) {
            if (model.getMsgType().equals("text")) {

                ((SenderViewHolder) holder).senderMsg.setText(model.getMsgValue());
                ((SenderViewHolder) holder).timestamp.setText(time);



                if(position< messages.size()-1){
                    chats pm = messages.get(position+1);
                    previousTs = Long.valueOf(pm.getMsgTime());
                }else{
                    previousTs=0;
                }
                try {
                    setTimeTextVisibility(Long.valueOf(model.getMsgTime()), previousTs, ((SenderViewHolder) holder).date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }


                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chats").child(model.getMsgSender()+model.getMsgReceiver()).child(model.getMsgId()).child("hasSeen");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.getValue(String.class);
                        ((SenderViewHolder) holder).delivered.setText(status);

                        if(status.equals("Seen")){
                            ((SenderViewHolder) holder).delivered.setTextColor(Color.BLACK);
                        }else{
                            ((SenderViewHolder) holder).delivered.setTextColor(Color.parseColor("#6d9bbc"));
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                //load image
            }
        } else {
            if(position< messages.size()-1){
                chats pm = messages.get(position+1);
                previousTs = Long.valueOf(pm.getMsgTime());
            }else{
                previousTs=0;
            }
            try {
                setTimeTextVisibility(Long.valueOf(model.getMsgTime()), previousTs, ((RecieverViewHolder) holder).date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
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

    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText) throws ParseException {

        String date1 = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date(ts1));
        String date2 = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date(ts2));

        SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        Date d1 = sdFormat.parse(date1);
        Date d2 = sdFormat.parse(date2);

        if(ts2==0){
            timeText.setVisibility(View.VISIBLE);
            timeText.setText(date1);
        }else {

            boolean same=true;

            assert d1 != null;
            if(d1.compareTo(d2)!=0)same=false;

            if(same){
                timeText.setVisibility(View.GONE);
                timeText.setText("");
            }else {
                timeText.setVisibility(View.VISIBLE);
                timeText.setText(date1);
            }

        }
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
            date=itemView.findViewById(R.id.time);
        }


    }


    public static class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView timestamp,delivered,date;
        EmojiEditText senderMsg;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.send_textmesssage);
            timestamp=itemView.findViewById(R.id.send_time);
            delivered=itemView.findViewById(R.id.delivered);
            date=itemView.findViewById(R.id.time);
        }
    }

}
