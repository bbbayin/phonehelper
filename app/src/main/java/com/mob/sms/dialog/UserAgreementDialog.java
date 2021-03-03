package com.mob.sms.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mob.sms.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UserAgreementDialog extends Dialog {
    private Unbinder bind;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void agree();

        void refuse();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public UserAgreementDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private UserAgreementDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_useragreement, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        initView(context);
    }

    private void initView(Context context) {

    }

    @OnClick({R.id.refuse, R.id.agree})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.refuse:
                if (mOnClickListener != null) {
                    mOnClickListener.refuse();
                }
                break;
            case R.id.agree:
                if (mOnClickListener != null) {
                    mOnClickListener.agree();
                }
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        bind.unbind();
    }
}
