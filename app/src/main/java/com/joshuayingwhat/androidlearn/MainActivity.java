package com.joshuayingwhat.androidlearn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.joshuayingwhat.androidlearn.cache.DiskLruCacheActivity;
import com.joshuayingwhat.androidlearn.cache.LruActivity;
import com.joshuayingwhat.androidlearn.threadtask.ThreadLoadingBitmapActivity;

/**
 * 这个是一个android知识的测试项目
 *
 * @author joshuayingwhat
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button lruBtn = (Button) findViewById(R.id.lru_btn);
        Button diskLruBtn = (Button) findViewById(R.id.disk_lru_btn);
        Button currentBtn = (Button) findViewById(R.id.concurrence_btn);
        Button photonWallBtn = (Button) findViewById(R.id.photo_wall);
        diskLruBtn.setOnClickListener(this);
        lruBtn.setOnClickListener(this);
        currentBtn.setOnClickListener(this);
        photonWallBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lru_btn:
                Intent intent = new Intent(this, LruActivity.class);
                startActivity(intent);
                break;
            case R.id.disk_lru_btn:
                Intent intent1 = new Intent(this, DiskLruCacheActivity.class);
                startActivity(intent1);
                break;
            case R.id.concurrence_btn:
                Intent intent2 = new Intent(this, ThreadLoadingBitmapActivity.class);
                startActivity(intent2);
                break;
            case R.id.photo_wall:
                Intent intent3 = new Intent(this, PhotoWallActivity.class);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }
}
