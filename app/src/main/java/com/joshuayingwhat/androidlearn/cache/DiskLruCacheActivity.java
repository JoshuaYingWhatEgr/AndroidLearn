package com.joshuayingwhat.androidlearn.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.joshuayingwhat.androidlearn.R;
import com.joshuayingwhat.androidlearn.api.RetrofitServer;
import com.joshuayingwhat.androidlearn.bean.CategoryResult;
import com.joshuayingwhat.androidlearn.net.RetrofitFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * 磁盘缓存
 *
 * @author joshuayingwhat
 */
public class DiskLruCacheActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DISK_CACHE_SUBDIR = "thumbails";

    private static final int DISK_CACHE_SIZE = 10 * 1024 * 1024;
    private DiskLruCache mDiskLruCache;
    private ImageView diskCacheIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disk_lrucache);
        Button diskCacheBtn = (Button) findViewById(R.id.disk_cache_btn);
        diskCacheBtn.setOnClickListener(this);
        diskCacheIv = (ImageView) findViewById(R.id.disk_cache_iv);
        //初始化磁盘缓存
        //首先设置磁盘缓存的地址
        File diskLruCacheDir = getDiskLruCacheDir(this);
        try {
            mDiskLruCache = DiskLruCache.open(diskLruCacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //网络请求图片
        acquireBitmap();
    }

    private File getDiskLruCacheDir(Context context) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !isExternalStorageRemovable() ? Objects.requireNonNull(getExternalCacheDir()).getPath() : context.getCacheDir().getPath();
        return new File(cachePath + File.separator + DiskLruCacheActivity.DISK_CACHE_SUBDIR);
    }

    private void acquireBitmap() {
        //首先请求网络获取图片资源
        Call<CategoryResult> randomBeauties = RetrofitFactory.getInstance().create(RetrofitServer.class).getRandomBeauties(1);
        randomBeauties.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(Call<CategoryResult> call, Response<CategoryResult> categoryResultResponse) {
                if (categoryResultResponse.body() != null && categoryResultResponse.body().results != null && categoryResultResponse.body().results.size() > 0) {
                    for (int i = 0; i < categoryResultResponse.body().results.size(); i++) {
                        CategoryResult.ResultsBean resultsBean = categoryResultResponse.body().results.get(i);
                        newThread(resultsBean.url);
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    public void newThread(String urls) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    //将bitmap加入硬盘缓存
                    DiskLruCache.Editor edit = mDiskLruCache.edit("100");
                    OutputStream os = edit.newOutputStream(0);
                    URL url = new URL(urls);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    int contentLength = urlConnection.getContentLength();
                    BufferedInputStream bis = new BufferedInputStream(urlConnection.getInputStream(), contentLength);
                    BufferedOutputStream bos = new BufferedOutputStream(os, contentLength);
                    int b;
                    while ((b = bis.read()) != -1) {
                        bos.write(b);
                    }
                    bos.flush();

                    bos.close();
                    bis.close();

                    edit.commit();
                    mDiskLruCache.flush();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public void onClick(View v) {
        //获取磁盘缓存中的图片
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get("100");
            InputStream inputStream = snapshot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            diskCacheIv.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
