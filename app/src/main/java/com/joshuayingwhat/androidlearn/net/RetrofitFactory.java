package com.joshuayingwhat.androidlearn.net;

import com.joshuayingwhat.androidlearn.BaseUrl;
import com.joshuayingwhat.androidlearn.net.interceptor.HttpInterceptorLogger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求功能
 */
public class RetrofitFactory {

    private Retrofit retrofit;

    public static RetrofitFactory getInstance() {
        return RetrofitHolder.INSTANCE;
    }

    private static class RetrofitHolder {
        private static final RetrofitFactory INSTANCE = new RetrofitFactory();
    }


    private RetrofitFactory() {
        //初始化okhttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(20000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .addInterceptor(HttpInterceptorLogger.getHttpLogging().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().client(okHttpClient)
                    .baseUrl(BaseUrl.base_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }


}
