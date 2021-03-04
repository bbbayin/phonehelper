package com.mob.sms.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mob.sms.R;
import com.mob.sms.adapter.SplashBannerAdapter;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.SplashBean;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends Activity {
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.skip)
    ImageView mSkip;
    @BindView(R.id.welcome_rl)
    RelativeLayout mWelcomeRl;

    private String[] mPermissions = new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_CONTACTS,Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE};
    private ArrayList<String> mPermissionList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        for (int i = 0; i < mPermissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, mPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(mPermissions[i]);//添加还未授予的权限
            }
        }

        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, mPermissions, 1);
        }
        initView();

        initGlobalData();
    }

    private void initGlobalData() {
        // 批量拨打，随机间隔
        int interval = SPUtils.getInt(SPConstant.SP_CALL_JGSZ, 0);
        if (interval == 0) {
            // 默认随机30
            SPUtils.put(SPConstant.SP_CALL_JGSZ, -30);
        }
    }

    private void initView() {
        if (SPUtils.getBoolean(SPConstant.SP_SPLASH_WELCOME, false)) {
            mWelcomeRl.setVisibility(View.VISIBLE);
            mBanner.setVisibility(View.GONE);
            mSkip.setVisibility(View.GONE);
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            List<String> datas = new ArrayList<>();
            datas.add("");
            datas.add("");
            datas.add("");
            List<SplashBean> lists = new ArrayList<>();
            lists.add(new SplashBean(R.mipmap.guide1, ""));
            lists.add(new SplashBean(R.mipmap.guide2, ""));
            lists.add(new SplashBean(R.mipmap.guide3, ""));
            mBanner.setAdapter(new SplashBannerAdapter(this, datas, lists))
                    .setIndicator(new CircleIndicator(this))
                    .setIndicatorRadius(0).start();

            mSkip.setOnClickListener(view -> {
                SPUtils.put(SPConstant.SP_SPLASH_WELCOME, true);
                mWelcomeRl.setVisibility(View.VISIBLE);
                mBanner.setVisibility(View.GONE);
                mSkip.setVisibility(View.GONE);
                mHandler.sendEmptyMessageDelayed(0, 2000);
            });
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""))) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                    break;
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionDenied = false;//有权限没有通过
        if (1 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionDenied = true;
                }
            }
            if (permissionDenied) {
                ToastUtil.show("权限受限，退出APP");
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
