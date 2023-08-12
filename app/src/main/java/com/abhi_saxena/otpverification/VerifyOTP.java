package com.abhi_saxena.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abhi_saxena.otpverification.databinding.ActivityVerifyOtpBinding;
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

public class VerifyOTP extends AppCompatActivity {

    ActivityVerifyOtpBinding binding;
    String backEndOTP;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                binding.pbLoading.setVisibility(View.GONE);


                Log.d("Phone Authentication", "onVerificationCompleted:" + phoneAuthCredential);

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                binding.pbLoading.setVisibility(View.GONE);

                Log.w("Phone Authentication", "onVerificationFailed: " + e.getMessage().toString());
            }

            @Override
            public void onCodeSent(@NonNull String newBackEndOTP, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                binding.pbLoading.setVisibility(View.GONE);
                backEndOTP = newBackEndOTP;
                Toast.makeText(VerifyOTP.this, "OTP sent Successfully", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), VerifyOTP.class);
//                intent.putExtra("mobile", getIntent().getStringExtra("mobile"));
//                intent.putExtra("backEndOTP", newBackEndOTP);
//                startActivity(intent);
            }
        };

        binding.tvMobile.setText(String.format(
                "+91-%s", getIntent().getStringExtra("mobile")
        ));

        backEndOTP = getIntent().getStringExtra("backEndOTP");

        binding.btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.btnVerifyOTP.setVisibility(View.GONE);
                if (validate()) {
                    String enteredOTP = binding.etOTP1.getText().toString() +
                            binding.etOTP2.getText().toString() +
                            binding.etOTP3.getText().toString() +
                            binding.etOTP4.getText().toString() +
                            binding.etOTP5.getText().toString() +
                            binding.etOTP6.getText().toString();
                    if (backEndOTP != null) {
                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                                backEndOTP, enteredOTP
                        );
                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        binding.pbLoading.setVisibility(View.GONE);
                                        binding.btnVerifyOTP.setVisibility(View.VISIBLE);
                                        if (task.isSuccessful()) {
                                            // todo: tutorial followed
                                            // using https://youtu.be/j0kQ_4D5YNo?t=1274
                                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(VerifyOTP.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        ;
                    } else {
                        Toast.makeText(VerifyOTP.this, "Unable to Fetch OTP from server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerifyOTP.this, "Enter Valid OTP", Toast.LENGTH_SHORT).show();
                }
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnVerifyOTP.setVisibility(View.VISIBLE);
            }
        });

        optDigitMove();

        binding.tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(String.format("+91%s", getIntent().getStringExtra("mobile")))
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(VerifyOTP.this)
                        .setCallbacks(mCallbacks)
                        .build();
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

    private void optDigitMove() {
        binding.etOTP1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.etOTP2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etOTP2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.etOTP3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etOTP3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.etOTP4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etOTP4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.etOTP5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etOTP5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.btnVerifyOTP.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean validate() {
        String opt1, opt2, opt3, opt4, opt5, opt6;
        opt1 = binding.etOTP1.getText().toString();
        opt2 = binding.etOTP2.getText().toString();
        opt3 = binding.etOTP3.getText().toString();
        opt4 = binding.etOTP4.getText().toString();
        opt5 = binding.etOTP5.getText().toString();
        opt6 = binding.etOTP6.getText().toString();
        return !opt1.isEmpty() && !opt2.isEmpty() && !opt3.isEmpty() && !opt4.isEmpty() && !opt5.isEmpty() && !opt6.isEmpty();
    }
}