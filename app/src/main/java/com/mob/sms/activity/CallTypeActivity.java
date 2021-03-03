package com.mob.sms.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
        if ("sim1".equals(SPUtils.getString(SPConstant.SP_CALL_TYPE, "sim1"))) {
            mGouIv.setVisibility(View.VISIBLE);
            mGouIv2.setVisibility(View.GONE);
            mGouIv3.setVisibility(View.GONE);
        } else if ("sim2".equals(SPUtils.getString(SPConstant.SP_CALL_TYPE, "sim1"))) {
            mGouIv.setVisibility(View.GONE);
            mGouIv2.setVisibility(View.VISIBLE);
            mGouIv3.setVisibility(View.GONE);
        } else if ("ysh".equals(SPUtils.getString(SPConstant.SP_CALL_TYPE, "sim1"))) {
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
                SPUtils.put(SPConstant.SP_CALL_TYPE, "sim1");
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.sim_rl2:
                mGouIv.setVisibility(View.GONE);
                mGouIv2.setVisibility(View.VISIBLE);
                mGouIv3.setVisibility(View.GONE);
                SPUtils.put(SPConstant.SP_CALL_TYPE, "sim2");
                setResult(RESULT_OK, getIntent());
                finish();
                break;
            case R.id.ysh_rl:
                mGouIv.setVisibility(View.GONE);
                mGouIv2.setVisibility(View.GONE);
                mGouIv3.setVisibility(View.VISIBLE);
                SPUtils.put(SPConstant.SP_CALL_TYPE, "ysh");
                setResult(RESULT_OK, getIntent());
                finish();
                break;
        }
    }
}
