package com.mob.sms.auto;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.activity.SetSecretInfoActivity;
import com.mob.sms.base.BaseActivity;
import com.mob.sms.databinding.ActivityAutoCallPhoneBinding;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.pns.BaiduPnsServiceImpl;
import com.mob.sms.receiver.PhoneStateReceiver;
import com.mob.sms.service.CallService;
import com.mob.sms.utils.Constants;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;
import com.mob.sms.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 单一任务：单号拨打/发短信
 */
public class SingleAutoTaskActivity extends BaseActivity {

    public static boolean phoneIdleFlag = false;

    private ActivityAutoCallPhoneBinding binding;
    private String mDialNumber;// 要拨打的电话
    private int mTotalCallTimes;// 拨打次数

    private int mInterval;
    // 当前页面任务类型
    public static String KEY_TASK = "key+task";
    public final static int VALUE_TASK_DIAL = 1;// 打电话
    public final static int VALUE_TASK_SEND_MSG = 2;// 发短信
    private int mTaskType;// 打电话/发短信
    private long mDelay;// 延迟开始
    private int mCurrentCount = 1;// 当前第几次任务
    private CountDownTimer timer;
    private boolean isRunning = false;// 倒计时是否在执行
    private boolean isManualStop = false;// 手动停止
    private int mSim;// 设置的sim卡
    private boolean mIsSecretDial;// 使用使用隐私拨打
    private ProgressDialog progressDialog;

