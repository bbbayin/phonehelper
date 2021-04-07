package com.mob.sms.application;

import android.app.Application;
import android.content.Context;

import com.mob.sms.utils.KeepAlive;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

public class MyApplication extends Application {
    private static Application mApplication;
    public static IWXAPI wxApi;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        KeepAlive.init(this);

        UMConfigure.init(this, "5feb38da44bb94418a6b06e2", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin("wx22e1a70e838f3267", "42e372330366d521a57ed99bcb350b5b");
        PlatformConfig.setQQZone("101924228", "1166dd0fd38327bb8f4da43276b8865f");
        PlatformConfig.setQQFileProvider("com.mob.sms.fileprovider");

        wxApi = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        wxApi.registerApp("wx5fe8deafb48e5513");
    }

    public static Context getContext(){
        return mApplication;
    }
}
