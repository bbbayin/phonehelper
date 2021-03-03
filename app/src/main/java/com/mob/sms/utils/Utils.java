package com.mob.sms.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static boolean isNetworkAvailable(Context context) {

        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isAvailable();
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    public static String getYear(){
        long time = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(time));
        if(date.contains("-")){
            return date.split("-")[0];
        }else {
            return "2021";
        }
    }

    public static long getTime(String str) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        try {
            Date date = df.parse(str);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
