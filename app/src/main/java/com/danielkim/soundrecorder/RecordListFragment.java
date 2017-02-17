package com.danielkim.soundrecorder;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.autolayout.utils.AutoUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordListFragment extends Fragment {

//    private File[] files;
    private List<File> fileList;
    private RecordAdapter adapter;
    private File file;

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
//        List<File> files = new ArrayList<>();
        adapter = new RecordAdapter();
        fileList = new ArrayList<>();
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
//                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder");
                File folder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder");
                File[] files = folder.listFiles();
/*                for (int start = 0, end = files.length - 1; start < end; start++, end--) {
                    File temp = files[end];
                    files[end] = files[start];
                    files[start] = temp;
                }*/
                for (int i = files.length - 1; i >= 0; i--) {
                    fileList.add(files[i]);
                }
                Message msg = handler.obtainMessage();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }).start();


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
            fileList.remove(file);
            adapter.notifyDataSetChanged();
        }
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
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        rv.setAdapter(adapter);
    }


    private class RecordViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView timeView;
        private TextView durationView;
        private Button deleteButton;
        private LinearLayout contentView;

        RecordViewHolder(View itemView) {
            super(itemView);
            AutoUtils.autoSize(itemView);

            nameView = (TextView) itemView.findViewById(R.id.list_name);
            timeView = (TextView) itemView.findViewById(R.id.list_time);
            durationView = (TextView) itemView.findViewById(R.id.list_duration);
            deleteButton = (Button) itemView.findViewById(R.id.list_delete);
            contentView = (LinearLayout) itemView.findViewById(R.id.list_content);
        }

        private void initView(final int position) {
            file = fileList.get(position);

            nameView.setText(file.getName().replace(".mp3", ""));
            timeView.setText(switchTime(file.lastModified()));
            MediaPlayer player = MediaPlayer.create(getActivity(), Uri.fromFile(file));
            durationView.setText(switchDuration(player.getDuration()));
            player.release();

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    file.delete();
                    fileList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, fileList.size());
                }
            });

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
            int totalSecond = Math.round(totalMiniSecond / 1000);
            int second = totalSecond % 60;
            int minute = second / 60;
            String secondString = second >= 10 ? second + "" : "0" + second;
            String minuteString = minute >= 10 ? minute + "" : "0" + minute;
            return minuteString + ":" + secondString;
        }
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {


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
            return fileList.size();
        }
    }


}
