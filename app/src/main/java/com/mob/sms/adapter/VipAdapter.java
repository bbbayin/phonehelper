package com.mob.sms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.network.bean.VipBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VipAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<VipBean.DataBean> mDatas;

    public VipAdapter(Context context, ArrayList<VipBean.DataBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onclick(int position);
    }

    public void setOnItemClickLsitener(OnItemClickListener onItemClickLsitener) {
        this.mOnItemClickListener = onItemClickLsitener;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        VHolder vHolder = (VHolder) viewHolder;
        final VipBean.DataBean dataBean = mDatas.get(position);
        vHolder.price.setText(dataBean.price + "");
        if (dataBean.isSelected) {
            viewHolder.itemView.setAlpha(1f);
        }else {
            viewHolder.itemView.setAlpha(0.5f);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataBean != null) {
                    for (int i = 0; i < mDatas.size(); i++) {
                        mDatas.get(i).isSelected = false;
                    }
                    dataBean.isSelected = true;
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_vip, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.price)
        TextView price;

        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
