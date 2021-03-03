package com.mob.sms.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mob.sms.R;
import com.mob.sms.network.bean.FeedbackDetailBean;
import com.mob.sms.network.bean.OrderHistoryBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<FeedbackDetailBean.DataBean.RecordsBean> mDatas;

    public FeedbackDetailAdapter(Context context, ArrayList<FeedbackDetailBean.DataBean.RecordsBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        VHolder vHolder = (VHolder) viewHolder;
        if (TextUtils.isEmpty(mDatas.get(position).userId)) {
            vHolder.content.setText("客服回复: " + mDatas.get(position).content);
        } else {
            vHolder.content.setText("用户回复: " + mDatas.get(position).content);
        }
        if(TextUtils.isEmpty(mDatas.get(position).img)){
            vHolder.image.setVisibility(View.GONE);
        }else {
            vHolder.image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mDatas.get(position).img).into(vHolder.image);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_feedback_detail, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.image)
        ImageView image;
        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