    private PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.i("jjjjjj", "state: " + state + "," + incomingNumber);
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
        }

    };
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // viewBinding
        binding = ActivityAutoCallPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(getResources().getColor(R.color.green));
        // init
        initView();
        initClick();
        initData();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initData() {
        mTaskType = getIntent().getIntExtra(KEY_TASK, VALUE_TASK_DIAL);
        if (mTaskType == VALUE_TASK_DIAL) {
            binding.tip.setText("单号拨打电话");
            // 判断是否隐私拨打
            String type = SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
            if (Constants.SIM_TYPE_SECRET.equals(type)) {
                // 隐私拨号
                mIsSecretDial = true;
                mSim = SPUtils.getInt(SPConstant.SP_SECRET_SIM_NO, 0);
                bindSecretNumber();
            } else {
                // 正常拨打
                mIsSecretDial = false;
                if (Constants.SIM_TYPE_SIM_1.equals(type)) {
                    mSim = 0;
                } else {
                    mSim = 1;
                }
                startTimer(mDelay);
            }
        } else {
            binding.tip.setText("单号发短信");
        }
    }

    /**
     * 获取隐私号
     */
    private void bindSecretNumber() {
        String phone = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        if (TextUtils.isEmpty(phone)) {
            Utils.showDialog(this, "请先设置隐私拨号信息", "提示",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(SingleAutoTaskActivity.this, SetSecretInfoActivity.class));
                        }
                    },
                    null);
        } else {
            showProgress("获取隐私号码...");
            new Thread() {
                @Override
                public void run() {
                    BaiduPnsServiceImpl impl = new BaiduPnsServiceImpl();
                    String callNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
                    String s = impl.bindingAxb(phone, callNumber);
                    Log.d("绑定隐私号结果", s);
                    hideProgress();
                    //{"code":"0","msg":"成功","data":{"bindId":"2411790078574043902","telX":"18468575717"}}
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject data = jsonObject.optJSONObject("data");
                        if (data != null) {
                            String telX = data.optString("telX");
                            if (!TextUtils.isEmpty(telX)) {
                                bindTelxSuccess(telX);
                            } else {
                                bindTelxFailed();
                            }
                        } else {
                            bindTelxFailed();
                        }
                    } catch (JSONException e) {
                        bindTelxFailed();
                    }
                }
            }.start();

        }
    }

    private void bindTelxSuccess(String telX) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialNumber = telX;
                ToastUtil.show("隐私号获取成功");
                startTimer(mDelay);
            }
        });
    }

    private void bindTelxFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showDialog(SingleAutoTaskActivity.this, "隐私号码获取失败，请重试",
                        "提示", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bindSecretNumber();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
            }
        });
    }

    private void showProgress(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(msg);
            if (isDestroyed() || isFinishing()) {
                return;
            }
            progressDialog.show();
        }
    }

    private void hideProgress() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume----" + System.currentTimeMillis());
        if (mCurrentCount > 1 && phoneIdleFlag) {
            startTimer(mInterval);
            phoneIdleFlag = false;
        }
    }

    private synchronized void startTimer(long duration) {
        if (isRunning && isManualStop) return;
        if (mCurrentCount > mTotalCallTimes) {
            binding.time.setText("拨打结束");
            return;
        }
        binding.num.setText(String.format("(%s/%s次)", mCurrentCount, mTotalCallTimes));
        System.out.println(String.format("开启任务，%s/%s", mCurrentCount, mTotalCallTimes));
        release();

        timer = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.time.setText(String.format("下一次拨打还需要%ss", millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                // 执行任务
                executeTask();
                mCurrentCount++;
            }
        };
        timer.start();
        isRunning = true;
    }

    /**
     * 拨打电话/发短信
     */
    private void executeTask() {
        if (mTaskType == VALUE_TASK_DIAL) {
            callPhone();
        } else {
            sendMsg();
        }
    }

    private void sendMsg() {
// TODO: 2021/3/14
    }

    private void initView() {
        telephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mDialNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        binding.tip.setText("单号拨打电话");
        binding.mobile.setText(mDialNumber);
        // 总共拨打次数
        mTotalCallTimes = SPUtils.getInt(SPConstant.SP_CALL_NUM, 1);
        // 定时时间
        String delayTimeStr = SPUtils.getString(SPConstant.SP_CALL_TIMING, "");
        // 时间间隔
        mInterval = SPUtils.getInt(SPConstant.SP_CALL_INTERVAL, 20);
        binding.num.setText(String.format("(%s/%s次)", mCurrentCount, mTotalCallTimes));
        mDelay = 0;
        if (delayTimeStr.contains("秒")) {
            int hour = Integer.parseInt(delayTimeStr.split("时")[0]);
            int min = Integer.parseInt(delayTimeStr.split("时")[1].split("分")[0]);
            int sec = Integer.parseInt(delayTimeStr.split("时")[1].split("分")[1].split("秒")[0]);
            long time = ((hour * 60 + min) * 60 + sec) * 1000;
            // 下次拨打倒计时
            mDelay = time / 1000;
            binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
        } else {
            if (delayTimeStr.contains("上午")) {
                String date = delayTimeStr.split("上午")[0];
                int hour = Integer.parseInt(delayTimeStr.split("上午")[1].split("时")[0]);
                int min = Integer.parseInt(delayTimeStr.split("上午")[1].split("时")[1].split("分")[0]);
                String sendDate = Utils.getYear() + "-" + date + " " + hour + ":" + min + ":" + "00";

                long time = Utils.getTime(sendDate);
                mDelay = (time / 1000);
                binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
            } else if (delayTimeStr.contains("下午")) {
                String date = delayTimeStr.split("下午")[0];
                int hour = Integer.parseInt(delayTimeStr.split("下午")[1].split("时")[0]);
                int min = Integer.parseInt(delayTimeStr.split("下午")[1].split("时")[1].split("分")[0]);
                String sendDate = Utils.getYear() + "-" + date + " " + (hour + 12) + ":" + min + ":" + "00";

                long time = Utils.getTime(sendDate);
                mDelay = time / 1000;
                binding.time.setText(String.format("下一次拨打还需要%ss", mDelay));
            }
        }
        // 注册电话广播监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("CustomAction.PRECISE_CALL_STATE");
        registerReceiver(new PhoneStateReceiver(), intentFilter);
    }

    private void initClick() {
        binding.back.setOnClickListener(v -> back());
        binding.stop.setOnClickListener(v -> back());
        binding.pause.setOnClickListener(v -> {
            // 手动暂停
            if (isManualStop) {
                startTimer(mInterval);
            } else {
                release();
                binding.time.setText("暂停拨打");
            }
            isManualStop = !isManualStop;
        });
    }

    /**
     * 打电话
     */
    private void callPhone() {
        try {
            // 1. 获取拨打的sim卡
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mDialNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                List<PhoneAccountHandle> phoneAccountHandleList = Utils.getAccountHandles(this);
                if (phoneAccountHandleList.size() > mSim) {
                    intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(mSim));
                }
                startActivityForResult(intent, 888);
                isRunning = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("收到结果：" + requestCode + "   res=" + resultCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
    }

    private void back() {
        finish();
        saveRecord();
    }

    @Override
    public void onBackPressed() {

    }

    private void saveRecord() {
        String status;
        if (mCurrentCount == 0) {
            status = "-1";
        } else if (mCurrentCount < mTotalCallTimes) {
            status = "0";
        } else {
            status = "1";
        }
        String time = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(System.currentTimeMillis()));
        String mDsbdTime = SPUtils.getString(SPConstant.SP_CALL_TIMING, "");
        Boolean gd = SPUtils.getBoolean(SPConstant.SP_CALL_GD, false);
        RetrofitHelper.getApi().saveCallRecord(1, time, mInterval + "", mTotalCallTimes,
                mSim == 0 ? Constants.SIM_TYPE_SIM_1 : Constants.SIM_TYPE_SIM_2,
                gd ? "1" : "0", status,
                mCurrentCount + 1, mDialNumber, mDsbdTime, gd ? "0" : "1").subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseBean -> {
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }
}
