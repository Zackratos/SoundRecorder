package com.danielkim.soundrecorder;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;

import com.android.client.AndroidSdk;
import com.ivy.dialog.EvaluationDialog;
import com.ivy.dialog.VersionDialog;
import com.ivy.util.JsonParser;
import com.ivy.util.Utility;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment implements View.OnClickListener {

    private ImageView startView;
    private ImageView stopView;
    private Chronometer chronometer;
    private ImageView leftWheel, rightWheel;
    private AnimatorSet animatorSet = new AnimatorSet();

    private MediaRecorder recorder = new MediaRecorder();

    private boolean isRecording;
    private boolean isPausing;
    private int current;
    private int count;

    public StartFragment() {
        // Required empty public constructor
    }


    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSdk.onCreate(getActivity(), new AndroidSdk.Builder());
/*        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder");
        if (!folder.exists()) {
            //folder /SoundRecorder doesn't exist, create the folder
            folder.mkdir();
        }*/

        update();
        count = SharePreferenceUtil.getBootCount(getActivity());
        if (count == 1) {
            praise();
        }

        File folder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder");
        if (!folder.exists()) {
            folder.mkdir();
        }
        current = SharePreferenceUtil.getCurrent(getActivity());
/*        File file;
        do {
            current++;
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".mp3";
            file = new File(filePath);
        } while (file.exists() && !file.isDirectory());*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_start, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recorder.release();
        recorder = null;
        animatorSet.end();
        SharePreferenceUtil.putCurrent(getActivity(), current);
        SharePreferenceUtil.putBootCount(getActivity(), ++count);
    }

    private void initView(View view) {
        startView = (ImageView) view.findViewById(R.id.start_start_record);
        stopView = (ImageView) view.findViewById(R.id.start_stop_record);
//        deleteView = (ImageView) view.findViewById(R.id.start_delete_record);
        chronometer = (Chronometer) view.findViewById(R.id.start_chronometer);
//        recorderView = (ImageView) view.findViewById(R.id.start_record_image);
        leftWheel = (ImageView) view.findViewById(R.id.start_wheel_left);
        rightWheel = (ImageView) view.findViewById(R.id.start_wheel_right);

        startView.setOnClickListener(this);
        stopView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_start_record:
                if (!isRecording) {
                    startRecord();
                }
//                initStartView();
                initStopView();
                break;
            case R.id.start_stop_record:
                if (isRecording) {
                    stopRecord();
                } else {
                    startActivity(RecordListActivity.newIntent(getActivity()));
                }
                initStopView();
                break;
            default:
                break;
        }
    }

    private void initStartView() {
        if (isRecording) {
            startView.setImageResource(R.drawable.stop_icon);
        } else {
            startView.setImageResource(R.drawable.start_icon);
        }
    }

    private void initStopView() {
        stopView.setImageResource(isRecording ? R.drawable.stop_icon : R.drawable.list_icon);
    }




    private void startRecord() {

        try {
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//            recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".mp3");
            recorder.setOutputFile(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".mp3");
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.prepare();
            recorder.start();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            playAnimation();
            current++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRecording = true;
    }


    private void pauseRecord() {


        isRecording = false;
        isPausing = true;
    }

    private void reStartRecord() {

        recorder.stop();
        isPausing = false;
        isRecording = true;
    }

    private void stopRecord() {
        recorder.stop();
        chronometer.stop();
        stopAnimation();
        isRecording = false;
    }


    private void playAnimation() {
        ObjectAnimator leftAnimator = ObjectAnimator.ofFloat(leftWheel, "rotation", 0f, 360f);
        ObjectAnimator rightAnimator = ObjectAnimator.ofFloat(rightWheel, "rotation", 0f, 360f);
//        leftAnimator.setInterpolator(new LinearInterpolator());
//        rightAnimator.setInterpolator(new LinearInterpolator());
        leftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rightAnimator.setRepeatCount(ValueAnimator.INFINITE);

        animatorSet.play(leftAnimator).with(rightAnimator);
        animatorSet.setDuration(10 * 1000);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.start();
    }

    private long pauseAnimation() {
        if (animatorSet != null && animatorSet.isRunning()) {
            long playTime = animatorSet.getStartDelay();
            animatorSet.cancel();
            return playTime;
        }
        return 0;
    }

    private void stopAnimation() {
        animatorSet.cancel();
    }


    private void update() {
        Default defaultJson = JsonParser.getInstance().fromJson(AndroidSdk.getExtraData(), Default.class);
        if (defaultJson.version > getVersionCode(getActivity())) {
            new VersionDialog(getActivity()).show().setCallBack(new VersionDialog.IUpdateCallBack() {
                @Override
                public void onUpdate(VersionDialog versionDialog) {
                    Utility.openPlayStore(getActivity(), getActivity().getPackageName());
                }

                @Override
                public void onCancel(VersionDialog versionDialog) {

                }
            });
        }
    }

    private int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo info;
        try {
            info = packageManager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void praise() {
        new EvaluationDialog(getActivity()).show();
    }

}
