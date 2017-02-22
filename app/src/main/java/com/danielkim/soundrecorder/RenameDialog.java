package com.danielkim.soundrecorder;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
//import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

/**
 * Created by IVY on 2017/2/20.
 */

public class RenameDialog extends DialogFragment {

    private OnButtonClickListener listener;

    public static RenameDialog newInstance(boolean save, String name) {
        RenameDialog dialog = new RenameDialog();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putBoolean("save", save);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rename, null);
        TextView cancelView = (TextView) view.findViewById(R.id.rename_cancel);
        TextView okayView = (TextView) view.findViewById(R.id.rename_okay);
        if (getArguments().getBoolean("save", false)) {
            cancelView.setText("Delete");
            okayView.setText("Save");
        }
        final EditText nameView = (EditText) view.findViewById(R.id.rename_name);
        final String oldName = getArguments().getString("name");
        nameView.setText(oldName);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNegativeClick();
                }
                dismiss();
            }
        });
        okayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getActivity().startService(RenameService.newIntent(getActivity(), oldName, nameView.getText().toString()));
//                File file = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + oldName + ".amr");
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + oldName + ".amr");
                if (file.exists()) {
//                    file.renameTo(new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + nameView.getText().toString() + ".amr"));
                    file.renameTo(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + nameView.getText().toString() + ".amr"));
                }
                if (listener != null) {
//                    listener.onPositiveClick(new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + nameView.getText().toString() + ".amr"));\
                    listener.onPositiveClick(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceRecorder/" + nameView.getText().toString() + ".amr"));
                }
                dismiss();
            }
        });

//        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.RenameDialogStyle))
        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.RenameDialogStyle))
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    interface OnButtonClickListener {
        void onPositiveClick(File file);
        void onNegativeClick();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }
}
