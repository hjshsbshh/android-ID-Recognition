package com.example.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Service_Activity extends AppCompatActivity {
    Button button1,button2,button3,button4;
    TextView textView8,textView9;
    MyService2.PlayerBinder binder;
    int[] songs={
            R.raw.wohuainiande,
    };

    BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        //播放
        button1=findViewById(R.id.button5);
        //暂停
        button2=findViewById(R.id.button6);
        button3=findViewById(R.id.button7);
        button4=findViewById(R.id.button8);
        Intent intent=new Intent(this,MyService1.class);
        intent.putExtra("qishi",songs[2]);
        Intent intent2=new Intent(this,MyService2.class);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
            }
        });

        textView8=findViewById(R.id.textView8);
        textView9=findViewById(R.id.textView9);

        //授权语句
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS}, 100);

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent intent1=new Intent(context,MyService1.class);
                context.startService(intent1);
            }
        };
        IntentFilter filter1=new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver,filter1);


        ServiceConnection connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder =(MyService2.PlayerBinder)service;
                binder.mybiner();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder=null;
            }

        };
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bindService(intent2,connection,BIND_AUTO_CREATE);

            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                unbindService(connection);
            }
        });
        };

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        if (code == 100 && r[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限通过
        }
    }


}
