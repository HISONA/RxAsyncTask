package com.hisona.rxasynctask;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


public class ProgressDialog {
    private static final String TAG = ProgressDialog.class.getSimpleName();

    private AlertDialog mDialog;
    private ProgressBar mProgress;
    private TextView mText;

    private DialogInterface.OnCancelListener mListener;

    public ProgressDialog(@NonNull Activity activity) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.progress_dialog, null);

        mProgress = view.findViewById(R.id.progress);
        mText = view.findViewById(R.id.text);

        mProgress.setProgress(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("");
        builder.setMessage("");
        builder.setView(view);
        mDialog = builder.create();
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(false);

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(mListener != null)
                    mListener.onCancel(dialogInterface);
            }
        });

    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mListener = listener;
    }

    public void setTitle(String title) {
        mDialog.setTitle(title);
    }

    public void setMessage(String message) {
        mDialog.setMessage(message);
    }

    public void setProgress(int value) {
        mProgress.setProgress(value);
        mText.setText(value + "%");
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

}

