package com.fourteenelevendev.android.apps.ioexperiment.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fourteenelevendev.android.apps.ioexperiment.BuildConfig;

public class LogUtils {
    public static void LOGE(final String tag, String message) {
    	if (BuildConfig.DEBUG && !TextUtils.isEmpty(message)) {
    		Log.e(tag, message);
    	}
    }

    private LogUtils() {
    }
}
