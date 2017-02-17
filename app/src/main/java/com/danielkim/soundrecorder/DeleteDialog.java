package com.danielkim.soundrecorder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import java.io.File;


/**
 * Created by IVY on 2017/2/17.
 */

public class DeleteDialog extends DialogFragment {

    private OnPositiveClickListener listener;

    public static DeleteDialog newInstance() {
        DeleteDialog deleteDialog = new DeleteDialog();
//        Bundle args = new Bundle();
//        args.putString("name", name);
//        deleteDialog.setArguments(args);
        return deleteDialog;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete, null));
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                String name = getArguments().getString("name");
//                new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/VoiceRecorder/" + name).delete();
                if (listener != null) {
                    listener.onClick();
                }
            }
        });
        AlertDialog dialog = builder.create();
//        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
//        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    interface OnPositiveClickListener {
        void onClick();
    }

    public void setOnPositiveClickListener(OnPositiveClickListener listener) {
        this.listener = listener;
    }

}
