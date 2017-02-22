package com.danielkim.soundrecorder;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete, null);
        TextView okayView = (TextView) view.findViewById(R.id.delete_okay);
        TextView cancelView = (TextView) view.findViewById(R.id.delete_cancel);
        okayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
            }
        });
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);

        return dialog;
    }

    interface OnPositiveClickListener {
        void onClick();
    }

    public void setOnPositiveClickListener(OnPositiveClickListener listener) {
        this.listener = listener;
    }

}
