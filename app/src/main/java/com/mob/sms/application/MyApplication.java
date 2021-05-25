package com.mob.sms.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.mob.sms.utils.ChannelUtil;
import com.mob.sms.utils.KeepAlive;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MyApplication extends Application {
    private static Application mApplication;
    public static IWXAPI wxApi;
    public static String Channel = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        Channel = ChannelUtil.getChannel(this);
        System.out.println("渠道好："+Channel);
        KeepAlive.init(this);

        UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, "5feb38da44bb94418a6b06e2", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");

//        UMConfigure.init(this, "6099327553b6726499f68bb7", Channel, UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin("wx22e1a70e838f3267", "42e372330366d521a57ed99bcb350b5b");
        PlatformConfig.setQQZone("101924228", "1166dd0fd38327bb8f4da43276b8865f");
        PlatformConfig.setQQFileProvider("com.mob.sms.fileprovider");

        wxApi = WXAPIFactory.createWXAPI(this, null);
        // 将该app注册到微信
        wxApi.registerApp("wx22e1a70e838f3267");
    }

    public static Context getContext(){
        return mApplication;
    }

    public static String getChannel(Context context) {
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith("mtchannel")) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String[] split = ret.split("_");
        if (split != null && split.length >= 2) {
            return ret.substring(split[0].length() + 1);

        } else {
            return "";
        }
    }
}
