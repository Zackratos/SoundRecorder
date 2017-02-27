package com.danielkim.soundrecorder;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.client.AndroidSdk;
import com.android.client.ClientNativeAd;
import com.zhy.autolayout.utils.AutoUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordListFragment extends Fragment {

//    private File[] files;
//    private List<File> fileList;

    private static final String TAG_AD = "list";

    private List<Record> records;
    private RecordAdapter adapter;
    private int resultPosition;
//    private File file;

    public RecordListFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RecordListFragment newInstance() {
        RecordListFragment fragment = new RecordListFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSdk.onCreate(getActivity(), new AndroidSdk.Builder());
        records = new ArrayList<>();
//        fileList = new ArrayList<>();
        adapter = new RecordAdapter();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            getVoiceRecord();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getVoiceRecord();
        }
    }

    private void getVoiceRecord() {

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 0) {
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder");
//                File folder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder");
                File[] files = folder.listFiles();
/*                for (int start = 0, end = files.length - 1; start < end; start++, end--) {
                    File temp = files[end];
                    files[end] = files[start];
                    files[start] = temp;
                }*/
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        long diff = f1.lastModified() - f2.lastModified();
                        if (diff > 0)
                            return -1;
                        else if (diff == 0)
                            return 0;
                        else
                            return 1;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return true;
                    }
                });


                for (File file : files) {
                    try {
                        MediaPlayer mp = MediaPlayer.create(getActivity(), Uri.fromFile(file));
                        records.add(new Record(file, mp.getDuration()));
                        mp.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Message msg = handler.obtainMessage();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidSdk.onResume(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_record_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Log.d("TAG", "name = " + data.getStringExtra("name"));
//            File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + data.getStringExtra("name"));
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + data.getStringExtra("name"));
//            fileList.remove(file);
            records.remove(resultPosition);
            file.delete();
            adapter.notifyItemRemoved(resultPosition + 1);
            adapter.notifyItemRangeChanged(resultPosition + 1, records.size());
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
    }

    private void initView(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.list_list);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rv.setAdapter(adapter);
    }


    private class AdvViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout adContainer;
        public AdvViewHolder(View itemView) {
            super(itemView);

            adContainer = (FrameLayout) itemView.findViewById(R.id.list_ad_container);
        }

        private void initView() {
            View adView = null;
            Log.d("TAG", "initView");
            if (AndroidSdk.hasNativeAd(TAG_AD, AndroidSdk.NATIVE_AD_TYPE_ALL)) {
                Log.d("TAG", "hasNativeAd");
                if (adView == null) {
                    Log.d("TAG", "adView == null");
                    adView = AndroidSdk.peekNativeAdViewWithLayout(TAG_AD, AndroidSdk.NATIVE_AD_TYPE_ALL, R.layout.native_ad, new ClientNativeAd.NativeAdClickListener() {
                        @Override
                        public void onNativeAdClicked(ClientNativeAd clientNativeAd) {

                        }
                    });
                }
                if (adView != null) {
                    Log.d("TAG", "adView != null");
                    adContainer.addView(adView);
                } else {
                    Log.d("TAG", "adView == null");
                }
            }
        }
    }


    private class RecordViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView timeView;
        private TextView durationView;
        private Button deleteButton;
        private Button renameButton;
        private RelativeLayout contentView;

        RecordViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);

            nameView = (TextView) itemView.findViewById(R.id.list_name);
            timeView = (TextView) itemView.findViewById(R.id.list_time);
            durationView = (TextView) itemView.findViewById(R.id.list_duration);
            renameButton = (Button) itemView.findViewById(R.id.list_rename);
            deleteButton = (Button) itemView.findViewById(R.id.list_delete);
            contentView = (RelativeLayout) itemView.findViewById(R.id.list_content);
        }

        private void initView(final int position) {
            final Record record = records.get(position);
            final File file = record.getFile();
            nameView.setText(file.getName().replace(".amr", ""));
            timeView.setText(switchTime(file.lastModified()));
            durationView.setText(switchDuration(record.getDuration()));
            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RenameDialog dialog = RenameDialog.newInstance(false, file.getName().replace(".amr", ""));
                    dialog.setOnButtonClickListener(new RenameDialog.OnButtonClickListener() {
                        @Override
                        public void onPositiveClick(File newFile) {
                            record.setFail(newFile);
                            adapter.notifyItemChanged(position + 1);
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show(getFragmentManager(), "rename");
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    file.delete();
                    records.remove(position);
                    adapter.notifyItemRemoved(position + 1);
                    adapter.notifyItemRangeChanged(position + 1, records.size());
                }
            });

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultPosition = position;
                    startActivityForResult(PlayActivity.newIntent(getActivity(), file.getName()), 0);
                }
            });
        }

        private String switchTime(long time) {
            Date date = new Date(time);
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            return format.format(date);
        }

        private String switchDuration(int totalMiniSecond) {
            int totalSecond = Math.round(totalMiniSecond / 1000f);
            int second = totalSecond % 60;
            int minute = totalSecond / 60;
            String secondString = second >= 10 ? second + "" : "0" + second;
            String minuteString = minute >= 10 ? minute + "" : "0" + minute;
            return minuteString + ":" + secondString;
        }
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int NORMAL = 1;
        private static final int HEADER = 2;

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? HEADER : NORMAL;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == HEADER ?
                    new AdvViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.record_list_item_adv, parent, false)) :
                    new RecordViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.record_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == 0) {
                ((AdvViewHolder) holder).initView();
            } else {
                ((RecordViewHolder) holder).initView(position - 1);
            }
        }

        @Override
        public int getItemCount() {
            return records.size() + 1;
        }
    }

/*    private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {


        @Override
        public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecordViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.record_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecordViewHolder holder, int position) {
            holder.initView(position);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }*/


}
