package com.kyadav.DhaamDhoom.VideoAction;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kyadav.DhaamDhoom.R;
import com.kyadav.DhaamDhoom.SimpleClasses.Fragment_Callback;
import com.kyadav.DhaamDhoom.SimpleClasses.Functions;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;

import java.io.File;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoAction_F extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RecyclerView recyclerView;

    Fragment_Callback fragment_callback;

    String video_id;

    ProgressBar progressBar;
    VideoSharingApps_Adapter adapter;

    public VideoAction_F() {
    }


    @SuppressLint("ValidFragment")
    public VideoAction_F(String id, Fragment_Callback fragment_callback) {
        video_id = id;
        this.fragment_callback = fragment_callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_action, container, false);
        context = getContext();

        progressBar = view.findViewById(R.id.progress_bar);
        view.findViewById(R.id.save_video_layout).setOnClickListener(this);
        view.findViewById(R.id.copy_layout).setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Get_Shared_app();
            }
        }, 1000);

        return view;
    }

    public void Get_Shared_app() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    PackageManager pm = getActivity().getPackageManager();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://google.com");

                    List<ResolveInfo> launchables = pm.queryIntentActivities(intent, 0);

                    for (int i = 0; i < launchables.size(); i++) {

                        if (launchables.get(i).activityInfo.name.contains("SendTextToClipboardActivity")) {
                            launchables.remove(i);
                            break;
                        }

                    }

                    Collections.sort(launchables,
                            new ResolveInfo.DisplayNameComparator(pm));

                    adapter = new VideoSharingApps_Adapter(context, launchables, new VideoSharingApps_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int positon, ResolveInfo item, View view) {
                            Toast.makeText(context, "" + item.activityInfo.name, Toast.LENGTH_SHORT).show();
                            Open_App(item);
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                } catch (Exception e) {

                }
            }
        }).start();


    }


    public void Open_App(ResolveInfo resolveInfo) {
        try {

            ActivityInfo activity = resolveInfo.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent i = new Intent(Intent.ACTION_MAIN);

            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);

            if (Functions.Checkstoragepermision(getActivity())) {

                Bundle bundle = new Bundle();
                bundle.putString("action", "save");
                dismiss();
                fragment_callback.Responce(bundle);
            }

            File f = new File("/storage/emulated/0/Tittic/" + video_id + ".mp4");
            Uri uriPath = Uri.parse(f.getPath());

            String text = "Dhaam Dhoom\n\n" + Variables.base_url + "view.php?id=" + video_id + "\n\n\nConnect with us\n\nhttps://play.google.com/store/apps/details?id=com.kyadav.DhaamDhoom";

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
            shareIntent.setType("video/*");

            //shareIntent.setType("text/plain");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "send"));


        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_video_layout:

                if (Functions.Checkstoragepermision(getActivity())) {

                    Bundle bundle = new Bundle();
                    bundle.putString("action", "save");
                    dismiss();
                    fragment_callback.Responce(bundle);
                }

                break;

            case R.id.copy_layout:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                String text = Variables.base_url + "view.php?id=" + video_id + "\n\n\nConnect with us\n\nhttps://play.google.com/store/apps/details?id=com.kyadav.DhaamDhoom";
                ClipData clip = ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Link Copy in clipboard", Toast.LENGTH_SHORT).show();
                break;
        }
    }


}
