package com.mob.sms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.CallRecordsAdapter;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.db.CallRecordsTable;
import com.mob.sms.db.DatabaseBusiness;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CallRecordsFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private CallRecordsAdapter mCallRecordsAdapter;
    private ArrayList<CallRecordsTable> mDatas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_records, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView(){
        List<CallRecordsTable> list = DatabaseBusiness.getCallRecords();
        if (list != null && list.size() > 0) {
            mDatas.clear();
            mDatas.addAll(list);
        }
        mCallRecordsAdapter = new CallRecordsAdapter(getContext(), mDatas);
        mRecyclerView.setAdapter(mCallRecordsAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }
}
