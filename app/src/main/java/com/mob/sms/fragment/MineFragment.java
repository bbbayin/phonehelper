package com.mob.sms.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms.R;
import com.mob.sms.activity.AboutUsActivity;
import com.mob.sms.activity.EnterpriseActivity;
import com.mob.sms.activity.FeedBackActivity;
import com.mob.sms.activity.OrderHistoryActivity;
import com.mob.sms.activity.QuestionActivity;
import com.mob.sms.activity.SettingActivity;
import com.mob.sms.activity.ShareQrcodeActivity;
import com.mob.sms.activity.UserInfoActivity;
import com.mob.sms.activity.VipActivity;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.dialog.ShareDialog;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.ShareBean;
import com.mob.sms.network.bean.UserInfoBean;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MineFragment extends BaseFragment {
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.head)
    ImageView mHead;
    @BindView(R.id.userid)
    TextView mUserid;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.user_minute)
    TextView mUseMinute;
    @BindView(R.id.all_minute)
    TextView mAllMinute;
    @BindView(R.id.progressbar)
    ProgressBar mProgressbar;

    private ShareBean.DataBean mShareInfo;
    private UserInfoBean.DataBean mUserInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        ButterKnife.bind(this, view);

//        mName.setText(SPUtils.getString(SPConstant.SP_USER_NAME, ""));
//        Glide.with(getContext()).load(SPUtils.getString(SPConstant.SP_USER_HEAD, "")).into(mHead);
        getShareInfo();
        getUserInfo();
        return view;
    }

    private void getShareInfo() {
        RetrofitHelper.getApi().getShare().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareBean -> {
                    if (shareBean != null && shareBean.code == 200) {
                        mShareInfo = shareBean.data;
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void getUserInfo() {
        RetrofitHelper.getApi().getUserInfo(SPUtils.getString(SPConstant.SP_USER_TOKEN, "")).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userInfoBean -> {
                    if (userInfoBean != null && userInfoBean.code == 200) {
                        mUserInfo = userInfoBean.data;
                        mUserid.setText("ID: " + mUserInfo.userId);
                        mTime.setText(mUserInfo.expTime);
                        mUseMinute.setText(mUserInfo.useMinute + "分钟");
                        mAllMinute.setText(mUserInfo.allMinute + "分钟");
                        if (!TextUtils.isEmpty(mUserInfo.avatar)) {
                            SPUtils.put(SPConstant.SP_USER_HEAD, mUserInfo.avatar);
                            Glide.with(getContext()).load(mUserInfo.avatar).into(mHead);
                        }
                        if (!TextUtils.isEmpty(mUserInfo.nickName)) {
                            SPUtils.put(SPConstant.SP_USER_NAME, mUserInfo.nickName);
                            mName.setText(mUserInfo.nickName);
                        }
                        mProgressbar.setMax(mUserInfo.allMinute);
                        mProgressbar.setProgress(mUserInfo.useMinute);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.vip_rl, R.id.about_rl, R.id.question_rl, R.id.share_rl, R.id.problem_rl, R.id.xiaofei_rl,
            R.id.setting_rl, R.id.qiye_rl, R.id.user_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.vip_rl:
                startActivity(new Intent(getContext(), VipActivity.class));
                break;
            case R.id.about_rl:
                startActivity(new Intent(getContext(), AboutUsActivity.class));
                break;
            case R.id.question_rl:
                startActivity(new Intent(getContext(), FeedBackActivity.class));
                break;
            case R.id.share_rl:
                ShareDialog shareDialog = new ShareDialog(getContext());
                shareDialog.show();
                shareDialog.setOnClickListener(new ShareDialog.OnClickListener() {
                    @Override
                    public void shareWx() {

                    }

                    @Override
                    public void shareQQ() {

                    }

                    @Override
                    public void qrcode() {
                        if (mShareInfo != null) {
                            Intent intent = new Intent(getContext(), ShareQrcodeActivity.class);
                            intent.putExtra("url", mShareInfo.url);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void copyUrl() {
                        if (mShareInfo != null) {
                            copyContentToClipboard(mShareInfo.url, getContext());
                        }
                    }
                });
                break;
            case R.id.problem_rl:
                startActivity(new Intent(getContext(), QuestionActivity.class));
                break;
            case R.id.xiaofei_rl:
                startActivity(new Intent(getContext(), OrderHistoryActivity.class));
                break;
            case R.id.setting_rl:
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case R.id.qiye_rl:
                startActivity(new Intent(getContext(), EnterpriseActivity.class));
                break;
            case R.id.user_ll:
                startActivity(new Intent(getContext(), UserInfoActivity.class));
                break;
        }
    }

    public void copyContentToClipboard(String content, Context context) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtil.show("已复制到粘贴板");
    }

    @Override
    public void onResume() {
        super.onResume();
        mName.setText(SPUtils.getString(SPConstant.SP_USER_NAME, ""));
        Glide.with(getContext()).load(SPUtils.getString(SPConstant.SP_USER_HEAD, "")).into(mHead);
    }
}