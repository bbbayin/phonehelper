package com.mob.sms.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms.R;
import com.mob.sms.utils.Constants;
import com.zyyoona7.wheel.WheelView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SetMultiCallIntervalDialog extends Dialog {
    private Unbinder bind;
    @BindView(R.id.wheelview)
    WheelView mWheelView;
    @BindView(R.id.wheelview2)
    WheelView mWheelView2;
    @BindView(R.id.btn_fixed_interval)
    CheckBox cbFixedInterval;
    @BindView(R.id.btn_random_interval)
    CheckBox cbRandomInterval;
    @BindView(R.id.tv_interval_suggest)
    TextView tvIntervalSuggest;

    private int mMinValue;
    private int mSecondValue;
    // 间隔类型
    private String intervalType = Constants.FIXED;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void confirm(int second);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public SetMultiCallIntervalDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private SetMultiCallIntervalDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_set_multicallinterval, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add(i + "");
            }
        }
        mWheelView.setData(list);
        mWheelView.setSelectedItemPosition(0);
        mMinValue = 0;

        mWheelView2.setData(list);
        mWheelView2.setSelectedItemPosition(6);
        mSecondValue = 6;

        mWheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mMinValue = position;
            }
        });
        mWheelView2.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, Object data, int position) {
                mSecondValue = position;
            }
        });

        cbFixedInterval.setChecked(true);
        cbRandomInterval.setChecked(false);

        // 固定间隔
        cbFixedInterval.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbRandomInterval.setChecked(false);
                intervalType = Constants.FIXED;
                tvIntervalSuggest.setVisibility(View.VISIBLE);
            }
        });
        // 随机间隔
        cbRandomInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbFixedInterval.setChecked(false);
                    intervalType = Constants.RANDOM;
                    tvIntervalSuggest.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick({R.id.cancel, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                if (mOnClickListener != null) {
                    int interval = mMinValue * 60 + mSecondValue;
                    if (Constants.FIXED.equals(intervalType)) {
                        mOnClickListener.confirm(interval);
                    }else {
                        // 负数代表随机间隔
                        mOnClickListener.confirm(-interval);
                    }
                }
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
