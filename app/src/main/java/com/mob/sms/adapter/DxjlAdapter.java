package com.mob.sms.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.activity.RecordListActivity;
import com.mob.sms.network.bean.RecordBean;
import com.mob.sms.network.bean.SmsRecordBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DxjlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<SmsRecordBean.DataBean.RowsBean> mDatas;
    private int mType;

    public DxjlAdapter(Context context, ArrayList<SmsRecordBean.DataBean.RowsBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public void setType(int type){
        this.mType = type;
    }

    public int getType(){
        return mType;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void select(int position);
        void jump(int position);
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
        SmsRecordBean.DataBean.RowsBean rowsBean = mDatas.get(position);
        vHolder.time.setText(rowsBean.createTime);
        vHolder.time2.setText(rowsBean.createTime);
        if ("0".equals(rowsBean.batchSend)) {
            vHolder.title.setText("发送短信");
        } else if ("1".equals(rowsBean.batchSend)) {
            vHolder.title.setText("批量发送短信");
        }
        if ("-1".equals(rowsBean.status)) {
            vHolder.state.setText("发送失败");
            vHolder.state.setTextColor(Color.parseColor("#00C296"));
            vHolder.call.setBackgroundResource(R.drawable.round_36_green);
            vHolder.call.setText("立即发送");
        } else if ("0".equals(rowsBean.status)) {
            vHolder.state.setText("已暂停");
            vHolder.state.setTextColor(Color.parseColor("#FFA439"));
            vHolder.call.setBackgroundResource(R.drawable.round_36_yellow);
            vHolder.call.setText("继续发送");
        } else if ("1".equals(rowsBean.status)) {
            vHolder.state.setText("已发送");
            vHolder.state.setTextColor(Color.parseColor("#A6A6A6"));
            vHolder.call.setBackgroundResource(R.drawable.round_36_red);
            vHolder.call.setText("重新发送");
        }

        if (mType == 0) {
            vHolder.select_iv.setVisibility(View.GONE);
        } else {
            vHolder.select_iv.setVisibility(View.VISIBLE);
            vHolder.select_iv.setImageResource(rowsBean.isSelect?R.mipmap.selected_icon:R.mipmap.unselected_icon);
        }
        vHolder.root_ll.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.select(position);
            }
        });
        vHolder.call.setOnClickListener(view -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.jump(position);
            }
        });
        vHolder.tvContent.setText(rowsBean.content);
        vHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RecordListActivity.class);
                intent.putExtra("list", rowsBean.tels);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_record_sms, parent, false);
        RecyclerView.ViewHolder holder = new VHolder(view);
        return holder;
    }

    class VHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_ll)
        LinearLayout root_ll;
        @BindView(R.id.select_iv)
        ImageView select_iv;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.state)
        TextView state;
        @BindView(R.id.time2)
        TextView time2;
        @BindView(R.id.call)
        TextView call;
        @BindView(R.id.tv_sms_content)
        TextView tvContent;
        public VHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }

}
