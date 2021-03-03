package com.mob.sms.activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimSettingActivity extends BaseActivity {
    @BindView(R.id.sim1_iv)
    ImageView mSim1Iv;
    @BindView(R.id.sim1_tv)
    TextView mSim1Tv;
    @BindView(R.id.sim2_iv)
    ImageView mSim2Iv;
    @BindView(R.id.sim2_tv)
    TextView mSim2Tv;
    @BindView(R.id.sim_double_iv)
    ImageView mSimDoubleIv;
    @BindView(R.id.sim_double_tv)
    TextView mSimDoubleTv;
    @BindView(R.id.sim_double_rl1)
    RelativeLayout mSimDoubleRl1;
    @BindView(R.id.sim_double_rl2)
    RelativeLayout mSimDoubleRl2;
    @BindView(R.id.divider1)
    View mDivider1;
    @BindView(R.id.divider2)
    View mDivider2;

    private String mType;
    private String mSimType;

    private final String SIM1 = "sim1";
    private final String SIM2 = "sim2";
    private final String SIM_DOUBLE = "sim_double";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_setting);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){
        mType = getIntent().getStringExtra("type");
        if ("call".equals(mType)) {
            mSimType = SPUtils.getString(SPConstant.SP_CALL_SKSZ, SIM1);
            mSim1Tv.setText("使用卡1拨打");
            mSim2Tv.setText("使用卡2拨打");
            mSimDoubleTv.setText("双卡轮流拨打");
        } else if ("sms".equals(mType)) {
            mSimType = SPUtils.getString(SPConstant.SP_SMS_SKSZ, "SIM1");
            mSim1Tv.setText("使用卡1发送");
            mSim2Tv.setText("使用卡2发送");
            mSimDoubleTv.setText("双卡轮流发送");
        }
        if (SIM1.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_on);
            mSim2Iv.setImageResource(R.mipmap.switch_off);
            mSimDoubleIv.setImageResource(R.mipmap.switch_off);
            mSimDoubleRl1.setVisibility(View.GONE);
            mSimDoubleRl2.setVisibility(View.GONE);
            mDivider1.setVisibility(View.GONE);
            mDivider2.setVisibility(View.GONE);
        } else if (SIM2.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_off);
            mSim2Iv.setImageResource(R.mipmap.switch_on);
            mSimDoubleIv.setImageResource(R.mipmap.switch_off);
            mSimDoubleRl1.setVisibility(View.GONE);
            mSimDoubleRl2.setVisibility(View.GONE);
            mDivider1.setVisibility(View.GONE);
            mDivider2.setVisibility(View.GONE);
        } else if (SIM_DOUBLE.equals(mSimType)) {
            mSim1Iv.setImageResource(R.mipmap.switch_off);
            mSim2Iv.setImageResource(R.mipmap.switch_off);
            mSimDoubleIv.setImageResource(R.mipmap.switch_on);
            mSimDoubleRl1.setVisibility(View.VISIBLE);
            mSimDoubleRl2.setVisibility(View.VISIBLE);
            mDivider1.setVisibility(View.VISIBLE);
            mDivider2.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.back, R.id.sim1_iv, R.id.sim2_iv, R.id.sim_double_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.sim1_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_on);
                mSim2Iv.setImageResource(R.mipmap.switch_off);
                mSimDoubleIv.setImageResource(R.mipmap.switch_off);
                mSimDoubleRl1.setVisibility(View.GONE);
                mSimDoubleRl2.setVisibility(View.GONE);
                mDivider1.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, SIM1);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, SIM1);
                }
                break;
            case R.id.sim2_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_off);
                mSim2Iv.setImageResource(R.mipmap.switch_on);
                mSimDoubleIv.setImageResource(R.mipmap.switch_off);
                mSimDoubleRl1.setVisibility(View.GONE);
                mSimDoubleRl2.setVisibility(View.GONE);
                mDivider1.setVisibility(View.GONE);
                mDivider2.setVisibility(View.GONE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, SIM2);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, SIM2);
                }
                break;
            case R.id.sim_double_iv:
                mSim1Iv.setImageResource(R.mipmap.switch_off);
                mSim2Iv.setImageResource(R.mipmap.switch_off);
                mSimDoubleIv.setImageResource(R.mipmap.switch_on);
                mSimDoubleRl1.setVisibility(View.VISIBLE);
                mSimDoubleRl2.setVisibility(View.VISIBLE);
                mDivider1.setVisibility(View.VISIBLE);
                mDivider2.setVisibility(View.VISIBLE);
                if ("call".equals(mType)) {
                    SPUtils.put(SPConstant.SP_CALL_SKSZ, SIM_DOUBLE);
                } else if ("sms".equals(mType)) {
                    SPUtils.put(SPConstant.SP_SMS_SKSZ, SIM_DOUBLE);
                }
                break;
        }
    }

}
