package com.example.webrtcrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class RecordService extends Service {
    public static final String CHANNEL_ID = "RECORD_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1001, new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("循环录制中")
                .setContentText("摄像头与外网服务运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "录制服务", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; 
    }
}
