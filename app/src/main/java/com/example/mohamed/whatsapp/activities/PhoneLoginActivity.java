package com.example.mohamed.whatsapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohamed.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    Button send_code_button,Verify_button;
    EditText phone_number_input,vertification_code_input;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        Verify_button = findViewById(R.id.Verify_button);
        send_code_button = findViewById(R.id.send_code_button);
        vertification_code_input = findViewById(R.id.vertification_code_input);
        phone_number_input = findViewById(R.id.phone_number_input);

        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);

        send_code_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = phone_number_input.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "please add phone number first", Toast.LENGTH_SHORT).show();
                }else {

                    loadingBar.setTitle("vertification code");
                    loadingBar.setMessage("please wait while we authentacting your code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    // Authentication phone number
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);// OnVerificationStateChangedCallbacks

                    callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "invalid phone number , please enter phone number with your country code", Toast.LENGTH_SHORT).show();
                            send_code_button.setVisibility(View.VISIBLE);
                            phone_number_input.setVisibility(View.VISIBLE);
                            Verify_button.setVisibility(View.INVISIBLE);
                            vertification_code_input.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            // Save verification ID and resending token so we can use them later
                            mVerificationId = verificationId;
                            mResendToken = token;

                            loadingBar.dismiss();

                            Toast.makeText(PhoneLoginActivity.this, "code has been sent , please check and verify", Toast.LENGTH_SHORT).show();
                            send_code_button.setVisibility(View.INVISIBLE);
                            phone_number_input.setVisibility(View.INVISIBLE);
                            Verify_button.setVisibility(View.VISIBLE);
                            vertification_code_input.setVisibility(View.VISIBLE);
                        }
                    };
                }
            }
        });

        vertification_code_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_code_button.setVisibility(View.INVISIBLE);
                phone_number_input.setVisibility(View.INVISIBLE);
                String vertificationCode = vertification_code_input.getText().toString();
                if (TextUtils.isEmpty(vertificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "please enter vertification code", Toast.LENGTH_SHORT).show();
                }else {

                    loadingBar.setTitle("Code Vertification");
                    loadingBar.setMessage("please wait while we are verifing your vertification code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, vertificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "you are logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
