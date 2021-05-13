package com.kyadav.DhaamDhoom.Accounts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gmail.samehadar.iosdialog.IOSDialog;
import com.kyadav.DhaamDhoom.BuildConfig;
import com.kyadav.DhaamDhoom.Main_Menu.MainMenuActivity;
import com.kyadav.DhaamDhoom.R;
import com.kyadav.DhaamDhoom.SimpleClasses.ApiRequest;
import com.kyadav.DhaamDhoom.SimpleClasses.Callback;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationCompleteActivity extends AppCompatActivity {
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbTrans;
    private AppCompatEditText tName, tAge;
    IOSDialog iosDialog;
    JSONObject parameters = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_complete);
        tName = findViewById(R.id.tName);
        tAge = findViewById(R.id.tAge);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbTrans = findViewById(R.id.rbTrans);
        rbMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), b ? R.anim.zoom_in : R.anim.zoom_normal));
            }
        });
        rbFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), b ? R.anim.zoom_in : R.anim.zoom_normal));
            }
        });
        rbTrans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), b ? R.anim.zoom_in : R.anim.zoom_normal));
            }
        });
        iosDialog = new IOSDialog.Builder(this)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();
        if(getIntent().getExtras()!=null&&!getIntent().getExtras().containsKey("parameters"))
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginSelectionActivity.class));
        }else try{
            parameters=new JSONObject(getIntent().getExtras().getString("parameters","{}"));
        }catch (Exception ex){
            startActivity(new Intent(getApplicationContext(),LoginSelectionActivity.class));
        }
    }

    public void registerOnClick(View view) {

        String name = tName.getText().toString().trim();
        String age = tAge.getText().toString().trim();
        String gender = "";
        switch (rgGender.getCheckedRadioButtonId()) {
            case R.id.rbMale:
                gender = "m";
                break;
            case R.id.rbFemale:
                gender = "f";
                break;
            case R.id.rbTrans:
                gender = "t";
                break;
        }
        if (gender.isEmpty())
            Toast.makeText(getApplicationContext(), R.string.select_gender, Toast.LENGTH_SHORT).show();
        else if (name.isEmpty())
            Toast.makeText(getApplicationContext(), R.string.enter_your_name, Toast.LENGTH_SHORT).show();
        else if (age.isEmpty())
            Toast.makeText(getApplicationContext(), R.string.enter_age, Toast.LENGTH_SHORT).show();
        else {
            try {
                parameters.put("first_name", name);
                parameters.put("age", age);
                parameters.put("gender", gender);
            } catch (Exception ex) {

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
    }



    public void Parse_signup_data(String loginData) {
        try {

            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                SharedPreferences sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
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
}