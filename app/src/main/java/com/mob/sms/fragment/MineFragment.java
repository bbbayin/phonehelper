package com.mob.sms.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.mob.sms.application.MyApplication;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.debug.DebugActivity;
import com.mob.sms.dialog.ShareDialog;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.ShareBean;
import com.mob.sms.network.bean.UserInfoBean;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;
import com.mob.sms.utils.Utils;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;

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
    @BindView(R.id.me_vip_info_layout)
    View vipLayout;

    private ShareBean.DataBean mShareInfo;
    private UserInfoBean.DataBean mUserInfo;
    private boolean isCreated = false;
    private Activity mActivity;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        ButterKnife.bind(this, view);

//        mName.setText(SPUtils.getString(SPConstant.SP_USER_NAME, ""));
//        Glide.with(getContext()).load(SPUtils.getString(SPConstant.SP_USER_HEAD, "")).into(mHead);
        getShareInfo();
        getUserInfo();
        isCreated = true;
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
        RetrofitHelper.getApi().getUserInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userInfoBean -> {
                    if (userInfoBean != null && userInfoBean.code == 200) {
                        mUserInfo = userInfoBean.data;
                        mUserid.setText("ID: " + mUserInfo.userId);
                        mTime.setText(mUserInfo.expTime);

                        if (!TextUtils.isEmpty(mUserInfo.avatar)) {
                            SPUtils.put(SPConstant.SP_USER_HEAD, mUserInfo.avatar);
                            Glide.with(getContext()).load(mUserInfo.avatar).into(mHead);
                        }
                        if (!TextUtils.isEmpty(mUserInfo.nickName)) {
                            SPUtils.put(SPConstant.SP_USER_NAME, mUserInfo.nickName);
                            mName.setText(mUserInfo.nickName);
                        }
                        if (mUserInfo.allMinute > 0) {
                            vipLayout.setVisibility(View.VISIBLE);
                        } else {
                            vipLayout.setVisibility(View.GONE);
                        }
                        int remain = mUserInfo.allMinute - mUserInfo.useMinute;
                        mProgressbar.setMax(mUserInfo.allMinute);
                        mProgressbar.setProgress(remain);
                        mUseMinute.setText(String.format("剩余%s分钟", remain));
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.vip_rl, R.id.about_rl, R.id.question_rl, R.id.share_rl, R.id.problem_rl, R.id.xiaofei_rl,
            R.id.setting_rl, R.id.qiye_rl, R.id.user_ll, R.id.settings_btn_debug})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.settings_btn_debug:
                startActivity(new Intent(getContext(), DebugActivity.class));
                break;
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
                RetrofitHelper.getApi().getShare().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(shareBean -> {
                            if (shareBean != null && shareBean.code == 200) {
                                mShareInfo = shareBean.data;
                                showShareDialog();
                            }
                        }, throwable -> {
                            ToastUtil.show("出错了，请重试");
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

    private void showShareDialog() {
        final ShareDialog shareDialog = new ShareDialog(getContext());
        shareDialog.show();
        shareDialog.setOnClickListener(new ShareDialog.OnClickListener() {
            @Override
            public void shareWx() {
                Glide.with(getContext()).load(mShareInfo.logo)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ToastUtil.show("图片加载失败");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                WXWebpageObject obj = new WXWebpageObject();
                                obj.webpageUrl = mShareInfo.url;
                                WXMediaMessage msg = new WXMediaMessage();
                                msg.title = mShareInfo.title;
                                msg.description = mShareInfo.content;
                                msg.mediaObject = obj;
                                Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
                                Canvas canvas = new Canvas(bitmap);
//                                canvas.drawColor(Color.BLUE);
                                resource.setBounds(0,0,200,200);
                                resource.draw(canvas);
                                msg.thumbData = Utils.bmpToByteArray(bitmap, true);
                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.message = msg;
                                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                                req.transaction = String.valueOf(System.currentTimeMillis());
                                MyApplication.wxApi.sendReq(req);
                                return false;
                            }
                        }).submit();


            }

            @Override
            public void shareQQ() {
                //APP ID：101924228
                //APP Key：1166dd0fd38327bb8f4da43276b8865f
                //审核通过
                Tencent instance = Tencent.createInstance("101924228", mActivity);
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mShareInfo.title);//必填
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mShareInfo.content);//选填
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mShareInfo.url);//必填
                ArrayList<String> images = new ArrayList<>();
                images.add(mShareInfo.logo);
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, images);
                instance.shareToQzone(mActivity, params, new IUiListener() {
                    @Override
                    public void onComplete(Object o) {

                    }

                    @Override
                    public void onError(UiError uiError) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onWarning(int i) {

                    }
                });
            }

            @Override
            public void wechat() {
                if (mShareInfo != null) {
                    Glide.with(getContext()).load(mShareInfo.logo)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    ToastUtil.show("图片加载失败");
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    WXWebpageObject obj = new WXWebpageObject();
                                    obj.webpageUrl = mShareInfo.url;
                                    WXMediaMessage msg = new WXMediaMessage();
                                    msg.title = mShareInfo.title;
                                    msg.description = mShareInfo.content;
                                    msg.mediaObject = obj;
                                    Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
                                    Canvas canvas = new Canvas(bitmap);
                                    resource.setBounds(0,0,200,200);
                                    resource.draw(canvas);
                                    msg.thumbData = Utils.bmpToByteArray(bitmap, true);
                                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                                    req.message = msg;
                                    req.scene = SendMessageToWX.Req.WXSceneSession;
                                    req.transaction = String.valueOf(System.currentTimeMillis());
                                    MyApplication.wxApi.sendReq(req);
                                    return false;
                                }
                            }).submit();
                }
            }

            @Override
            public void qqChat() {
                if (mShareInfo != null) {
                    Tencent instance = Tencent.createInstance("101924228", mActivity);
                    final Bundle params = new Bundle();
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mShareInfo.title);//必填
                    params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mShareInfo.content);//选填
                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mShareInfo.url);//必填
                    ArrayList<String> images = new ArrayList<>();
                    images.add(mShareInfo.logo);
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, images);
                    instance.shareToQQ(mActivity, params, new IUiListener() {
                        @Override
                        public void onComplete(Object o) {

                        }

                        @Override
                        public void onError(UiError uiError) {

                        }

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onWarning(int i) {

                        }
                    });
                }
            }
        });
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isCreated) {
            getUserInfo();
        }
    }
}
