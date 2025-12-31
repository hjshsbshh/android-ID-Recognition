package com.example.test;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class  MainActivity1020 extends AppCompatActivity implements View.OnClickListener {

    FragmentManager fm;
    Fragment f1, f2, f3;
    private MediaPlayer mediaPlayer; // 改为成员变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main1020);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化Fragment
        f1 = new Fragment1();
        f2 = new Fragment2(); // 假设您有Fragment2类
        f3 = new Fragment3(); // 假设您有Fragment3类

        // 设置点击监听
        findViewById(R.id.tvfragment1).setOnClickListener(this);
        findViewById(R.id.tvfragment2).setOnClickListener(this);
        findViewById(R.id.tvfragment3).setOnClickListener(this);

        fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.frame, f1)
                .add(R.id.frame, f2)
                .add(R.id.frame, f3)
                .hide(f2)
                .hide(f3)
                .commit();

        // 最后初始化音乐播放
        //playBackgroundMusic();
    }

    private void playBackgroundMusic() {
        try {
            // 创建 MediaPlayer 并设置音频资源
            mediaPlayer = MediaPlayer.create(this, R.raw.wohuainiande);

            if (mediaPlayer == null) {
                Log.e("MusicPlayer", "MediaPlayer创建失败");
                return;
            }

            // 设置错误监听器
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("MusicPlayer", "播放错误 - what: " + what + ", extra: " + extra);
                    return false;
                }
            });

            // 设置音量（0.0f - 1.0f）
            mediaPlayer.setVolume(0.7f, 0.7f);

            // 开始播放
            mediaPlayer.start();
            Log.d("MusicPlayer", "开始播放音乐");

            // 设置播放完成的监听器
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("MusicPlayer", "音乐播放完成");
                    // 如果需要循环播放，可以在这里重新开始
                    // mp.start();
                }
            });

        } catch (Exception e) {
            Log.e("MusicPlayer", "播放音乐异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void hideAllFragments() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(f1).hide(f2).hide(f3);
        ft.commit();
    }

    @Override
    public void onClick(View v) {
        hideAllFragments();

        FragmentTransaction ft = fm.beginTransaction();

        int id = v.getId();
        if (id == R.id.tvfragment1) {
            ft.show(f1);
        } else if (id == R.id.tvfragment2) {
            ft.show(f2);
        } else if (id == R.id.tvfragment3) {
            ft.show(f3);
        } else {
            ft.show(f1);
        }
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放 MediaPlayer 资源
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}