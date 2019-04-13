package com.joshuayingwhat.androidlearn.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.joshuayingwhat.androidlearn.R;
import com.joshuayingwhat.androidlearn.api.RetrofitServer;
import com.joshuayingwhat.androidlearn.bean.CategoryResult;
import com.joshuayingwhat.androidlearn.net.RetrofitFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 内存缓存功能
 *
 * @author joshuayingwhat
 */
public class LruActivity extends AppCompatActivity implements View.OnClickListener {

    private LruCache<String, Bitmap> lruCache;
    private ImageView urlIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lru);
        urlIv = (ImageView) findViewById(R.id.url_iv);
        Button lruBtn = (Button) findViewById(R.id.show_lru_btn);
        Button intentBtn = (Button) findViewById(R.id.intent_btn);
        ImageView lruIv = (ImageView) findViewById(R.id.lru_iv);

        lruBtn.setOnClickListener(this);
        intentBtn.setOnClickListener(this);

        //获取系统运行内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //设置缓存大小
        final int cacheSize = maxMemory / 8;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void acquireBitmap() {
        //首先请求网络获取图片资源
        if (getBitmapFormCache("100") == null) {
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
        } else {
            Log.e("tag", "从缓存中提取!");
            urlIv.setImageBitmap(getBitmapFormCache("100"));
        }
    }

    /**
     * 将bitmap设置到缓存中
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (lruCache != null) {
            if (getBitmapFormCache(key) == null) {
                lruCache.put(key, bitmap);
            }
            Log.e("tag", "图片已经加入缓存了");
        }
    }

    /**
     * 从缓存中取出bitmap
     */
    public Bitmap getBitmapFormCache(String key) {
        return lruCache.get(key);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_lru_btn:
                urlIv.setImageBitmap(getBitmapFormCache("100"));
                break;
            case R.id.intent_btn:
                acquireBitmap();
                break;
            default:
                break;
        }

    }

    public void newThread(String urls) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(urls);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    int contentLength = urlConnection.getContentLength();
                    InputStream is = urlConnection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    //将bitmap加入缓存
                    addBitmapToMemoryCache("100", bitmap);
                    is.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            urlIv.setImageBitmap(bitmap);
                        }
                    });
//
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
