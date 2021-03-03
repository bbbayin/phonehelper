package com.mob.sms.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.ContactsAdapter;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.bean.ContactsBean;
import com.mob.sms.contacts.CharacterParser;
import com.mob.sms.contacts.PinyinComparator;
import com.mob.sms.contacts.SideBar;
import com.mob.sms.contacts.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TxlFragment extends BaseFragment {
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

    private int lastFirstVisibleItem = -1;

    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    private static Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_txl, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView(){
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();

        getContacts();

        List<SortModel> sortModels = filledData(mDatas);
        Collections.sort(sortModels, mPinyinComparator);

        mContactsAdapter = new ContactsAdapter(getContext(), sortModels);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mContactsAdapter);

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
            ContentResolver cr = getContext().getContentResolver();
            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
            while (cursor.moveToNext()) {
                mDatas.add(new ContactsBean(cursor.getString(cursor.getColumnIndex(NUM)), cursor.getString(cursor.getColumnIndex(NAME))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<SortModel> filledData(ArrayList<ContactsBean> datas) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < datas.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(datas.get(i).name);
            String pinyin = mCharacterParser.getSelling(datas.get(i).name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }
}
