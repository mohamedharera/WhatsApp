package com.example.mohamed.whatsapp.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mohamed.whatsapp.R;
import com.example.mohamed.whatsapp.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages>userMessageList;
    private DatabaseReference userRf;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages>userMessageList){
        this.userMessageList=userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();
        MessageViewHolder messageViewHold = new MessageViewHolder(view);
        return messageViewHold;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderUid=mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userRf = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userRf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String recieverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(recieverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.reciever_profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (fromMessageType.equals("text")){
            messageViewHolder.reciever_message.setVisibility(View.INVISIBLE);
            messageViewHolder.reciever_profile_image.setVisibility(View.INVISIBLE);
            messageViewHolder.sender_message.setVisibility(View.INVISIBLE);


            if (fromUserId.equals(messageSenderUid)){

                messageViewHolder.sender_message.setVisibility(View.VISIBLE);

                messageViewHolder.sender_message.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.sender_message.setTextColor(Color.BLACK);
                messageViewHolder.sender_message.setText(messages.getMessage());
            }else {
                messageViewHolder.reciever_message.setVisibility(View.VISIBLE);
                messageViewHolder.reciever_profile_image.setVisibility(View.VISIBLE);

                messageViewHolder.reciever_message.setBackgroundResource(R.drawable.reciever_messages_layout);
                messageViewHolder.reciever_message.setTextColor(Color.BLACK);
                messageViewHolder.reciever_message.setText(messages.getMessage());
            }
        }



    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView reciever_message,sender_message;
        CircleImageView reciever_profile_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            reciever_message = itemView.findViewById(R.id.reciever_message);
            sender_message = itemView.findViewById(R.id.sender_message);
            reciever_profile_image = itemView.findViewById(R.id.message_profile_image);

        }
    }
}
