package com.mob.sms.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mob.sms.R;
import com.mob.sms.policy.PolicyActivity;

import org.w3c.dom.Text;

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
        TextView tvContent = findViewById(R.id.dialog_tv_policy);
        String content = "内容请你务必审慎阅读，充分理解\"服务协议\"和\"隐私政策\"各条款，包括但不限于：为了向你提供即时通讯，内容分享等服务，我们需要收集你的设备信息，操作日志，等个人信息。" +
                "你可以在设置中查看、变更、删除个人信息并管理你的授权。\n你可阅读《服务协议》《隐私政策》了解详细信息。如你同意，请点击\"同意\"开始接受我们的服务。";
        SpannableString spannableString = new SpannableString(content);
        int start1 = content.indexOf("《服");
        int center = content.indexOf("《隐");
        int end2 = content.indexOf("了解详");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#00C296")),
                start1,
                end2,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 用户协议
                        toPolicy(context, PolicyActivity.TYPE_USER);
                    }
                },
                start1,
                center,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // 隐私政策
                        toPolicy(context, PolicyActivity.TYPE_SECRET);
                    }
                },
                center,
                end2,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvContent.setText(spannableString);
    }

    private void toPolicy(Context context, int type) {
        Intent intent = new Intent(context, PolicyActivity.class);
        intent.putExtra(PolicyActivity.KEY_POLICY_TYPE, type);
        context.startActivity(intent);
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
