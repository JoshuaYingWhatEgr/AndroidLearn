package com.joshuayingwhat.androidlearn.threadtask;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.joshuayingwhat.androidlearn.R;

import java.lang.ref.WeakReference;

/**
 * 这里是处理bitmap在子线程中
 * todo 处理并发问题
 *
 * @author joshuayingwhat
 */
public class ThreadLoadingBitmapActivity extends AppCompatActivity {

    private ImageView threadIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_loading_bitmap);
        threadIv = (ImageView) findViewById(R.id.thread_loading_iv);

        //执行bitmap的asynctask
        loadBitmap(R.drawable.ic_launcher_background, threadIv);
//        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(threadIv);
//        bitmapWorkerTask.execute(R.mipmap.ic_launcher);
    }

    @SuppressLint("StaticFieldLeak")
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        //软引用
        WeakReference<ImageView> imageViewReference;

        private Integer imageId;

        public BitmapWorkerTask(ImageView threadIv) {
            imageViewReference = new WeakReference<>(threadIv);
        }

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            imageId = integers[0];
            return BitmapFactory.decodeResource(getResources(), imageId);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    /**
     * 处理多线程并发问题
     * 这个类主要是处理bitmapworkertask工作线程,同时在还没用图片的时候给定一个占位图
     */
    static class AsyncDrawable extends BitmapDrawable {
        final WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskWeakReference = new WeakReference<>(bitmapWorkerTask);
        }


        public BitmapWorkerTask getBitmapWorkerTAsk() {
            return bitmapWorkerTaskWeakReference.get();
        }
    }

    /**
     * 检查当前工作线程是否已经在执行
     */
    public static boolean cancelWorkerTask(int data, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            //当bitmapworkertask没有执行的data或者
            // 当前的data和工作线程中的data不一致时就将这个工作线程删除
            if (bitmapWorkerTask.imageId == 0 || data != bitmapWorkerTask.imageId) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTAsk();
            }
        }
        return null;
    }

    public void loadBitmap(int data, ImageView imageView) {
        if (cancelWorkerTask(data, imageView)) {
            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
            AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), bitmap, bitmapWorkerTask);
            imageView.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute(data);
        }
    }
}
