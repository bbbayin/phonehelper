package com.mob.sms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.bean.SplashBean;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class SplashBannerAdapter extends BannerAdapter<String, SplashBannerAdapter.BannerViewHolder> {
    private Context mContext;
    private List<SplashBean> mLists;

    public SplashBannerAdapter(Context context, List<String> mDatas, List<SplashBean> lists) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
        mContext = context;
        mLists = lists;
    }

    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner_splash, parent, false);
        BannerViewHolder holder = new BannerViewHolder(view);
        return holder;
    }

    @Override
    public void onBindView(BannerViewHolder holder, String img, int position, int size) {
        holder.imageView.setBackgroundResource(mLists.get(position).res);
    }


    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image);
        }
    }
}
