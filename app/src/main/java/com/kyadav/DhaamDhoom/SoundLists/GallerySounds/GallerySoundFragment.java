package com.kyadav.DhaamDhoom.SoundLists.GallerySounds;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.kyadav.DhaamDhoom.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.kyadav.DhaamDhoom.R;
import com.kyadav.DhaamDhoom.SimpleClasses.Functions;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;
import com.kyadav.DhaamDhoom.SoundLists.Sounds_GetSet;
import com.kyadav.DhaamDhoom.SoundLists.VideoSound_A;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static android.app.Activity.RESULT_OK;


public class GallerySoundFragment extends RootFragment implements Player.EventListener {


    public static String running_sound_id;
    static boolean active = false;
    Context context;
    View view;
    ArrayList<Sounds_GetSet> datalist;
    GallerySoundAdapter adapter;
    RecyclerView listview;
    DownloadRequest prDownloader;
    IOSDialog iosDialog;
    SwipeRefreshLayout swiperefresh;
    View previous_view;
    Thread thread;
    SimpleExoPlayer player;
    String previous_url = "none";


    public GallerySoundFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_sound, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        context = getContext();

        running_sound_id = "none";

        iosDialog = new IOSDialog.Builder(context)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();

        PRDownloader.initialize(context);

        listview = view.findViewById(R.id.listview);
        listview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listview.setNestedScrollingEnabled(false);

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllAudios();
            }
        });

        getAllAudios();
    }

    public void getAllAudios() {
        datalist = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.DISPLAY_NAME, MediaStore.Audio.AudioColumns.DURATION
                , MediaStore.Audio.AudioColumns.DATE_MODIFIED};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String audio_path = cursor.getString(0);
                Log.d("audio_path", "" + audio_path);


                try {

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(context, Uri.parse(cursor.getString(0)));
                    int duration = Integer.parseInt(cursor.getString(2));

                    Sounds_GetSet item = new Sounds_GetSet();
                    item.id = null;
                    item.acc_path = audio_path;
                    item.sound_name = cursor.getString(1);
                    item.description = change_sec_to_time(duration);
                    item.section = null;
                    item.thum = null;
                    item.date_created = cursor.getString(3);
                    if (duration > 5000) {
                        datalist.add(item);
                    }

                } catch (Exception e) {

                }

            }
            cursor.close();
        }
        Set_adapter();
    }

    public String change_sec_to_time(long file_duration) {
        long second = (file_duration / 1000) % 60;
        long minute = (file_duration / (1000 * 60)) % 60;

        return String.format("%02d:%02d", minute, second);

    }

    public void Set_adapter() {

        adapter = new GallerySoundAdapter(context, datalist, new GallerySoundAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Sounds_GetSet item) {

                if (view.getId() == R.id.done) {
                    StopPlaying();
                    Convert_Mp3_to_acc(item.id, item.sound_name, item.acc_path);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        StopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        StopPlaying();
                        playaudio(view, item);
                    }
                }

            }
        });

        listview.setAdapter(adapter);
    }

    public void Convert_Mp3_to_acc(final String id, final String sound_name, String url) {
        Functions.Show_loader(context, false, false);
        AndroidAudioConverter.load(context, new ILoadCallback() {
            @Override
            public void onSuccess() {
                File flacFile = new File(url);
                IConvertCallback callback = new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Log.d("copyaudio", "" + convertedFile.getAbsolutePath() + "");
                        Functions.cancel_loader();
                        try {
                            VideoSound_A.copyFile(convertedFile, new File(Variables.app_folder + Variables.SelectedAudio_AAC));
                            Open_video_recording(id, sound_name);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Exception error) {
                        Functions.cancel_loader();
                        Toast.makeText(context, "" + error, Toast.LENGTH_SHORT).show();
                    }
                };
                AndroidAudioConverter.with(context)
                        .setFile(flacFile)
                        .setFormat(AudioFormat.AAC)
                        .setCallback(callback)
                        .convert();
            }

            @Override
            public void onFailure(Exception error) {
                Functions.cancel_loader();
            }
        });


    }

    public void Open_video_recording(final String id, final String sound_name) {
        Intent output = new Intent();
        output.putExtra("isSelected", "yes");
        output.putExtra("sound_name", sound_name);
        output.putExtra("sound_id", id);
        getActivity().setResult(RESULT_OK, output);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
        Log.d("copyaudio", "success");

    }

    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }

    public void playaudio(View view, final Sounds_GetSet item) {
        previous_view = view;

        if (previous_url.equals(item.acc_path)) {

            previous_url = "none";
            running_sound_id = "none";
        } else {

            previous_url = item.acc_path;
            running_sound_id = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));


            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);


        }

    }


    public void StopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;

        running_sound_id = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    public void Show_Run_State() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state() {
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id = "none";

    }


    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            Show_loading_state();
        } else if (playbackState == Player.STATE_READY) {
            Show_Run_State();
        } else if (playbackState == Player.STATE_ENDED) {
            show_Stop_state();
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }


}
