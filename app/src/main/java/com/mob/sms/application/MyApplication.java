package com.mob.sms.application;

import android.app.Application;
import android.content.Context;

import com.mob.sms.utils.KeepAlive;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

public class MyApplication extends Application {
    private static Application mApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        KeepAlive.init(this);

        UMConfigure.init(this, "5feb38da44bb94418a6b06e2", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin("wx22e1a70e838f3267", "42e372330366d521a57ed99bcb350b5b");
        PlatformConfig.setQQZone("101924228", "1166dd0fd38327bb8f4da43276b8865f");
        PlatformConfig.setQQFileProvider("com.mob.sms.fileprovider");
    }

    public static Context getContext(){
        return mApplication;
    }
}
