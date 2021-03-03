package com.mob.sms.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mob.sms.R;
import com.mob.sms.activity.MainActivity;

import cn.rs.keepalive.Helper;
import cn.rs.keepalive.Task;

public class KeepAlive {
    public static final String TAG = "KeepAlive";
    public static final int NOTIFICATIONID_FOR_SERVICE = 0x1f00012;
    public static final String CHANNEL_ID = "Mob";
    public static final String CHANNEL_NAME = "Mob";
    private static NotificationManager manager;

    private static Context mContext;

    private static NotificationManager getManager(Context context) {
        if (manager == null) {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public static void init(Context context) {
        mContext = context;
        startKeepAlive(context);
    }

    private static void startKeepAlive(Context context) {
        Helper.initialize(context, new Task() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
                Log.w(TAG, "Call onStop!");
            }
        }, buildNotification(context), NOTIFICATIONID_FOR_SERVICE);
        Helper.start();
        Log.w(TAG, "start keep alive");
    }


    @TargetApi(Build.VERSION_CODES.O)
    private static Notification buildNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            getManager(context).createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, -1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("")
                .setContentIntent(pendingIntent)
                .setDefaults(1);//跟随手机设置

        return builder.build();
    }

}