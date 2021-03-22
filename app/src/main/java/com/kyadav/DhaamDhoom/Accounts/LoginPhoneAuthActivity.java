package com.kyadav.DhaamDhoom.Accounts;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.kyadav.DhaamDhoom.R;

import java.util.concurrent.TimeUnit;

public class LoginPhoneAuthActivity extends AppCompatActivity {

    private LinearLayout linearMobile, linearOtp;
    private AppCompatEditText tMobile;
    private AppCompatEditText tOtp;
    private PhoneAuthProvider.ForceResendingToken finalforceResendingToken;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(LoginPhoneAuthActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            //super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            finalforceResendingToken = forceResendingToken;
            linearMobile.setVerticalGravity(View.GONE);
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_auth);
        linearMobile = findViewById(R.id.linearMobile);
        linearOtp = findViewById(R.id.linearOtp);
        tMobile = findViewById(R.id.tMobile);
        tOtp = findViewById(R.id.tOtp);
        mAuth = FirebaseAuth.getInstance();
    }

    public void sendOTP(View view) {
        String mobileNo = tMobile.getText().toString();
        if (!Patterns.PHONE.matcher(mobileNo).find()) {
            Toast.makeText(getApplicationContext(), "Enter Valid Number !",
                    Toast.LENGTH_LONG).show();
        } else {

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    mobileNo,
                    60,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    mCallbacks);
        }
    }

    public void verifyOTP(View view) {
        String otp = tOtp.getText().toString();
        if (otp.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Valid Otp !",
                    Toast.LENGTH_LONG).show();
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            if (credential != null) {
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(getApplicationContext(), "Enter Valid OTP !",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "success",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "Enter Valid OTP !",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Enter Valid OTP !",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}