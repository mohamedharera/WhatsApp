package com.example.mohamed.whatsapp.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mohamed.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String recieverId,currentState,senderUserId;
    CircleImageView visit_profile_image;
    TextView visit_user_name,visit_user_status;
    Button sendMessage_btn,decline_message_btn;

    DatabaseReference userRef,chatRequestRef,contactRef,notificationRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth = FirebaseAuth.getInstance();

        senderUserId = mAuth.getCurrentUser().getUid();
        recieverId = getIntent().getExtras().get("visit_id").toString();

        visit_profile_image = findViewById(R.id.visit_profile_image);
        visit_user_name = findViewById(R.id.user_name);
        visit_user_status = findViewById(R.id.visit_profile_status);
        sendMessage_btn = findViewById(R.id.sendMessage_btn);
        decline_message_btn = findViewById(R.id.decline_message_btn);

        currentState = "new";
        retrieveUserInfo();
    }


    private void manageChatRequest() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(recieverId)){
                            String request_type = dataSnapshot.child(recieverId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                currentState ="request_sent";
                                sendMessage_btn.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("recieved"))
                            {
                                currentState = "request_recieved";
                                sendMessage_btn.setText("Accept Chat Request");
                                decline_message_btn.setVisibility(View.VISIBLE);
                                decline_message_btn.setEnabled(true);
                                decline_message_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                            {
                            contactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(recieverId)){
                                                currentState="friends";
                                                sendMessage_btn.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(recieverId)){

            sendMessage_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage_btn.setEnabled(false);
                    if (currentState.equals("new")){
                        sendChatRequest();
                    }
                    if (currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if(currentState.equals("request_recieved")){
                        acceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });


        }else {
            contactRef.child(senderUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(recieverId)){
                                currentState="friends";
                                sendMessage_btn.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        userRef.child(recieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(visit_profile_image);
                    visit_user_name.setText(userName);
                    visit_user_status.setText(userStatus);

                    manageChatRequest();

                }else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    visit_user_name.setText(userName);
                    visit_user_status.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeSpecificContact() {
        contactRef.child(senderUserId).child(recieverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(recieverId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessage_btn.setEnabled(true);
                                                currentState="new";
                                                sendMessage_btn.setText("Send message");

                                                decline_message_btn.setVisibility(View.INVISIBLE);
                                                decline_message_btn.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });
    }

    private void acceptChatRequest() {
        contactRef.child(senderUserId).child(recieverId)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(recieverId).child(senderUserId)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                chatRequestRef.child(senderUserId).child(recieverId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    chatRequestRef.child(recieverId).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendMessage_btn.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    sendMessage_btn.setText("Remove this Contact");
                                                                                    decline_message_btn.setVisibility(View.INVISIBLE);
                                                                                    decline_message_btn.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(recieverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(recieverId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessage_btn.setEnabled(true);
                                                currentState="new";
                                                sendMessage_btn.setText("Send message");

                                                decline_message_btn.setVisibility(View.INVISIBLE);
                                                decline_message_btn.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(recieverId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(recieverId).child(senderUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                // push notifications
                                                HashMap<String,String>chatNotification = new HashMap<>();
                                                chatNotification.put("from",senderUserId);
                                                chatNotification.put("type","request");
                                                notificationRef.child(recieverId).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    sendMessage_btn.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    sendMessage_btn.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
