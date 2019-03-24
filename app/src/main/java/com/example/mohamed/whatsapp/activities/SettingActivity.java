package com.example.mohamed.whatsapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohamed.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    CircleImageView profile_image;
    EditText set_username;
    EditText set_status;
    Button update_settings;
    private ProgressDialog loadingBar;
    Toolbar toolbar;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rotRef;

    private StorageReference userProfileImageRef;

    private static final int galleryPick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profile_image = findViewById(R.id.profile_image);
        set_username = findViewById(R.id.set_username);
        set_status = findViewById(R.id.set_status);
        update_settings = findViewById(R.id.update_settings);
        update_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile images");

//        set_username.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rotRef = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(this);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);
            }
        });

        retrieveUserInfo();
    }

    private void updateSettings() {
        String userName = set_username.getText().toString();
        String status = set_status.getText().toString();
        if (TextUtils.isEmpty(userName)){
            Toast.makeText(this, "please add your name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "please add your status", Toast.LENGTH_SHORT).show();
        }else {
            HashMap<String,Object>profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",userName);
            profileMap.put("status",status);

            rotRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "profile updated successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // retrieve data of user(name,status,image) inside EditText
    private void retrieveUserInfo() {
        rotRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            set_username.setText(retrieveUserName);
                            set_status.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(profile_image);
                        }
                        else if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            set_username.setText(retrieveUserName);
                            set_status.setText(retrieveStatus);
                        }
                        else {
//                            set_username.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this, "please update user information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){

                loadingBar.setTitle("setting profile image");
                loadingBar.setMessage("please wait while profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resutlUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(currentUserId+" .jpg");
                filePath.putFile(resutlUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(SettingActivity.this, "image uploaded successfully", Toast.LENGTH_SHORT).show();
                            final String downloadUri = task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                            rotRef.child("Users").child(currentUserId).child("image")
                                    .setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(SettingActivity.this, "image saved in database successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }else {
                                                Toast.makeText(SettingActivity.this, "Error: "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }else {
                            Toast.makeText(SettingActivity.this, "Error"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });

            }
        }
    }


    private void sendUserToMainActivity() {
        Intent intent = new Intent(SettingActivity.this,MainActivity.class);
        // prevent user from back
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
