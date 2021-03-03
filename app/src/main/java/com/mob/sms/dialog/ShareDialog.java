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

public class ShareDialog extends Dialog {
    private Unbinder bind;


    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void shareWx();
        void shareQQ();
        void qrcode();
        void copyUrl();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public ShareDialog(@NonNull Context context) {
        this(context, R.style.dialogNoBg);
    }

    private ShareDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        View view = View.inflate(context, R.layout.dialog_share, null);
        bind = ButterKnife.bind(this, view);

        setContentView(view);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @OnClick({R.id.wx_ll, R.id.qq_ll, R.id.qrcode_ll, R.id.copy_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wx_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.shareWx();
                }
                break;
            case R.id.qq_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.shareQQ();
                }
                break;
            case R.id.qrcode_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.qrcode();
                }
                break;
            case R.id.copy_ll:
                if(mOnClickListener!=null){
                    mOnClickListener.copyUrl();
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
