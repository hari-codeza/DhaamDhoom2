package com.kyadav.DhaamDhoom.SimpleClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.kyadav.DhaamDhoom.R;

import java.util.ArrayList;
import java.util.List;

public class AppPermissions {
    public static String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static String[] STORAGE_PERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] CALENDAR_PERMISSION = {Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR};
    public static String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};
    public static String[] AUDIO_PERMISSION = {Manifest.permission.RECORD_AUDIO};
    public static String[] CONTACT_PERMISSION = {Manifest.permission.READ_CONTACTS};
    public static String[] CALL_LOG_PERMISSION = {Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALL_LOG};
    public static String[] TELEPHONE_PERMISSION = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS};
    private Activity mActivity;
    private View mView;
    private String rationaleMsg = "";

    public AppPermissions(@NonNull Activity activity) {
        mActivity = activity;
        mView = activity.findViewById(android.R.id.content);
    }

    public AppPermissions(@NonNull Fragment fragment) {
        mActivity = fragment.getActivity();
        mView = fragment.getView();
    }

    public void setRationaleMessage(String rationaleMsg) {
        this.rationaleMsg = rationaleMsg == null ? "" : rationaleMsg;
    }

    public boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(mActivity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermission(String[] permissionsList) {
        for (String permission : permissionsList) {
            if (ActivityCompat.checkSelfPermission(mActivity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void requestPermission(String permission, int requestCode) {
        if (ActivityCompat.checkSelfPermission(mActivity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                askSetting(mView, rationaleMsg);
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, requestCode);
            }
        }
    }

    public void requestPermission(String[] permissionsList, int requestCode) {
        List<String> permissionNeeded = new ArrayList<>();
        for (String permission : permissionsList) {
            if (ActivityCompat.checkSelfPermission(mActivity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded.add(permission);
            }
        }
        if (permissionNeeded.size() > 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissionNeeded.get(0))) {
                askSetting(mView, rationaleMsg);
            } else {
                ActivityCompat.requestPermissions(mActivity,
                        permissionNeeded.toArray(new String[permissionNeeded.size()]), requestCode);
            }
        }
    }

    private void askSetting(@NonNull View view, @NonNull String msg) {
        Snackbar sb = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        TextView tv = sb.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setGravity(Gravity.LEFT);
        tv.setMaxLines(5);
        sb.setAction(R.string.grant, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", view.getContext().getPackageName(), null)));
            }
        });
        sb.show();
    }
}
