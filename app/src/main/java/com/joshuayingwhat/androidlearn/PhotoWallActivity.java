package com.joshuayingwhat.androidlearn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * 照片墙：主要目的是将之前学习的内容结合lrucache和disklrucach两种缓存方式
 * 实现一个照片墙效果
 *
 * @author joshuayingwhat
 */
public class PhotoWallActivity extends AppCompatActivity {

    private RecyclerView photoWallRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_wall);
        photoWallRv = (RecyclerView) findViewById(R.id.photo_wall);
        //初始化rv
        photoWallRv.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        PhotoWallAdapter photoWallAdapter = new PhotoWallAdapter(this, Image.imageThumbUrls);
        photoWallRv.setAdapter(photoWallAdapter);
    }
}
