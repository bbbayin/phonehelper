package com.mob.sms.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.mob.sms.BuildConfig;
import com.mob.sms.bean.ChannelChargeBean;
import com.mob.sms.bean.CloudPermissionBean;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.BaseResponse;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FreeCheckUtils {

    public static boolean isSecretCall() {
        String sim = (String) SPUtils.get(SPConstant.SP_SIM_CARD_TYPE, "");
        return TextUtils.equals(sim, Constants.SIM_TYPE_SECRET);
    }

    public static void check(Activity activity,boolean isSecretCall, OnCheckCallback callback) {
        checkPermission(activity,isSecretCall, callback);
    }
    private static void checkPermission(Activity activity, boolean isSecretCall, OnCheckCallback callback) {
        // 先判断渠道
        RetrofitHelper.getApi().getMarketCharge(BuildConfig.FLAVOR)
                .subscribe(new Action1<BaseResponse<ChannelChargeBean>>() {
                    @Override
                    public void call(BaseResponse<ChannelChargeBean> response) {
                        if (response != null && response.data != null) {
                            switch (response.data.status) {
                                case "0":
                                    if (callback != null) {
                                        callback.onResult(true);
                                    }
//                                    Intent intent = new Intent(activity, SingleAutoTaskActivity.class);
//                                    intent.putExtra("type", "dhbd");
//                                    activity.startActivity(intent);
                                    break;
                                default:
                                    checkUserVip(activity, isSecretCall, callback);
                                    break;
                            }
                        }

                    }
                });
    }

    private static void checkUserVip(Activity activity,boolean isSecretCall, OnCheckCallback callback) {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CloudPermissionBean>() {
                    @Override
                    public void call(CloudPermissionBean permissionBean) {
                        if (permissionBean != null && "200".equals(permissionBean.code)) {
                            // 有权限
                            if (callback != null) {
                                callback.onResult(true);
                            }
//                            Intent intent = new Intent(activity, SingleAutoTaskActivity.class);
//                            intent.putExtra("type", "dhbd");
//                            activity.startActivity(intent);
                        } else if ("500".equals(permissionBean.code)) {
                            if (isSecretCall) {
                                ToastUtil.show(permissionBean.msg);
                                callback.onResult(false);
                            }else {
                                // 非隐私拨号不用拦截时间
                                callback.onResult(true);
                            }
                        } else {
                            if (callback != null) {
                                callback.onResult(false);
                            }
                            //activity.startActivity(new Intent(activity, VipActivity.class));
                        }
                    }
                });
    }

    public interface OnCheckCallback{
        void onResult(boolean free);
    }
}
