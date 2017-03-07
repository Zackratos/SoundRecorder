package com.danielkim.soundrecorder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.client.AndroidSdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecorderService extends Service {
    private boolean isRecording;
    private boolean isDeleted;
    private boolean isPausing;
    private MediaRecorder recorder;
    private int count;

    private List<File> tempFiles = new ArrayList<>();


    public RecorderService() {
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, RecorderService.class);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void popNotification() {
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_layout);
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.notification_go, pi);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification = builder
                .setSmallIcon(R.drawable.notification_small_icon)
                .setContent(rv)
                .build();

        startForeground(1, notification);
    }


    private void startRecord() {
        resetRecorder();
        isRecording = true;
    }

    private void pauseRecord() {
        recorder.stop();
        isRecording = false;
        isPausing = true;
        tempFiles.add(new File(getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr"));
    }

    private void reStartRecord() {
        count++;

        resetRecorder();
        isPausing = false;
        isRecording = true;
    }


    private void resetRecorder() {
        try {
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//            recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioChannels(1);
            recorder.prepare();
            recorder.start();
            isRecording = true;
            isDeleted = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        File file = new File(getExternalCacheDir().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/" + "temp" + count + ".amr");
        if (!tempFiles.contains(file)) {
            tempFiles.add(file);
        }
        String name = getInputCollection();

        initRecorder();

    }

    private void initRecorder() {
        if (isRecording) {
            recorder.stop();
        }
        isRecording = false;
        isPausing = false;


        count = 0;
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
        tempFiles.clear();
    }

    private String getCurrentTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(date);
    }

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
        Toast.makeText(this, "Voice has saved in /Voice Recorder/", Toast.LENGTH_SHORT).show();
        return name;

    }


    public class RecorderBinder extends Binder {

        public void clickStartButton() {
            if (isRecording) {
                pauseRecord();
            } else if (isPausing) {
                reStartRecord();
            } else {
                startRecord();
//                AndroidSdk.track("录音按钮", "点击次数", "", 1);
            }
        }

        public void clickStopButton() {
            if (isRecording || isPausing) {
                stopRecord();
            } else {
                isDeleted = true;
                startActivity(RecordListActivity.newIntent(RecorderService.this));
            }
        }


        public void clickDeleteButton() {
            initRecorder();
            isDeleted = true;
        }


    }
}
