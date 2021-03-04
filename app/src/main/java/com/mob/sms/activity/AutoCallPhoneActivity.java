package com.mob.sms.activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms.R;
import com.mob.sms.adapter.CallPhoneRecordsAdapter;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.bean.CallPhoneRecord;
import com.mob.sms.db.CallContactTable;
import com.mob.sms.db.DatabaseBusiness;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.pns.BaiduPnsServiceImpl;
import com.mob.sms.receiver.PhoneStateReceiver;
import com.mob.sms.rx.CallEvent;
import com.mob.sms.rx.RxBus;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;
import com.mob.sms.utils.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AutoCallPhoneActivity extends BaseActivity {
    @BindView(R.id.tip)
    TextView mTip;
    @BindView(R.id.mobile)
    TextView mMobile;
    @BindView(R.id.num)
    TextView mNum;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.success_num)
    TextView mSuccessNumTv;
    @BindView(R.id.fail_num)
    TextView mFailNumTv;
    @BindView(R.id.pause)
    ImageView mPause;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.record_ll)
    LinearLayout mRecoreLl;
    @BindView(R.id.pre)
    TextView mPre;
    @BindView(R.id.next)
    TextView mNext;

    private String mType;
    private int mSendNum;//总拨打次数
    private int mSendIndex;//当前拨打序列
    private int mInterval;//拨打间隔
    private String mDsbdTime;//定时拨打
    private int mShowDaojishi;//显示的倒计时时间
    private ArrayList<CallContactTable> mDatas = new ArrayList<>();//批量拨打联系人
    private CallPhoneRecordsAdapter mCallPhoneRecordsAdapter;
    private ArrayList<CallPhoneRecord> mRecords = new ArrayList<>();

    private boolean mPauseState;
    private boolean mSim1Call = true;// 双卡轮流拨打情况使用
    private TelephonyManager mTm;

    //指定SIM卡拨打
    private String[] dualSimTypes = { "subscription", "Subscription",
            "com.android.phone.extra.slot",
            "phone", "com.android.phone.DialingMode",
            "simId", "simnum", "phone_type",
            "simSlot" };

    private Subscription mSub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_call_phone);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(new PhoneStateReceiver(), intentFilter);
        initView();

        mSub =  RxBus.getInstance().toObserverable(CallEvent.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::nextCall);
    }

    private void useAxb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BaiduPnsServiceImpl pnsServiceImpl = new BaiduPnsServiceImpl();
                String data = pnsServiceImpl.bindingAxb("15851877564", "18020164573");
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String texlX = jsonObject1.getString("telX");
                    String bindId = jsonObject1.getString("bindId");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void nextCall(CallEvent event) {
        Log.i("jqt", "nextCall");
        if ("dhbd".equals(mType)) {
            if (mSendIndex + 1 < mSendNum) {
                mShowDaojishi = mInterval;
                mTime.setText("下一次拨打还需要" + mInterval + "s");
                mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                mTime.setText("下一次拨打还需要0s");
            }
        } else if ("plbd".equals(mType)) {
            if (mSendIndex + 1 < mSendNum) {
                mShowDaojishi = mInterval;
                mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(BATCH_CALL_MSG, 1000);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                mTime.setText("拨打下一个号码还需要0s");
            }
        }
    }

    private void initView(){
        mTm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
        mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        mType = getIntent().getStringExtra("type");
        if ("dhbd".equals(mType)) {
            mTip.setText("单号拨打电话");
            mMobile.setText(SPUtils.getString(SPConstant.SP_CALL_SRHM, ""));
            mSendNum = SPUtils.getInt(SPConstant.SP_CALL_NUM, 1);
            mDsbdTime  = SPUtils.getString(SPConstant.SP_CALL_TIMING, "");
            mInterval =  SPUtils.getInt(SPConstant.SP_CALL_INTERVAL, 20);

            if (mSendIndex < mSendNum) {
                if (TextUtils.isEmpty(mDsbdTime)) {
                    mShowDaojishi = mInterval;
                    mNum.setText("(" + 1 + "/" + mSendNum + "次)");
                    callPhone(SPUtils.getString(SPConstant.SP_CALL_SRHM, ""));
                    mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                } else if(mDsbdTime.contains("秒")){
                    mNum.setText("(0/" + mSendNum + "次)");
                    int hour = Integer.parseInt(mDsbdTime.split("时")[0]);
                    int min = Integer.parseInt(mDsbdTime.split("时")[1].split("分")[0]);
                    int sec = Integer.parseInt(mDsbdTime.split("时")[1].split("分")[1].split("秒")[0]);

                    long time = ((hour * 60 + min) * 60 + sec) * 1000;
                    mShowDaojishi = (int)(time/1000);
                    mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                    mHandler.sendEmptyMessageDelayed(2, 1000);
                } else {
                    mNum.setText("(0/" + mSendNum + "次)");
                    if(mDsbdTime.contains("上午")){
                        String date = mDsbdTime.split("上午")[0];
                        int hour = Integer.parseInt(mDsbdTime.split("上午")[1].split("时")[0]);
                        int min = Integer.parseInt(mDsbdTime.split("上午")[1].split("时")[1].split("分")[0]);
                        String sendDate = Utils.getYear() + "-" + date + " " + hour + ":" + min + ":" + "00";

                        long time = Utils.getTime(sendDate);
                        mShowDaojishi = (int)(time/1000);
                        mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                        mHandler.sendEmptyMessageDelayed(2, 1000);
                    } else if(mDsbdTime.contains("下午")){
                        String date = mDsbdTime.split("下午")[0];
                        int hour = Integer.parseInt(mDsbdTime.split("下午")[1].split("时")[0]);
                        int min = Integer.parseInt(mDsbdTime.split("下午")[1].split("时")[1].split("分")[0]);
                        String sendDate = Utils.getYear() + "-" + date + " " + (hour+12) + ":" + min + ":" + "00";

                        long time = Utils.getTime(sendDate);
                        mShowDaojishi = (int)(time/1000);
                        mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                        mHandler.sendEmptyMessageDelayed(2, 1000);
                    }
                }

            } else {
                mTime.setText("下一次拨打还需要" + 0 + "s");
            }
        } else if ("plbd".equals(mType)) {
            mRecoreLl.setVisibility(View.VISIBLE);
            mPre.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
            mTip.setText("批量拨打电话");
            mDatas.addAll(DatabaseBusiness.getCallContacts());

            for (CallContactTable callContactTable : mDatas) {
                mRecords.add(new CallPhoneRecord(callContactTable.name, callContactTable.mobile, false));
            }
            mCallPhoneRecordsAdapter = new CallPhoneRecordsAdapter(this, mRecords);
            mRecyclerView.setAdapter(mCallPhoneRecordsAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            mInterval =  SPUtils.getInt(SPConstant.SP_CALL_JGSZ, 0);
            if (mInterval < 0) {
                // 随机
                mInterval = Utils.getRandomNum(Math.abs(mInterval));
            }
            mShowDaojishi = mInterval;
            mSendNum = mDatas.size();
            mMobile.setText(mDatas.get(mSendIndex).mobile);
            mNum.setText("(" + 1 + "/" + mDatas.size() + "联系人)");
            callPhone(mDatas.get(mSendIndex).mobile);
            if (mSendIndex < mSendNum) {
                mTime.setText("拨打下一个号码还需要" + mShowDaojishi + "s");
                mHandler.sendEmptyMessageDelayed(BATCH_CALL_MSG, 1000);
            } else {
                mTime.setText("拨打下一个号码还需要" + 0 + "s");
            }
        }
    }

    private void callPhone(String mobile) {
        Log.i("jqt", "mobile: " + mobile);
        try{
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mobile));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String sksz = SPUtils.getString(SPConstant.SP_CALL_TYPE, "");
                List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                for (int i = 0; i < dualSimTypes.length; i++) {
                    //0代表卡1,1代表卡2
                    if ("sim1".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], 0);
                    } else if ("sim2".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], 1);
                    }  else if ("sim_double".equals(sksz)) {
                        intent.putExtra(dualSimTypes[i], mSim1Call?1:2);
                        mSim1Call = !mSim1Call;
                    }
                }

                ToastUtil.show(sksz + "," + phoneAccountHandleList.size());
                if(phoneAccountHandleList.size()>0){
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    Log.i("jqt", "sim: " + telephonyManager.getLine1Number());
                    ToastUtil.show("sim: " + telephonyManager.getLine1Number());
                    if ("sim1".equals(sksz)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(0));
                    } else if ("sim2".equals(sksz)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(1));
                    }  else if ("sim_double".equals(sksz)) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(mSim1Call?1:0));
                    }
                }
                startActivity(intent);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(2, 3000);
    }

    // 批量拨打
    private final int BATCH_CALL_MSG = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    //单号拨打  非定时
                    if (mSendIndex < mSendNum) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                            sendEmptyMessageDelayed(0, 1000);
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mSendNum + "次)");
                            mTime.setText("下一次拨打还需要" + mInterval + "s");
                            mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                            callPhone(SPUtils.getString(SPConstant.SP_CALL_SRHM, ""));
                        }
                    } else {
                        mTime.setText("下一次拨打还需要" + 0 + "s");
                    }
                    break;
                case BATCH_CALL_MSG:
                    //批量拨打
                    if (mSendIndex < mSendNum) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("拨打下一个号码还需要" + mShowDaojishi + "s");
                            sendEmptyMessageDelayed(BATCH_CALL_MSG, 1000);
                        } else {
                            mShowDaojishi = mInterval;
                            mSendIndex++;
                            mMobile.setText(mDatas.get(mSendIndex).mobile);
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mDatas.size() + "联系人)");
                            mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                            mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                            callPhone(mDatas.get(mSendIndex).mobile);
                        }
                    } else {
                        mTime.setText("拨打下一个号码还需要" + 0 + "s");
                    }
                    break;
                case 2:
                    //单号拨打，定时
                    if (mSendIndex < mSendNum) {
                        mShowDaojishi--;
                        if (mShowDaojishi > 0) {
                            mTime.setText("下一次拨打还需要" + mShowDaojishi + "s");
                            sendEmptyMessageDelayed(2, 1000);
                        } else {
                            mShowDaojishi = mInterval;
                            mNum.setText("(" + (mSendIndex + 1) + "/" + mSendNum + "次)");
                            mTime.setText("下一次拨打还需要" + mInterval + "s");
                            mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                            callPhone(SPUtils.getString(SPConstant.SP_CALL_SRHM, ""));
                        }
                    } else {
                        mTime.setText("下一次拨打还需要" + 0 + "s");
                    }
                    break;
            }
        }
    };

    @OnClick({R.id.back, R.id.pause, R.id.stop, R.id.pre, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.pause:
                mPauseState = !mPauseState;
                if (mPauseState) {
                    mHandler.removeMessages(0);
                } else {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
                break;
            case R.id.stop:
                finish();
                break;
            case R.id.pre:
                if(mSendIndex-1>=0){
                    mShowDaojishi = mInterval;
                    mSendIndex--;
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex + 1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                    callPhone(mDatas.get(mSendIndex).mobile);
                    mHandler.sendEmptyMessageDelayed(BATCH_CALL_MSG, 1000);
                }
                break;
            case R.id.next:
                if(mSendIndex+1<mDatas.size()) {
                    mShowDaojishi = mInterval;
                    mSendIndex++;
                    mMobile.setText(mDatas.get(mSendIndex).mobile);
                    mNum.setText("(" + (mSendIndex + 1) + "/" + mDatas.size() + "联系人)");
                    mTime.setText("拨打下一个号码还需要" + mInterval + "s");
                    mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                    callPhone(mDatas.get(mSendIndex).mobile);
                    mHandler.sendEmptyMessageDelayed(BATCH_CALL_MSG, 1000);
                }
                break;
        }
    }



    private PhoneStateListener listener=new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            if ("plbd".equals(mType)) {
                mRecords.get(mSendIndex).isSend = true;
                mCallPhoneRecordsAdapter.notifyDataSetChanged();
            }
            Log.i("jqt", "state: " + state + "," + incomingNumber);
            mTm.listen(listener,PhoneStateListener.LISTEN_NONE);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        saveRecord();
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }

    private void saveRecord(){
        int allNum = 0;
        String tel = "";
        if("dhbd".equals(mType)){
            allNum = 1;
            tel = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        } else if("plbd".equals(mType)){
            allNum = mDatas.size();
            for (CallContactTable callContactTable:mDatas){
                if(TextUtils.isEmpty(tel)){
                    tel = callContactTable.mobile;
                } else {
                    tel = tel + "," + callContactTable.mobile;
                }
            }
        }
        String status;
        if (mSendIndex == 0) {
            status = "-1";
        } else if (mSendIndex < mSendNum) {
            status = "0";
        } else {
            status = "1";
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(System.currentTimeMillis()));
        if("dhbd".equals(mType)){
            RetrofitHelper.getApi().saveCallRecord(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""),
                    allNum, time, mInterval + "", mSendNum,  SPUtils.getString(SPConstant.SP_CALL_TYPE, ""),
                    SPUtils.getBoolean(SPConstant.SP_CALL_GD, false)?"1":"0",status,
                    mSendIndex+1, tel, mDsbdTime,SPUtils.getBoolean(SPConstant.SP_CALL_GD, false)?"0":"1").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else if("plbd".equals(mType)){
            RetrofitHelper.getApi().savePlCallRecord(SPUtils.getString(SPConstant.SP_USER_TOKEN, ""),
                    allNum, SPUtils.getString(SPConstant.SP_CALL_SKSZ, "sim1"),time,
                    mInterval+"",
                    SPUtils.getBoolean(SPConstant.SP_CALL_PL_GD, false)?"1":"0",status,
                    mSendIndex+1, tel, SPUtils.getBoolean(SPConstant.SP_CALL_PL_GD, false)?"0":"1").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseBean -> {
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        }
    }
}
