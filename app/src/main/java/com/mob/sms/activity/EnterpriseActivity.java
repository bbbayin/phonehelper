package com.mob.sms.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.EnterpriseBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EnterpriseActivity extends BaseActivity {
    @BindView(R.id.info)
    TextView mInfo;

    private EnterpriseBean.DataBean mEnterpriseBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterprise);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        getData();
    }

    private void getData() {
        RetrofitHelper.getApi().getEnterpriseInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(enterpriseBean -> {
                    if (enterpriseBean != null && enterpriseBean.code == 200) {
                        mEnterpriseBean = enterpriseBean.data;
                        mInfo.setText(enterpriseBean.data.brief);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @OnClick({R.id.back, R.id.upgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.upgrade:
                try {
                    if (mEnterpriseBean != null && !TextUtils.isEmpty(mEnterpriseBean.link)) {
                        Uri uri = Uri.parse(mEnterpriseBean.link);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
