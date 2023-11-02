package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.Adapters.ChatRecycler;
import com.example.wechat.Adapters.ContactAdapter;
import com.example.wechat.ContactDetails.contact;
import com.example.wechat.databinding.ActivityContactsBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Contacts extends AppCompatActivity {

    ArrayList<contact>list;
    Set<String> weChatUsers;

    ContactAdapter adapter;
    ActivityContactsBinding contactsActivity;

    public static Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactsActivity=ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(contactsActivity.getRoot());


        list=new ArrayList<>();
        weChatUsers=new HashSet<>();
        FetchContacts();
        onClickListener();


    }

    private void onClickListener() {
        RecyclerView.OnItemTouchListener listener= new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if(contactsActivity.progressBar.getVisibility()== View.GONE)return false;
                return true;
            }
        };
        contactsActivity.chatRecyclerView.addOnItemTouchListener(listener);
    }

    private void saveToSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("ContactDetails", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        for(contact contact:list){
//            Toast.makeText(this, contact.getPhone(), Toast.LENGTH_SHORT).show();
            myEdit.putString(contact.getPhone(),contact.getName());}
        myEdit.apply();
    }

    private void loadContacts() {
        adapter=new ContactAdapter(list);
        contactsActivity.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsActivity.chatRecyclerView.setAdapter(adapter);
        contactsActivity.progressBar.setVisibility(View.GONE);
    }


    private void getPhoneContacts(){
        ContentResolver contentResolver=getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor=contentResolver.query(uri,null,null,null,ContactsContract.Contacts.SORT_KEY_PRIMARY+" ASC");
        if(cursor==null)return;
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                String contactName=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String contactNumber=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactNumber=contactNumber.replaceAll("\\s+","");

                if(contactNumber.charAt(0)=='+'){
                    contactNumber=contactNumber.substring(1);
                }else if(contactNumber.length()==10){
                    contactNumber="91"+contactNumber;
                }else if(contactNumber.charAt(0)=='0'){
                    contactNumber="91"+contactNumber.substring(1);
                }



//                Log.d("statuss",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1)+"here");

                String curUserPhnNumber=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);

                if(contactNumber.equals(curUserPhnNumber)){
                    contactName="You";
                }
                contact contact=new contact();
                contact.setPhone(contactNumber);
                contact.setName(contactName);

                if(weChatUsers.contains(contactNumber)){

                    list.add(contact);
                    weChatUsers.remove(contactNumber);
                }
            }
        }

        sort(list);

        loadContacts();
        saveToSharedPreference();
    }
    public static void sort(ArrayList<contact> list) {

        list.sort((o1, o2)
                -> o1.getName().compareTo(
                o2.getName()));
    }

    private void FetchContacts(){
        contactsActivity.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    weChatUsers.add(dataSnapshot.getKey());
                }

                getPhoneContacts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}