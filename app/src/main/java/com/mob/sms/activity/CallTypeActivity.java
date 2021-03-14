package com.mob.sms.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.CloudPermissionBean;
import com.mob.sms.dialog.CheckTipDialog;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.utils.Constants;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 拨打方式
 */
public class CallTypeActivity extends BaseActivity {
    @BindView(R.id.gou_iv)
    ImageView mGouIv;
    @BindView(R.id.gou_iv2)
    ImageView mGouIv2;
    @BindView(R.id.gou_iv3)
    ImageView mGouIv3;
    @BindView(R.id.sim_rl2)
    RelativeLayout mSimRl2;
    @BindView(R.id.divider2)
    View mDivider2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_type);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));

        TelephonyManager manager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int numSlots = manager.getPhoneCount();
            if (numSlots < 2) {
                mSimRl2.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
            }
        }
        if ("sim1".equals(SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, "sim1"))) {
            mGouIv.setVisibility(View.VISIBLE);
            mGouIv2.setVisibility(View.GONE);
            mGouIv3.setVisibility(View.GONE);
        } else if ("sim2".equals(SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, "sim1"))) {
            mGouIv.setVisibility(View.GONE);
            mGouIv2.setVisibility(View.VISIBLE);
            mGouIv3.setVisibility(View.GONE);
        } else if ("ysh".equals(SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, "sim1"))) {
            mGouIv.setVisibility(View.GONE);
            mGouIv2.setVisibility(View.GONE);
            mGouIv3.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.back, R.id.sim_rl, R.id.sim_rl2, R.id.ysh_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.sim_rl:
                mGouIv.setVisibility(View.VISIBLE);
                mGouIv2.setVisibility(View.GONE);
                mGouIv3.setVisibility(View.GONE);
                SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.sim_rl2:
                mGouIv.setVisibility(View.GONE);
                mGouIv2.setVisibility(View.VISIBLE);
                mGouIv3.setVisibility(View.GONE);
                SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_2);
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.ysh_rl:
                Intent intent = new Intent(this, SetSecretInfoActivity.class);
                startActivityForResult(intent, 1234);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            enableCloudCall();
        }
    }

    private void enableCloudCall() {
        mGouIv.setVisibility(View.GONE);
        mGouIv2.setVisibility(View.GONE);
        mGouIv3.setVisibility(View.VISIBLE);
        SPUtils.put(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SECRET);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void cloudDialCheck() {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CloudPermissionBean>() {
                    @Override
                    public void call(CloudPermissionBean permissionBean) {
                        // TODO: 2021/3/13  test
                        if (permissionBean != null) {
                            permissionBean.code = "200";
                            if ("200".equals(permissionBean.code)) {
                                enableCloudCall();
                            }else {
                                CheckTipDialog dialog = new CheckTipDialog(CallTypeActivity.this);
                                dialog.setContent(permissionBean.msg);
                                dialog.show();
                            }
                        }else {
                            CheckTipDialog dialog = new CheckTipDialog(CallTypeActivity.this);
                            dialog.setContent("隐私拨打不能使用，您还未购买套餐");
                            dialog.show();
                        }
                    }
                });
    }
}
