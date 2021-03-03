package com.mob.sms.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.DocAdapter;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.DocBean;
import com.mob.sms.dialog.SortDialog;
import com.mob.sms.utils.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class DocSelectActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ArrayList<DocBean> mDatas = new ArrayList<>();
    private DocAdapter mDocAdapter;
    private String filePath = Environment.getExternalStorageDirectory().toString() + File.separator;
    private PromptDialog mPromptDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_select);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView(){
        String type = getIntent().getStringExtra("type");
        mPromptDialog = new PromptDialog(this);
        mPromptDialog.showLoading("正在加载...");

        mDocAdapter = new DocAdapter(this, mDatas);
        mRecyclerView.setAdapter(mDocAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDocAdapter.setOnItemClickLsitener(new DocAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                for (int i = 0; i < mDatas.size(); i++) {
                    if (i == position) {
                        mDatas.get(position).isSelected = !mDatas.get(position).isSelected;
                    } else {
                        mDatas.get(i).isSelected = false;
                    }
                }
                mDocAdapter.notifyDataSetChanged();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                getData(filePath, type);
                mHandler.sendEmptyMessageDelayed(0, 500);
            }
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            mPromptDialog.dismissImmediately();
            mDocAdapter.notifyDataSetChanged();
        }
    };

    /****
     * 递归算法获取本地文件
     * @param path
     */
    private void getData(String path, String type) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                for (File f : fileArray) {
                    if (f.isDirectory()) {
                        getData(f.getPath(), type);
                    } else {
                        if ("word".equals(type)) {
                            if (f.getName().endsWith(".docx") || f.getName().endsWith(".doc")) {
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(f);
                                    String time = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date(f.lastModified()));
                                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(),
                                            time, time, f.length(), false, type);
                                    mDatas.add(bean);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("excel".equals(type)) {
                            if (f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) {
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(f);
                                    String time = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date(f.lastModified()));
                                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(),
                                            time, time, f.length(), false, type);
                                    mDatas.add(bean);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("txt".equals(type)) {
                            if (f.getName().endsWith(".txt")) {
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(f);
                                    String time = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date(f.lastModified()));
                                    DocBean bean = new DocBean(f.getName(), f.getAbsolutePath(),
                                            time, time, f.length(), false, type);
                                    mDatas.add(bean);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OnClick({R.id.back, R.id.search_iv, R.id.import_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.search_iv:
                SortDialog sortDialog = new SortDialog(DocSelectActivity.this);
                sortDialog.show();
                sortDialog.setOnClickListener(new SortDialog.OnClickListener() {
                    @Override
                    public void sort(int type) {
                        sortDialog.dismiss();
                    }
                });
                break;
            case R.id.import_tv:
                boolean mIsSelected = false;
                DocBean docBean = null;
                for (int i = 0; i < mDatas.size(); i++) {
                    if (mDatas.get(i).isSelected) {
                        docBean = mDatas.get(i);
                        mIsSelected = true;
                    }
                }
                if (!mIsSelected) {
                    ToastUtil.show("请选择文档");
                } else {
                    Intent intent = new Intent(DocSelectActivity.this, PhoneInfoActivity.class);
                    if ("word".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 0);
                    } else if ("excel".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 1);
                    } else if ("txt".equals(getIntent().getStringExtra("type"))) {
                        intent.putExtra("type", 2);
                    }
                    intent.putExtra("title", docBean.fileName);
                    intent.putExtra("filePath", docBean.filePath);
                    intent.putExtra("type2", getIntent().getStringExtra("type2"));
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }
}
