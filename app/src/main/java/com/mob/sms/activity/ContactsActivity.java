package com.mob.sms.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.ContactsAdapter;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.ContactsBean;
import com.mob.sms.contacts.CharacterParser;
import com.mob.sms.contacts.PinyinComparator;
import com.mob.sms.contacts.SideBar;
import com.mob.sms.contacts.SortModel;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.sidebar)
    SideBar mSideBar;
    @BindView(R.id.top_tv)
    TextView mTopTv;

    private ContactsAdapter mContactsAdapter;
    private ArrayList<ContactsBean> mDatas = new ArrayList<>();
    private CharacterParser mCharacterParser;
    private PinyinComparator mPinyinComparator;
    private ArrayList<SortModel> mSortList = new ArrayList<>();

    private int lastFirstVisibleItem = -1;

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private String mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        setStatusBar(getResources().getColor(R.color.green));
        ButterKnife.bind(this);
        initView();
    }

    private void initView(){
        mType = getIntent().getStringExtra("type");
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();

        getContacts();

        filledData(mDatas);
        Collections.sort(mSortList, mPinyinComparator);

        mContactsAdapter = new ContactsAdapter(this, mSortList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mContactsAdapter);
        mContactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                for (int i = 0; i < mSortList.size(); i++) {
                    if (i == position) {
                        mSortList.get(i).setChecked(!mSortList.get(i).isChecked());
                    } else {
                        mSortList.get(i).setChecked(false);
                    }
                }
                mContactsAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();

                    int section = mContactsAdapter.getSectionForPosition(firstItemPosition);
                    if (firstItemPosition != lastFirstVisibleItem) {
                        mTopTv.setText(String.valueOf((char) section));
                    }
                    lastFirstVisibleItem = firstItemPosition;
                }
            }
        });

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mRecyclerView.scrollToPosition(position);
                }

            }
        });
    }

    private void getContacts() {
        try {
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
            while (cursor.moveToNext()) {
                mDatas.add(new ContactsBean(cursor.getString(cursor.getColumnIndex(NUM)), cursor.getString(cursor.getColumnIndex(NAME))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filledData(ArrayList<ContactsBean> datas) {
        for (int i = 0; i < datas.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(datas.get(i).name);
            sortModel.setMobile(datas.get(i).mobile);
            sortModel.setChecked(false);
            String pinyin = mCharacterParser.getSelling(datas.get(i).name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
    }

    @OnClick({R.id.back, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.confirm:
                boolean isSelect = false;
                SortModel sortModel = null;
                for (int i = 0; i < mSortList.size(); i++) {
                    if (mSortList.get(i).isChecked()) {
                        isSelect = true;
                        sortModel = mSortList.get(i);
                    }
                }
                if (isSelect) {
                    if ("call".equals(mType)) {
                        SPUtils.put(SPConstant.SP_CALL_SRHM, sortModel.getMobile());
                    } else if ("sms".equals(mType)) {
                        SPUtils.put(SPConstant.SP_SMS_SRHM, sortModel.getMobile());
                    }
                    finish();
                } else {
                    ToastUtil.show("请选择联系人");
                }
                break;
        }
    }
}