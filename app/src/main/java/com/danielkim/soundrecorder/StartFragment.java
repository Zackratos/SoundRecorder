package com.danielkim.soundrecorder;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.client.AndroidSdk;
import com.ivy.dialog.EvaluationDialog;
import com.ivy.dialog.VersionDialog;
import com.ivy.util.JsonParser;
import com.ivy.util.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment implements View.OnClickListener {

    private ImageView startView;
    private ImageView stopView;
    private TextView currentView;
    private ImageView deleteView;
    private ImageView leftWheel, rightWheel;

    private MediaRecorder recorder = new MediaRecorder();

    private boolean isRecording;
    private boolean isPausing;
    private boolean isDeleted;
//    private int current;
    private int count;
    private int bootCount;
    private long animationCurrent;
    private int animationCount;
    private List<File> tempFiles = new ArrayList<>();

    private ObjectAnimator leftAnimator, rightAnimator;





    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSdk.onCreate(getActivity(), new AndroidSdk.Builder());
        AndroidSdk.track("主界面", "启动次数", "", 1);
        update();
        bootCount = SharePreferenceUtil.getBootCount(getActivity());
        if (bootCount == 1) {
            praise();
        }
        File rf = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder");
        File tf = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp");
//        File recordFolder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder");
//        File tempFolder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp");
        if (!rf.exists()) {
            rf.mkdir();
        }
        if (!tf.exists()) {
            tf.mkdir();
        }
//        current = SharePreferenceUtil.getCurrent(getActivity());

        popNotification();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_start, container, false);
        initView(view);
        initAnimation();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            resetRecorder();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            recorder.stop();
        }
        recorder.release();
        recorder = null;
//        SharePreferenceUtil.putCurrent(getActivity(), current);
        SharePreferenceUtil.putBootCount(getActivity(), ++bootCount);
    }

    private void initView(View view) {
        startView = (ImageView) view.findViewById(R.id.start_start_record);
        stopView = (ImageView) view.findViewById(R.id.start_stop_record);
        deleteView = (ImageView) view.findViewById(R.id.start_delete_record);
        currentView = (TextView) view.findViewById(R.id.start_current);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        currentView.setTypeface(tf);

        leftWheel = (ImageView) view.findViewById(R.id.start_wheel_left);
        rightWheel = (ImageView) view.findViewById(R.id.start_wheel_right);

        startView.setOnClickListener(this);
        stopView.setOnClickListener(this);
        deleteView.setOnClickListener(this);

        deleteView.setClickable(false);
    }

    private void initAnimation() {
        leftAnimator = ObjectAnimator.ofFloat(leftWheel, "rotation", 0f, 360f);
        rightAnimator = ObjectAnimator.ofFloat(rightWheel, "rotation", 0f, 360f);


        leftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rightAnimator.setRepeatCount(ValueAnimator.INFINITE);

        leftAnimator.setInterpolator(new LinearInterpolator());
        rightAnimator.setInterpolator(new LinearInterpolator());

        leftAnimator.setDuration(10 * 1000);
        rightAnimator.setDuration(10 * 1000);

        leftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentView.setText(convertTimeToString(
                        Build.VERSION.SDK_INT >= 24 ?
                        valueAnimator.getCurrentPlayTime() :
                                animationCount * 10 * 1000 + valueAnimator.getCurrentPlayTime()));
            }
        });

        leftAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                animationCount++;
            }
        });



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_start_record:
/*                if (!isRecording) {
                    startRecord();
                }*/
                if (isRecording) {
                    pauseRecord();
                } else if (isPausing) {
                    reStartRecord();
                } else {
                    startRecord();
                    AndroidSdk.track("录音按钮", "点击次数", "", 1);
                }
                initStartView();
                initStopView();
                initDeleteView();
                break;
            case R.id.start_stop_record:
                if (isRecording || isPausing) {
                    stopRecord();
                } else {
                    isDeleted = true;
                    startActivity(RecordListActivity.newIntent(getActivity()));
                }
                initStartView();
                initStopView();
                initDeleteView();
                break;
            case R.id.start_delete_record:
