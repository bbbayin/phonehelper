package com.mob.sms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.rx.ExitEvent;
import com.mob.sms.rx.RxBus;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){

    }

    @OnClick({R.id.back, R.id.bind_rl, R.id.modify_rl, R.id.zhuxiao, R.id.exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.bind_rl:
                startActivity(new Intent(SettingActivity.this, BindMobileActivity.class));
                break;
            case R.id.modify_rl:
                Intent intent = new Intent(SettingActivity.this, ForgetPwdActivity.class);
                intent.putExtra("type", "modify");
                startActivity(intent);
                break;
            case R.id.zhuxiao:
                delUser();
                break;
            case R.id.exit:
                logout();
                break;
        }
    }

    private void delUser() {
        RetrofitHelper.getApi().delUser(SPUtils.getString(SPConstant.SP_USER_TOKEN, "")).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        logout();
                    } else {
                        Toast.makeText(SettingActivity.this, baseBean.msg, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Toast.makeText(SettingActivity.this, "注销失败", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                });
    }


    private void logout() {
        SPUtils.clear();
        RxBus.getInstance().post(new ExitEvent());
        finish();
    }
}
