package com.joshuayingwhat.androidlearn.threadtask;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.joshuayingwhat.androidlearn.R;

import java.lang.ref.WeakReference;

/**
 * 这里是处理bitmap在子线程中
 *
 * @author joshuayingwhat
 */
public class ThreadLoadingBitmap extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_loading_bitmap);
        ImageView threadIv = (ImageView) findViewById(R.id.thread_loading_iv);

        //执行bitmap的asynctask
        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(threadIv);
        bitmapWorkerTask.execute(R.mipmap.ic_launcher);
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
}
