package com.example.mohamed.whatsapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.whatsapp.R;
import com.example.mohamed.whatsapp.adapter.MessageAdapter;
import com.example.mohamed.whatsapp.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView profile_image;
    TextView userName,lastSeen;
    EditText input_message;
    ImageButton send_image_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference rotRef;

    final List<Messages>messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView userMessageList;

    String messageRecieverId,messageRecieverName,messageRecieverImage,messageSenderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rotRef = FirebaseDatabase.getInstance().getReference();

        messageRecieverId = getIntent().getExtras().get("user_id").toString();
        messageRecieverName = getIntent().getExtras().get("user_name").toString();
        messageRecieverImage = getIntent().getExtras().get("user_image").toString();

        toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionView);

        userName = findViewById(R.id.custom_user_name);
        lastSeen = findViewById(R.id.custom_user_lastSeen);
        profile_image = findViewById(R.id.custom_profile_image);
        input_message = findViewById(R.id.input_message);
        send_image_btn = findViewById(R.id.send_image_brn);
        send_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        messageAdapter = new MessageAdapter(messagesList);
        userMessageList = findViewById(R.id.chat_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        userMessageList.setHasFixedSize(true);
        userMessageList.setAdapter(messageAdapter);
        userMessageList.setLayoutManager(linearLayoutManager);


        userName.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(profile_image);

    }

    @Override
    protected void onStart() {
        super.onStart();

        rotRef.child("Messages").child(messageSenderId).child(messageRecieverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void displayLastSeen(){
        rotRef.child("Users").child(messageSenderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("Online")){
                                lastSeen.setText("Online");
                            }else if (state.equals("Offline")){
                                lastSeen.setText("Last seen: "+date+" "+time);
                            }

                        }else {
                            lastSeen.setText("Offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = input_message.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "first write your message", Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef= "Messages/" + messageSenderId + "/" + messageRecieverId;
            String messageRecieverRef= "Messages/" + messageRecieverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rotRef
                    .child("Messages")
                    .child(messageSenderId)
                    .child(messageRecieverId)
                    .push();
            String messagePushId = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextBody);

            rotRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){

                    }else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    input_message.setText("");
                }
            });
        }
    }
}
