package com.hisona.rxasynctask;

import android.os.Bundle;

public interface ResultListener {

    int RESULT_PROGRESS     =  3;
    int RESULT_RETRY        =  2;
    int RESULT_START        =  1;
    int RESULT_SUCCESS      = -1;
    int RESULT_FAIL         = -2;
    int RESULT_TIMEOUT      = -3;
    int RESULT_CANCEL       = -4;

    void onTaskTerminate(int requestCode, int resultCode, Bundle result);
}
