package com.mob.sms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.j256.ormlite.stmt.query.In;
import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.dialog.UserAgreementDialog;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.rx.LoginEvent;
import com.mob.sms.rx.RxBus;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.mobile_et)
    EditText mMobileEt;
    @BindView(R.id.pwd_et)
    EditText mPwdEt;
    @BindView(R.id.eye_icon)
    ImageView mEyeIcon;
    @BindView(R.id.login)
    ImageView mLoginIv;
    @BindView(R.id.wx_tip)
    TextView mWxTip;
    @BindView(R.id.qq_tip)
    TextView mQqTip;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;

    private boolean mCanSeePwd;

    private boolean mTxMobile;
    private boolean mTxPwd;
    private boolean mCanLogin;

    private Subscription mSub;
    private boolean mSelectAgreement;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.white));
        initView();
        mSub =  RxBus.getInstance().toObserverable(LoginEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::finishPage);
    }

    private void finishPage(LoginEvent event) {
        finish();
    }

    private void initView(){
        mMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxMobile = true;
                } else {
                    mTxMobile = false;
                }
                if(mTxMobile && mTxPwd&& mSelectAgreement){
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mLoginIv.setBackgroundResource(mCanLogin?R.mipmap.login_bg_green:R.mipmap.login_bg_grey);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString())){
                    mTxPwd = true;
                } else {
                    mTxPwd = false;
                }
                if(mTxMobile && mTxPwd&& mSelectAgreement){
                    mCanLogin = true;
                } else {
                    mCanLogin = false;
                }
                mLoginIv.setBackgroundResource(mCanLogin?R.mipmap.login_bg_green:R.mipmap.login_bg_grey);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        if("qq".equals(SPUtils.getString(SPConstant.SP_USER_LOGIN_TYPE, ""))){
            mQqTip.setVisibility(View.VISIBLE);
            mWxTip.setVisibility(View.INVISIBLE);
        } else if("wx".equals(SPUtils.getString(SPConstant.SP_USER_LOGIN_TYPE, ""))){
            mQqTip.setVisibility(View.INVISIBLE);
            mWxTip.setVisibility(View.VISIBLE);
        } else {
            mQqTip.setVisibility(View.INVISIBLE);
            mWxTip.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected boolean setTransparentStatusEnable() {
        return true;
    }

    @OnClick({R.id.register, R.id.forget, R.id.eye_icon, R.id.login, R.id.wx_login, R.id.qq_login, R.id.select_iv,
            R.id.agreement})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.forget:
                startActivity(new Intent(this, ForgetPwdActivity.class));
                break;
            case R.id.eye_icon:
                mCanSeePwd = !mCanSeePwd;
                mPwdEt.setTransformationMethod(mCanSeePwd? PasswordTransformationMethod.getInstance(): HideReturnsTransformationMethod.getInstance());
                mEyeIcon.setBackgroundResource(mCanSeePwd?R.mipmap.eye_icon:R.mipmap.eye2_icon);
                break;
            case R.id.wx_login:
                UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        Intent intent = new Intent(LoginActivity.this, WxInfoActivity.class);
                        intent.putExtra("uid", map.get("uid"));
                        intent.putExtra("name", map.get("name"));
                        intent.putExtra("iconurl", map.get("iconurl"));
                        startActivity(intent);
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {

                    }
                });
                break;
            case R.id.qq_login:
                UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.QQ, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        SPUtils.put(SPConstant.SP_USER_NAME, map.get("name"));
                        SPUtils.put(SPConstant.SP_USER_HEAD, map.get("iconurl"));
                        SPUtils.put(SPConstant.SP_USER_ID, map.get("uid"));
                        authLogin(map.get("iconurl"), "3", map.get("name"), map.get("uid"), "umeng");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                    }
                });
                break;
            case R.id.login:
                if (mCanLogin) {
                    login();
                }
                break;
            case R.id.select_iv:
            case R.id.agreement:
                UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
                userAgreementDialog.show();
                userAgreementDialog.setOnClickListener(new UserAgreementDialog.OnClickListener() {
                    @Override
                    public void agree() {
                        mSelectAgreement = true;
                        mSelectIv.setBackgroundResource(R.mipmap.selected_icon);
                        userAgreementDialog.dismiss();

                        if(mTxMobile && mTxPwd&& mSelectAgreement){
                            mCanLogin = true;
                        } else {
                            mCanLogin = false;
                        }
                        mLoginIv.setBackgroundResource(mCanLogin?R.mipmap.login_bg_green:R.mipmap.login_bg_grey);
                    }

                    @Override
                    public void refuse() {
                        mSelectAgreement = false;
                        mSelectIv.setBackgroundResource(R.mipmap.unselected_icon);
                        userAgreementDialog.dismiss();

                        if(mTxMobile && mTxPwd&& mSelectAgreement){
                            mCanLogin = true;
                        } else {
                            mCanLogin = false;
                        }
                        mLoginIv.setBackgroundResource(mCanLogin?R.mipmap.login_bg_green:R.mipmap.login_bg_grey);
                    }
                });
                break;
        }
    }

    private void authLogin(String avatar, String loginType, String nickName, String openid, String originate) {
        RetrofitHelper.getApi().authLogin(avatar, loginType, nickName, openid, originate).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginBean -> {
                    if (loginBean != null && loginBean.code == 200) {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        Log.i("jqt", "loginBean.data: " + loginBean.token);
                        SPUtils.put(SPConstant.SP_USER_TOKEN, loginBean.token);
                        SPUtils.put(SPConstant.SP_USER_LOGIN_TYPE, "qq");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    private void login(){
        RetrofitHelper.getApi().login(mMobileEt.getText().toString(), mPwdEt.getText().toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginBean -> {
                    if (loginBean != null && loginBean.code == 200) {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                        SPUtils.put(SPConstant.SP_USER_TOKEN, loginBean.data);
                        SPUtils.put(SPConstant.SP_USER_LOGIN_TYPE, "mobile");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }
}
