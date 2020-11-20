package com.hisona.rxasynctask;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.hisona.rxasynctask.ResultListener.RESULT_FAIL;
import static com.hisona.rxasynctask.ResultListener.RESULT_PROGRESS;

public abstract class RxAsyncTask {

    private static final String TAG = RxAsyncTask.class.getSimpleName();

    protected WeakReference<Activity> mWeakReference;
    protected DisposableObserver<ProgressOrResult> mObserver;
    public ResultListener mResultListener;

    protected RxAsyncTask(Activity activity, ResultListener resultListener) {
        mWeakReference = new WeakReference<>(activity);
        mResultListener = resultListener;
    }

    protected String getString(int resourceId) {
        Activity activity = mWeakReference.get();
        return activity.getString(resourceId);
    }

    public static class ProgressOrResult {
        public int progress;
        public int result;
        public ProgressOrResult(int progress, int result) {
            this.progress = progress;
            this.result = result;
        }
    }

    abstract void onPreExecute();
    abstract void onProgressUpdate(Integer value);
    abstract void doInBackground(ObservableEmitter<ProgressOrResult> emitter);
    abstract void onPostExecute(ProgressOrResult result);

    protected void cancel() {
        if(mObserver != null && !mObserver.isDisposed()) {
            mObserver.dispose();
        }
    };

    public final boolean isCancelled() {
        return (mObserver == null || mObserver.isDisposed());
    }

    public void execute() {

        // call onPreExecute ...
        onPreExecute();

        Observable<ProgressOrResult> observable = Observable.create(this::doInBackground);

        mObserver = new DisposableObserver<ProgressOrResult>() {
            @Override
            public void onNext(@NonNull ProgressOrResult result) {

                if(result.result == RESULT_PROGRESS)
                    onProgressUpdate(result.progress);
                else if(result.result < 0)
                    onPostExecute(result);

                //Log.e(TAG, "onNext(" + result.progress + ", " + result.result + ")");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //Log.e(TAG, "onError()");
                onPostExecute(new ProgressOrResult(-1, RESULT_FAIL));
            }

            @Override
            public void onComplete() {
                //Log.e(TAG, "onComplete()");
                mObserver = null;
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mObserver);
    }

}

