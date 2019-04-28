package com.joshuayingwhat.androidlearn;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * 内存缓存
 */
public class MemoryLruCache {

    private LruCache<String, Bitmap> memoryLruCache;

    static MemoryLruCache getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final MemoryLruCache INSTANCE = new MemoryLruCache();
    }


    /**
     * 初始化内存缓存
     */
    private MemoryLruCache() {
        //内存缓存大小
        int cacheMemeory = (int) ((Runtime.getRuntime().maxMemory() / 1024) / 8);
        memoryLruCache = new LruCache<String, Bitmap>(cacheMemeory) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * 添加资源到缓存中
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryLruCache.put(key, bitmap);
        } else {
            Log.e("Tag", "当前资源已经添加到内存缓存中了");
        }
    }


    /**
     * 获取缓存的内容
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return memoryLruCache.get(key);
    }
}
