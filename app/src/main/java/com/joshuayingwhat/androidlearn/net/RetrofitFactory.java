package com.joshuayingwhat.androidlearn.net;

import android.text.TextUtils;

import com.joshuayingwhat.androidlearn.BaseUrl;
import com.joshuayingwhat.androidlearn.net.interceptor.HttpInterceptorLogger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求功能
 *
 * @author joshuayingwhat
 */
public class RetrofitFactory {

    private Retrofit.Builder retrofit;
    private Retrofit build;

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
                    .addConverterFactory(GsonConverterFactory.create());
        }

        if (!TextUtils.isEmpty(BaseUrl.base_url)) {
            build = retrofit.baseUrl(BaseUrl.base_url).build();
        }
    }

    public <T> T create(Class<T> clazz) {
        checkNotNull(build);
        return build.create(clazz);
    }

    private <T> void checkNotNull(T object) {
        if (object == null) {
            throw new NullPointerException("BaseUrl为空,请先配置url参数!");
        }
    }


}
