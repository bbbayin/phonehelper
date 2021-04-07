package com.mob.sms.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mob.sms.R;
import com.mob.sms.base.BaseActivity;

public class ImageViewerActivity extends BaseActivity {
    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer_layout);
        ImageView ivBig = findViewById(R.id.image_big_viewer);
        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            Glide.with(this).load(url).into(ivBig);
        }
    }
}
