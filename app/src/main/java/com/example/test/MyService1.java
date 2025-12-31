package com.example.test;

import static android.content.Intent.getIntent;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MyService1 extends Service {
    MediaPlayer mediaPlayer;
    public MyService1() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int song=intent.getIntExtra("qishi",6666666);
        if(mediaPlayer!=null)mediaPlayer.stop();
        mediaPlayer=MediaPlayer.create(this,R.raw.wohuainiande);
        mediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}