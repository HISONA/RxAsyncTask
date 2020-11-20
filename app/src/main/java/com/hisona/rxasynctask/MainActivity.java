package com.hisona.rxasynctask;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.rxjava3.core.ObservableEmitter;

public class MainActivity extends AppCompatActivity implements ResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int BACKGROUND_TASK = 1;

    Button startButton, cancelButton;
    TextView outText;
    BackgroundTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start_button);
        cancelButton = findViewById(R.id.cancel_button);
        outText = findViewById(R.id.out_text);

        mTask = new BackgroundTask(this, this, BACKGROUND_TASK);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTask != null)
                    mTask.execute();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTask != null)
                    mTask.cancel();
            }
        });

    }

    @Override
    public void onTaskTerminate(int requestCode, int resultCode, Bundle result) {
        Log.i(TAG, "onTaskTerminate");

        if(requestCode == BACKGROUND_TASK) {

            switch (resultCode) {
                case RESULT_CANCEL:
                    outText.setText("Result: BackgroundTask cancelled.");
                    break;
                case RESULT_TIMEOUT:
                    outText.setText("Result: BackgroundTask timeout.");
                    break;
                case RESULT_FAIL:
                    outText.setText("Result: BackgroundTask failed.");
                    break;
                case RESULT_SUCCESS:
                    String retStr = result.getString("RESULT");
                    outText.setText("Result: BackgroundTask success. return: " + retStr);
                    break;
            }
        }

    }

    private class BackgroundTask extends RxAsyncTask {

        ProgressDialog mProgressDialog;
        int mRequestCode;
        String mResultString;

        protected BackgroundTask(Activity activity, ResultListener resultListener, int requestCode) {
            super(activity, resultListener);
            mRequestCode = requestCode;
        }

        @Override
        protected void cancel() {
            super.cancel();

            Bundle bundle = new Bundle();

            mResultListener.onTaskTerminate(mRequestCode, RESULT_CANCEL, bundle);
        }

        @Override
        void onPreExecute() {
            Log.i(TAG, "onPreExecute");

            Activity activity = mWeakReference.get();
            if(activity == null || activity.isFinishing()) return;

            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setTitle(activity.getString(R.string.background_task));
            mProgressDialog.setMessage("");

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.i(TAG, "mProgressDialog:onCancel");
                    cancel();
                }
            });

            mProgressDialog.show();
        }

        @Override
        void onProgressUpdate(Integer value) {
            Log.i(TAG, "onProgressUpdate");
            mProgressDialog.setProgress(value);
        }

        @Override
        void doInBackground(ObservableEmitter<ProgressOrResult> emitter) {
            Log.i(TAG, "doInBackground");

            Activity activity = mWeakReference.get();
            if(activity == null || activity.isFinishing())
                return;

            int i;
            for(i=0; i<=10; i++) {
                emitter.onNext(new ProgressOrResult(i*10, RESULT_PROGRESS));
                SystemClock.sleep(1000);
            }

            // Do not do any work after Background Task is cancelled.
            if(this.isCancelled()) return;

            mResultString = Integer.toString(i);
            emitter.onNext(new ProgressOrResult(-1, RESULT_SUCCESS));
            emitter.onComplete();
        }

        @Override
        void onPostExecute(ProgressOrResult result) {
            Log.i(TAG, "onPostExecute");

            Activity activity = mWeakReference.get();
            if(activity == null || activity.isFinishing())
                return;

            mProgressDialog.dismiss();

            int resultCode = result.result;

            Bundle bundle = new Bundle();
            bundle.putString("RESULT", mResultString);

            mResultListener.onTaskTerminate(mRequestCode, resultCode, bundle);
        }
    }

}