package com.kyadav.DhaamDhoom.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.kyadav.DhaamDhoom.BuildConfig;
import com.kyadav.DhaamDhoom.Main_Menu.MainMenuActivity;
import com.kyadav.DhaamDhoom.R;
import com.kyadav.DhaamDhoom.SimpleClasses.ApiRequest;
import com.kyadav.DhaamDhoom.SimpleClasses.Callback;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class LoginPhoneAuthActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    IOSDialog iosDialog;
    SharedPreferences sharedPreferences;
    private LinearLayout linearMobile, linearOtp;
    private AppCompatEditText tMobile;
    private AppCompatEditText tOtp;
    private AppCompatTextView tPhoneCode;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            super.onCodeSent(verificationId, token);
            Log.d(TAG, "onCodeSent:" + verificationId);
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
            linearMobile.setVisibility(View.GONE);
            linearOtp.setVisibility(View.VISIBLE);
            iosDialog.hide();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);
            String code = credential.getSmsCode();
            if (code != null) {
                tOtp.setText(code);
                verifyOTP(null);
            } else {
                signInWithPhoneAuthCredential(credential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            iosDialog.hide();
            Toast.makeText(LoginPhoneAuthActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_auth);
        mAuth = FirebaseAuth.getInstance();
        linearMobile = findViewById(R.id.linearMobile);
        linearOtp = findViewById(R.id.linearOtp);
        tPhoneCode = findViewById(R.id.tPhoneCode);
        tMobile = findViewById(R.id.tMobile);
        tOtp = findViewById(R.id.tOtp);

        iosDialog = new IOSDialog.Builder(this)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();
        sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void sendOTP(View view) {
        String mobileNo = tMobile.getText().toString();
        String phoneCode = tPhoneCode.getText().toString();
        if (!Patterns.PHONE.matcher(mobileNo).find()) {
            Toast.makeText(getApplicationContext(), "Enter Valid Number !",
                    Toast.LENGTH_LONG).show();
        } else {

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneCode + mobileNo,
                    60,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    mCallbacks);
            iosDialog.show();
        }
    }

    public void verifyOTP(View view) {
        String otp = tOtp.getText().toString();
        if (otp.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Otp !",
                    Toast.LENGTH_LONG).show();
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            if (credential != null) {
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(getApplicationContext(), "Invalid OTP !",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        iosDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        iosDialog.hide();
                        if (task.isSuccessful()) {
                            FirebaseUser cUser = task.getResult().getUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                JSONObject parameters = new JSONObject();
                                try {

                                    parameters.put("fb_id", cUser.getPhoneNumber());
                                    parameters.put("first_name", "" + cUser.getDisplayName());
                                    parameters.put("last_name", "" + "");
                                    parameters.put("profile_pic", cUser.getPhotoUrl() == null ? null : cUser.getPhotoUrl().toString());
                                    parameters.put("gender", "n");
                                    parameters.put("version", BuildConfig.VERSION_NAME);
                                    parameters.put("signup_type", "Defualt");
                                    parameters.put("device", Variables.device);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Intent it = new Intent(getApplicationContext(), RegistrationCompleteActivity.class);
                                it.putExtra("parameters", parameters.toString());
                                startActivity(it);
                                finish();
                            } else {
                                String pic_url = "https://graph.facebook.com/picture?width=500&width=500";

                                //String id = Profile.getCurrentProfile().getId();
                                Call_Api_For_Signup(cUser.getPhoneNumber(), cUser.getDisplayName(), "", cUser.getPhotoUrl() == null ? null : cUser.getPhotoUrl().toString(), "Default");
                            }
                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(getApplicationContext(), "Authentication failed !",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failure",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // this function call an Api for Signin
    private void Call_Api_For_Signup(String id,
                                     String f_name,
                                     String l_name,
                                     String picture,
                                     String singnup_type) {

        String appversion = BuildConfig.VERSION_NAME;

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("fb_id", id);
            parameters.put("first_name", "" + f_name);
            parameters.put("last_name", "" + l_name);
            parameters.put("profile_pic", picture);
            parameters.put("gender", "n");
            parameters.put("version", appversion);
            parameters.put("signup_type", singnup_type);
            parameters.put("device", Variables.device);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        iosDialog.show();
        ApiRequest.Call_Api(this, Variables.SignUp, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                iosDialog.cancel();
                Parse_signup_data(resp);

            }
        });

    }

    public void Parse_signup_data(String loginData) {
        try {

            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                JSONObject userdata = jsonArray.getJSONObject(0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.u_id, userdata.optString("fb_id"));
                editor.putString(Variables.f_name, userdata.optString("first_name"));
                editor.putString(Variables.l_name, userdata.optString("last_name"));
                editor.putString(Variables.u_name, userdata.optString("first_name") + " " + userdata.optString("last_name"));
                editor.putString(Variables.gender, userdata.optString("gender"));
                editor.putString(Variables.u_pic, userdata.optString("profile_pic"));
                editor.putBoolean(Variables.islogin, true);
                editor.commit();

                Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
                Variables.user_id = Variables.sharedPreferences.getString(Variables.u_id, "");

                finish();
                startActivity(new Intent(this, MainMenuActivity.class));


            } else {
                Toast.makeText(this, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // this function will print the keyhash of your project
    // which is very helpfull during Fb login implimentation
    public void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}