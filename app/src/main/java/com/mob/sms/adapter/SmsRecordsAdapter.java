package com.mob.sms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.bean.SendSmsRecord;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SmsRecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<SendSmsRecord> mDatas;

    public SmsRecordsAdapter(Context context, ArrayList<SendSmsRecord> datas) {
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
        vHolder.name.setText(mDatas.get(position).name);
        vHolder.mobile.setText(mDatas.get(position).mobile);
        vHolder.state.setText(mDatas.get(position).isSend ? "已发送" : "未发送");
        vHolder.state.setTextColor(mDatas.get(position).isSend ? Color.parseColor("#33C197") :
                Color.parseColor("#FF5151"));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sms_record, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.mobile)
        TextView mobile;
        @BindView(R.id.state)
        TextView state;

        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
