package com.example.mohamed.whatsapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mohamed.whatsapp.R;
import com.example.mohamed.whatsapp.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFragment extends Fragment {

    RecyclerView contact_list;

    DatabaseReference contactRef,usersRef;
    FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_contact, container, false);

        contact_list = view.findViewById(R.id.contact_list);
        contact_list.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts,ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIds = getRef(position).getKey();
                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            // copied from chatFragment
                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("Online")){
                                    holder.user_online.setVisibility(View.VISIBLE);
                                }else if (state.equals("Offline")){
                                    holder.user_online.setVisibility(View.INVISIBLE);
                                }

                            }else {
                                holder.user_online.setVisibility(View.INVISIBLE);
                            }


                            if (dataSnapshot.hasChild("image")){
                                String profileImage = dataSnapshot.child("image").getValue().toString();
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();
                                holder.user_profile_name.setText(userName);
                                holder.user_status.setText(userStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.users_profile_image);
                            }else {
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();
                                holder.user_profile_name.setText(userName);
                                holder.user_status.setText(userStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                return contactsViewHolder;
            }
        };
        contact_list.setAdapter(adapter);
        adapter.startListening();

    }
    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView user_profile_name,user_status;
        CircleImageView users_profile_image;
        ImageView user_online;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            users_profile_image = itemView.findViewById(R.id.users_profile_image);
            user_profile_name = itemView.findViewById(R.id.user_profile_name);
            user_status = itemView.findViewById(R.id.user_status);
            user_online = itemView.findViewById(R.id.user_online);
        }
    }
}
