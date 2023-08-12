package com.abhi_saxena.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abhi_saxena.otpverification.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        final String[] OTP = new String[1];
        setContentView(binding.getRoot());

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String backEndOTP, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnGetOTP.setVisibility(View.VISIBLE);
                OTP[0] = backEndOTP;
                Intent intent = new Intent(getApplicationContext(), VerifyOTP.class);
                intent.putExtra("mobile", binding.etMobile.getText().toString());
                intent.putExtra("backEndOTP", backEndOTP);
                startActivity(intent);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnGetOTP.setVisibility(View.VISIBLE);
                Log.d("Phone Authentication", "onVerificationCompleted:" + phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnGetOTP.setVisibility(View.VISIBLE);
                Log.w("Phone Authentication", "onVerificationFailed: " + e.getMessage());
            }
        };

        binding.llMobileNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etMobile.requestFocus();
            }
        });

        binding.btnGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.btnGetOTP.setVisibility(View.GONE);
                Log.e("Check", String.valueOf(validate()));
                if (validate()) {

                    String number = binding.etMobile.getText().toString().trim();
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(String.format("+91%s", number))
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();
//                    Intent intent = new Intent(getApplicationContext(), VerifyOTP.class);
//                    intent.putExtra("mobile", binding.etMobile.getText().toString());
//                    intent.putExtra("backEndOTP", OTP[0]);
//                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Enter valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnGetOTP.setVisibility(View.VISIBLE);
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Phone Authentication", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("Phone Authentication", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private boolean validate() {
        String number = binding.etMobile.getText().toString().trim();
        if (number.length() != 10) {
            binding.etMobile.setError("Enter Valid Mobile Number");
            binding.etMobile.requestFocus();
            return false;
        }
        return true;
    }
}