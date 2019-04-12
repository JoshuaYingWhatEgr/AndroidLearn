package com.joshuayingwhat.androidlearn.net.interceptor;

import android.util.Log;

import okhttp3.logging.HttpLoggingInterceptor;

public class HttpInterceptorLogger {

    public static HttpLoggingInterceptor getHttpLogging() {
        return new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("netdata", message);
            }
        });
    }
}
