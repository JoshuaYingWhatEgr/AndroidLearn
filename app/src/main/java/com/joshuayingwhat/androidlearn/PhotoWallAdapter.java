package com.joshuayingwhat.androidlearn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * 处理加载图片并发问题Concurrency
 *
 * @author joshuayingwhat
 */
public class PhotoWallAdapter extends RecyclerView.Adapter<PhotoWallAdapter.ViewHolder> {

    private Set<CacheMemoryWorkerTask> CacheMemoryWorkerTaskcollecation;

    private String[] imageThumbUrls;
    private Context mContext;

    PhotoWallAdapter(Context context, String[] imageThumbUrls) {
        mContext = context;
        this.imageThumbUrls = imageThumbUrls;
        CacheMemoryWorkerTaskcollecation = new HashSet<CacheMemoryWorkerTask>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_photo_wall, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        //这里处理图片 1.从网络下载图片 2.缓存(内存缓存和硬盘缓存)
        loadBitMap(viewHolder, i);
    }

    @Override
    public int getItemCount() {
        return imageThumbUrls.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView grideIv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            grideIv = (ImageView) itemView.findViewById(R.id.gride_iv);
        }
    }

    private void loadBitMap(ViewHolder viewHolder, int i) {
        //处理并发问题
        if (cancelPreviousTask(imageThumbUrls[i], viewHolder.grideIv)) {
            //如果内存缓存中没有资源就执行下载操作
            if (MemoryLruCache.getInstance().getBitmapFromMemoryCache(imageThumbUrls[i]) == null) {

                CacheMemoryWorkerTask cacheMemoryWorkerTask = new CacheMemoryWorkerTask(viewHolder, viewHolder.grideIv);
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.empty_photo);
                AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), bitmap, cacheMemoryWorkerTask);
                viewHolder.grideIv.setImageDrawable(asyncDrawable);
                cacheMemoryWorkerTask.execute(imageThumbUrls[i]);
                CacheMemoryWorkerTaskcollecation.add(cacheMemoryWorkerTask);
                Log.e("Tag", "网络请求资源");
            } else {
                viewHolder.grideIv.setImageBitmap(MemoryLruCache.getInstance().getBitmapFromMemoryCache(imageThumbUrls[i]));
                Log.e("Tag", "从缓存中获取");
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    class CacheMemoryWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private ViewHolder viewHolder;
        private Bitmap bitmap;
        private WeakReference<ImageView> imageViewWeakReference;
        private String s1;

        CacheMemoryWorkerTask(ViewHolder viewHolder, ImageView imageView) {
            this.viewHolder = viewHolder;
            imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... s) {
            try {
                s1 = s[0];
                URL url = new URL(s[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                int contentLength = urlConnection.getContentLength();
                InputStream is = urlConnection.getInputStream();
                bitmap = decodeBitmapFromMemory(is, 50, 50);
                //将bitmap加入缓存
                if (bitmap != null) {
                    MemoryLruCache.getInstance().addBitmapToMemoryCache(s[0], bitmap);
                }
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                if (imageViewWeakReference.get() != null) {
                    CacheMemoryWorkerTask cacheMemoryWorkerTask = getCacheMemoryWorkerTask(imageViewWeakReference.get());
                    if (this == cacheMemoryWorkerTask) {
                        imageViewWeakReference.get().setImageBitmap(bitmap);
                    }
                }

            }
            CacheMemoryWorkerTaskcollecation.remove(this);
        }
    }

    /**
     * 计算bitmap大小
     */
    private Bitmap decodeBitmapFromMemory(InputStream is, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        //该资源已经在内存中了
        BitmapFactory.decodeStream(is, new Rect(), options);

        options.inSampleSize = calculateBitmapSize(options, reqWidth, reqHeight);

        //系统默认是ARGB_8888每个像素占4个字节
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;

        return BitmapFactory.decodeStream(is, new Rect(), options);
    }

    private int calculateBitmapSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        int outHeight = options.outHeight;

        int outWidth = options.outWidth;

        int inSampleSize = 1;

        if (outHeight > reqHeight || outWidth > reqWidth) {
            final int halfWidth = outWidth / 2;
            final int halfHeight = outHeight / 2;

            while ((halfWidth / inSampleSize) >= reqWidth && (halfHeight / inSampleSize) >= reqHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 解决并发问题
     */
    private boolean cancelPreviousTask(String resId, ImageView imageView) {
        //获取到当前视图的task
        CacheMemoryWorkerTask cacheMemoryWorkerTask = getCacheMemoryWorkerTask(imageView);
        if (cacheMemoryWorkerTask != null) {
            if (!cacheMemoryWorkerTask.s1.equals(resId)) {
                cacheMemoryWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static class AsyncDrawable extends BitmapDrawable {
        private WeakReference<CacheMemoryWorkerTask> cacheMemoryWorkerTaskWeakReference = null;

        AsyncDrawable(Resources resources, Bitmap bitmap, CacheMemoryWorkerTask cacheMemoryWorkerTask) {
            super(resources, bitmap);
            cacheMemoryWorkerTaskWeakReference = new WeakReference(cacheMemoryWorkerTask);
        }

        CacheMemoryWorkerTask getCacheMemoryWorkerTask() {
            return cacheMemoryWorkerTaskWeakReference.get();
        }
    }

    /**
     * 检索asynctask是否被分配到指定的imageview
     *
     * @param imageView
     * @return
     */
    private static CacheMemoryWorkerTask getCacheMemoryWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getCacheMemoryWorkerTask();
            }
        }
        return null;
    }
}
