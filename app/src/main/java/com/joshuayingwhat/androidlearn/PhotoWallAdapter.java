package com.joshuayingwhat.androidlearn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
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
        //如果内存缓存中没有资源就执行下载操作
        if (MemoryLruCache.getInstance().getBitmapFromMemoryCache(imageThumbUrls[i]) == null) {

            CacheMemoryWorkerTask cacheMemoryWorkerTask = new CacheMemoryWorkerTask(viewHolder);
            cacheMemoryWorkerTask.execute(imageThumbUrls[i]);
            CacheMemoryWorkerTaskcollecation.add(cacheMemoryWorkerTask);
            Log.e("Tag", "网络请求资源");
        } else {
            viewHolder.grideIv.setImageBitmap(MemoryLruCache.getInstance().getBitmapFromMemoryCache(imageThumbUrls[i]));
            Log.e("Tag", "从缓存中获取");
        }
    }

    class CacheMemoryWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private ViewHolder viewHolder;
        private Bitmap bitmap;

        public CacheMemoryWorkerTask(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected Bitmap doInBackground(String... s) {
            try {
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
            if (bitmap != null) {
                viewHolder.grideIv.setImageBitmap(bitmap);
            }
            CacheMemoryWorkerTaskcollecation.remove(this);
        }
    }

    /**
     * 计算bitmap大小
     */
    public Bitmap decodeBitmapFromMemory(InputStream is, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        //该资源已经在内存中了
        BitmapFactory.decodeStream(is, new Rect(), options);
        options.inSampleSize = calculateBitmapSize(options, reqWidth, reqHeight);

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
}