/*                File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".amr");
                if (file.exists()) {
                    file.delete();
                }*/
                initRecorder();
//                current--;
                isDeleted = true;

                initStartView();
                initStopView();
                initDeleteView();
                break;
            default:
                break;
        }
    }

    private void initStartView() {
        if (isRecording) {
            startView.setImageResource(R.drawable.pause_icon);
        } else if (isPausing){
            startView.setImageResource(R.drawable.play_icon);
        } else {
            startView.setImageResource(R.drawable.start_icon);
        }
    }

    private void initStopView() {
        stopView.setImageResource(isRecording || isPausing ? R.drawable.stop_icon : R.drawable.list_icon);
    }

    private void initDeleteView() {
/*        if (isRecording || isDeleted) {
            deleteView.setImageResource(R.drawable.delete_icon_off);
            deleteView.setClickable(false);
        } else {
            deleteView.setImageResource(R.drawable.delete_icon_on);
            deleteView.setClickable(true);
        }*/
        if (isPausing && !isDeleted) {
            deleteView.setImageResource(R.drawable.delete_icon_on);
            deleteView.setClickable(true);
        } else {
            deleteView.setImageResource(R.drawable.delete_icon_off);
            deleteView.setClickable(false);
        }
    }



    private void resetRecorderPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            resetRecorder();
        }
    }



    private void resetRecorder() {
        try {
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            recorder.setOutputFile(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//            recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioChannels(1);
            recorder.prepare();
            recorder.start();
//            chronometer.setBase(SystemClock.elapsedRealtime() - animationCurrent);
//            chronometer.start();
            isRecording = true;
            isDeleted = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
//        current++;
        resetRecorderPermission();
        playAnimation();
        isRecording = true;
    }


    private void pauseRecord() {
//        chronometer.stop();
        recorder.stop();
        pauseAnimation();
        isRecording = false;
        isPausing = true;
        tempFiles.add(new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr"));
//        tempFiles.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/" + "temp" + count + ".amr"));
//        mergeAmrFiles();
    }

    private void reStartRecord() {
        count++;
        resetRecorderPermission();

        reStartAnimation();
        isPausing = false;
        isRecording = true;
    }

    private void stopRecord() {
/*        if (isRecording) {
            recorder.stop();
        }
        isRecording = false;
        isPausing = false;
        animationCurrent = 0;
        animationCount = 0;
        pauseAnimation();
        stopAnimation();
        File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
        if (!tempFiles.contains(file)) {
            tempFiles.add(file);
        }
        getInputCollection();
        count = 0;
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
        tempFiles.clear();*/
        File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
        if (!tempFiles.contains(file)) {
            tempFiles.add(file);
        }
        final String name = getInputCollection();

        initRecorder();

        RenameDialog dialog = RenameDialog.newInstance(true, name);
        dialog.setOnButtonClickListener(new RenameDialog.OnButtonClickListener() {
            @Override
            public void onPositiveClick(File file) {
                isDeleted = true;
                startActivity(RecordListActivity.newIntent(getActivity()));
            }

            @Override
            public void onNegativeClick() {
//                File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".amr");
//                File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + name + ".amr");
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + name + ".amr");
                if (file.exists()) {
                    file.delete();
//                    current--;
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "rename");
    }


    private void initRecorder() {
        if (isRecording) {
            recorder.stop();
        }
        isRecording = false;
        isPausing = false;

        animationCurrent = 0;
        animationCount = 0;
//        pauseAnimation();
        stopAnimation();
        currentView.setText("00:00:00");
        count = 0;
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
        tempFiles.clear();
    }

    private void playAnimation() {
        leftAnimator.start();
        rightAnimator.start();
    }

    private void pauseAnimation() {
        if (leftAnimator.isRunning() && rightAnimator.isRunning()) {
            animationCurrent = leftAnimator.getCurrentPlayTime();
            leftAnimator.cancel();
            rightAnimator.cancel();
        }
    }

    private void reStartAnimation() {
        playAnimation();
        leftAnimator.setCurrentPlayTime(animationCurrent);
        rightAnimator.setCurrentPlayTime(animationCurrent);
    }

    private void stopAnimation() {
        leftAnimator.end();
        rightAnimator.end();
    }


    private void popNotification() {
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews rv = new RemoteViews(getActivity().getPackageName(), R.layout.notification_layout);
        Intent intent = new Intent(getActivity().getApplicationContext(), StartActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.notification_go, pi);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        Notification notification = builder
                .setSmallIcon(R.drawable.notification_small_icon)
                .setContent(rv)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        manager.notify(1, notification);
    }

    private String getCurrentTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(date);
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



    private String convertTimeToString(long time) {
        int totalSecond = Math.round(time / 1000);
        int second = totalSecond % 60;
        int minute = totalSecond / 60;
        int hour = minute / 60;
        String hourString = hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
        String minuteString = minute < 10 ? "0" + String .valueOf(minute) : String.valueOf(minute);
        String secondString = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);

        return hourString + ":" + minuteString + ":" + secondString;
    }

//    private String convert

    private long convertStrTimeToLong (String text) {
        String[] time = text.split(":");
        return SystemClock.elapsedRealtime() - (Long.valueOf(time[0]) * 60 + Long.valueOf(time[1])) * 1000;
    }






    /**
     * 需求:将两个amr格式音频文件合并为1个
     * 注意:amr格式的头文件为6个字节的长度
     * @param partsPaths       各部分路径
     * @param unitedFilePath   合并后路径
     */
    private void uniteAMRFile(String[] partsPaths, String unitedFilePath) {
        try {
            File unitedFile = new File(unitedFilePath);
            FileOutputStream fos = new FileOutputStream(unitedFile);
            RandomAccessFile ra = null;
            for (int i = 0; i < partsPaths.length; i++) {
                ra = new RandomAccessFile(partsPaths[i], "r");
                if (i != 0) {
                    ra.seek(6);
                }
                byte[] buffer = new byte[1024 * 8];
                int len = 0;
                while ((len = ra.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            ra.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*    *//** 合并音频文件 *//*
    public void mergeAmrFiles(){
        // 创建音频文件,合并的文件放这里
        File targetFile = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".amr");
        FileOutputStream fileOutputStream = null;

        if(!targetFile.exists()){
            try {
                targetFile.createNewFile();
            } catch (IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            fileOutputStream = new FileOutputStream(targetFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }*/



    /**
     *  @return 将合并的流用字符保存
     */
    public String getInputCollection(){



        // 创建音频文件,合并的文件放这里
//        File file1 = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + "voice recorder " + current + ".amr");
        String name = getCurrentTime();
//        File file1 = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + name + ".amr");
        File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + name + ".amr");
        FileOutputStream fileOutputStream = null;

        if(!file1.exists()){
            try {
                file1.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            fileOutputStream = new FileOutputStream(file1);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件




        for(int i = 0; i < tempFiles.size(); i++){
            File file = tempFiles.get(i);

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte  []myByte = new byte[fileInputStream.available()];
                //文件长度
                int length = myByte.length;

                //头文件
                if(i==0){
                    while(fileInputStream.read(myByte) != -1){
                        Log.d("TAG", "0-0");
                        fileOutputStream.write(myByte, 0, length);
                        Log.d("TAG", "0-1");
                    }
                }

                //之后的文件，去掉头文件就可以了
                else{
                    while(fileInputStream.read(myByte) != -1){
                        Log.d("TAG", "1-0");
                        fileOutputStream.write(myByte, 6, length-6);
                        Log.d("TAG", "1-1");
                    }
                }

                fileOutputStream.flush();
                fileInputStream.close();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("TAG", "catch");
                e.printStackTrace();
            }



        }
        //结束后关闭流
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), "Voice has saved in /Voice Recorder/", Toast.LENGTH_SHORT).show();
        return name;

    }


}
