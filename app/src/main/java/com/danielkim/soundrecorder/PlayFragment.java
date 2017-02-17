package com.danielkim.soundrecorder;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.client.AndroidSdk;
import com.android.client.ClientNativeAd;

import java.io.File;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends Fragment implements View.OnClickListener {

    private File file;
    private MediaPlayer mp;

    private ImageView playView;
    private ImageView deleteView;
    private ImageView backView;
    private SeekBar seekBar;
    private TextView currentView;
//    private Chronometer chronometer;
    private TextView timeView;
//    private FrameLayout adContainer;


    private Handler handler;
    private Runnable runnable;


    private static final String TAG_AD = "recorder";

    public PlayFragment() {
        // Required empty public constructor
    }

    public static PlayFragment newInstance(String name) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSdk.onCreate(getActivity(), new AndroidSdk.Builder());
        String name = getArguments().getString("name");
        file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + name);
        mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                initPlayView();
                handler.removeCallbacks(runnable);
            }
        });
        try {
            mp.setDataSource(file.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                currentView.setText(switchTime(mp.getCurrentPosition()));
                seekBar.setProgress(mp.getCurrentPosition() / 1000);
                handler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidSdk.onResume(getActivity());
    }

    private void initView(View view) {
        playView = (ImageView) view.findViewById(R.id.play_play);
        deleteView = (ImageView) view.findViewById(R.id.play_delete);
        backView = (ImageView) view.findViewById(R.id.play_back);
        seekBar = (SeekBar) view.findViewById(R.id.play_seekBar);
//        chronometer = (Chronometer) view.findViewById(R.id.play_chronometer);
        currentView = (TextView) view.findViewById(R.id.play_current);
        timeView = (TextView) view.findViewById(R.id.play_time);

        seekBar.setMax(Math.round(mp.getDuration() / 1000));
        timeView.setText(switchTime(mp.getDuration()));
        playView.setOnClickListener(this);
        deleteView.setOnClickListener(this);
        backView.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress() * 1000);
                currentView.setText(switchTime(seekBar.getProgress() * 1000));
            }
        });

        FrameLayout adContainer = (FrameLayout) view.findViewById(R.id.play_ad_container);
        View adView = null;
        if (AndroidSdk.hasNativeAd(TAG_AD, AndroidSdk.NATIVE_AD_TYPE_ALL)) {
            if (adView == null) {
                adView = AndroidSdk.peekNativeAdViewWithLayout(TAG_AD, AndroidSdk.NATIVE_AD_TYPE_ALL, R.layout.native_ad, new ClientNativeAd.NativeAdClickListener() {
                    @Override
                    public void onNativeAdClicked(ClientNativeAd clientNativeAd) {

                    }
                });
            }
            if (adView != null) {
                adContainer.addView(adView);
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_play:
                if (mp.isPlaying()) {
                    mp.pause();
                    handler.removeCallbacks(runnable);
                } else {
                    mp.start();
                    handler.post(runnable);
//                    chronometer.setBase(SystemClock.elapsedRealtime() - base);
//                    chronometer.start();
                }
                initPlayView();
                break;
            case R.id.play_delete:
                if (!mp.isPlaying()) {
//                    DeleteDialog.newInstance().show(getFragmentManager(), "TAG");
                    DeleteDialog deleteDialog = DeleteDialog.newInstance();
                    deleteDialog.setOnPositiveClickListener(new DeleteDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            file.delete();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    });
                    deleteDialog.show(getFragmentManager(), "TAG");
                }
                break;
            case R.id.play_back:
                getActivity().finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidSdk.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AndroidSdk.onDestroy();
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.release();
        handler.removeCallbacks(runnable);
    }

    private void initPlayView() {
        playView.setImageResource(mp.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
    }


    private String switchTime(long duration) {
        int totalSecond = Math.round(duration / 1000);
        int second = totalSecond % 60;
        int minute = second / 60;
        String secondString = second >= 10 ? second + "" : "0" + second;
        String minuteString = minute >= 10 ? minute + "" : "0" + minute;
        return minuteString + ":" + secondString;
    }

    private int convertStringToSeconds(String stopTime) {
        String[] time = stopTime.split(":");
        return Integer.valueOf(time[0]) * 60 + Integer.valueOf(time[1]);
    }

}
