package com.mob.sms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DocImportActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.tip)
    TextView mTip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_import);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){
        String type = getIntent().getStringExtra("type");
        mTitle.setText(type + "文档导入");
        mTip.setText(type + "格式");
    }

    @OnClick({R.id.back, R.id.choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.choose:
                Intent intent = new Intent(DocImportActivity.this, DocSelectActivity.class);
                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("type2", getIntent().getStringExtra("type2"));
                startActivity(intent);
                finish();
                break;
        }
    }
}
