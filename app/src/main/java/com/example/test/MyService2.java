package com.example.test;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MyService2 extends Service {
    MediaPlayer mp;
    public MyService2() {
    }

    @Override
    public void onDestroy() {
        mp.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return (IBinder) new PlayerBinder();
    }
    public  class  PlayerBinder extends Binder{
        public void mybiner(){
            mp= MediaPlayer.create(getApplicationContext(),R.raw.wohuainiande);
        }
    }


}