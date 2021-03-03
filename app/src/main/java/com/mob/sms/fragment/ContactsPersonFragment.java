package com.mob.sms.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.activity.AddContactActivity;
import com.mob.sms.adapter.ContactsPersonAdapter;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.bean.ContactsBean;
import com.mob.sms.db.CallRecordsTable;
import com.mob.sms.db.DatabaseBusiness;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.OnlineContactBean;
import com.mob.sms.rx.ChooseEvent;
import com.mob.sms.rx.ContactEvent;
import com.mob.sms.rx.LoginEvent;
import com.mob.sms.rx.RxBus;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContactsPersonFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.bottom_rl)
    RelativeLayout mBottomRl;
    @BindView(R.id.select_iv)
    ImageView mSelectIv;
    @BindView(R.id.delete)
    TextView mDelete;

    private ContactsPersonAdapter mContactsPersonAdapter;
    private ArrayList<OnlineContactBean.DataBean> mDatas = new ArrayList<>();

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    private Subscription mSub;
    private Subscription mSub1;
    private boolean mAllSelect;
    private int mSelectNum;

    private String[] dualSimTypes = { "subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_person, container, false);
        ButterKnife.bind(this, view);
        initView();
        getContacts();
        return view;
    }

    private void initView(){
        mContactsPersonAdapter = new ContactsPersonAdapter(getContext(), mDatas);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mContactsPersonAdapter);
        mContactsPersonAdapter.setOnItemClickLsitener(new ContactsPersonAdapter.OnItemClickListener() {
            @Override
            public void select(int position) {
                if (mContactsPersonAdapter.getType() == 1) {
                    mDatas.get(position).isSelect = !mDatas.get(position).isSelect;
                    if (mDatas.get(position).isSelect) {
                        mSelectNum++;
                    } else {
                        mSelectNum--;
                    }
                    mDelete.setText("删除(" + mSelectNum + ")");
                    mContactsPersonAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void call(int position) {
                callPhone(mDatas.get(position).tel);
                CallRecordsTable callRecordsTable = new CallRecordsTable(mDatas.get(position).name,
                        mDatas.get(position).tel, System.currentTimeMillis());
                DatabaseBusiness.createCallRecord(callRecordsTable);
            }

            @Override
            public void sms(int position) {

            }
        });
        mSub =  RxBus.getInstance().toObserverable(ContactEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refresh);
        mSub1 =  RxBus.getInstance().toObserverable(ChooseEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::choose);
    }

    private void callPhone(String mobile) {
        try{
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mobile));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                for (int i = 0; i < dualSimTypes.length; i++) {
                    //1代表卡1,2代表卡2
                    intent.putExtra(dualSimTypes[i], 1);
                }
                startActivity(intent);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void refresh(ContactEvent event) {
        getContacts();
    }

    private void choose(ChooseEvent event) {
        if (event.type == 0) {
            mBottomRl.setVisibility(View.GONE);
            mContactsPersonAdapter.setType(0);
            for (OnlineContactBean.DataBean bean : mDatas) {
                bean.isSelect = false;
            }
            mContactsPersonAdapter.notifyDataSetChanged();
        } else if (event.type == 1) {
            mBottomRl.setVisibility(View.VISIBLE);
            mContactsPersonAdapter.setType(1);
            mContactsPersonAdapter.notifyDataSetChanged();
        }
    }

    @OnClick({R.id.select_iv, R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_iv:
                mAllSelect = !mAllSelect;
                mSelectIv.setImageResource(mAllSelect ? R.mipmap.selected_icon : R.mipmap.unselected_icon);
                for (OnlineContactBean.DataBean bean : mDatas) {
                    bean.isSelect = mAllSelect;
                }
                mContactsPersonAdapter.notifyDataSetChanged();
                if (mAllSelect) {
                    mSelectNum = mDatas.size();
                } else {
                    mSelectNum = 0;
                }
                mDelete.setText("删除(" + mSelectNum + ")");
                break;
            case R.id.delete:
                if (mSelectNum == 0) {
                    ToastUtil.show("请选择要删除的联系人");
                } else {
                    String ids = "";
                    for (OnlineContactBean.DataBean bean : mDatas) {
                        if (bean.isSelect) {
                            if (TextUtils.isEmpty(ids)) {
                                ids = bean.id + "";
                            } else {
                                ids = ids + "," + bean.id;
                            }
                        }
                    }
                    delete(ids);
                }
                break;
        }
    }

    private void delete(String ids){
        RetrofitHelper.getApi().deleteContacts(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""), ids).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                    if (baseBean != null && baseBean.code == 200) {
                        getContacts();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void getContacts() {
//        try {
//            ContentResolver cr = getContext().getContentResolver();
//            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
//            while (cursor.moveToNext()) {
//                mDatas.add(new ContactsBean(cursor.getString(cursor.getColumnIndex(NUM)), cursor.getString(cursor.getColumnIndex(NAME))));
//            }
//            mContactsPersonAdapter.notifyDataSetChanged();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        RetrofitHelper.getApi().getContacts(SPUtils.getString(SPConstant.SP_USER_TOKEN, "")).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onlineContactBean -> {
                    if (onlineContactBean != null && onlineContactBean.code == 200) {
                        mDatas.clear();
                        mDatas.addAll(onlineContactBean.data);
                        mContactsPersonAdapter.notifyDataSetChanged();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        if (mSub1 != null && !mSub1.isUnsubscribed()) {
            mSub1.unsubscribe();
        }
    }
}
