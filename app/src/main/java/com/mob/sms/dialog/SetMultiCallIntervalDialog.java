package com.mob.sms.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mob.sms.R;
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
    private int mMinValue;
    private int mSecondValue;

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
    }

    @OnClick({R.id.cancel, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                if (mOnClickListener != null) {
                    if (mMinValue == 0) {
                        mOnClickListener.confirm(mSecondValue);
                    } else {
                        mOnClickListener.confirm(mMinValue * 60 + mSecondValue);
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
