package com.mob.sms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mob.sms.auto.SingleAutoTaskActivity;

public class PhoneStateReceiver extends BroadcastReceiver {
    private String TAG = "xxxxx";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("xxxxx", "onReceive: " + intent.getAction() + "--------");
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d("xxxxx", "电话状态：" + state);

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 去电，可以用定时挂断
        } else {
            //来电
            Log.i("jqt", "PhoneStateReceiver onReceive state: " + state);
            if ("IDLE".equals(state)) {
                SingleAutoTaskActivity.phoneIdleFlag = true;
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.i("jqt", "PhoneStateReceiver onReceive endCall");
            }
        }

        if (intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case "CustomAction.PRECISE_CALL_STATE":
                int callState = intent.getIntExtra("foreground_state", -2);
                switch (callState) {
                    case PreciseCallState.PRECISE_CALL_STATE_IDLE:
                        Log.d(TAG, "IDLE");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_DIALING:
                        Log.d(TAG, "DIALING");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_ALERTING:
                        Log.d(TAG, "ALERTING isHandFree=");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_ACTIVE:
                        Log.d(TAG, "ACTIVE");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_INCOMING:
                        Log.d(TAG, "INCOMING来电");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_DISCONNECTING:
                        Log.d(TAG, "DISCONNECTING");
                        break;
                    case PreciseCallState.PRECISE_CALL_STATE_DISCONNECTED:
                        Log.d(TAG, "DISCONNECTED");
                        break;
                }
                break;
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                break;
        }
    }

    public static class PreciseCallState {
        public static final int PRECISE_CALL_STATE_IDLE = 0; //通话空闲
        public static final int PRECISE_CALL_STATE_ACTIVE = 1; //正在通话(活动中)
        public static final int PRECISE_CALL_STATE_HOLDING = 2; //通话挂起(例如我和多个人通话,其中一个通话在活动,而其它通话就会进入挂起状态)
        public static final int PRECISE_CALL_STATE_DIALING = 3; //拨号开始
        public static final int PRECISE_CALL_STATE_ALERTING = 4; //正在呼出(提醒对方接电话)
        public static final int PRECISE_CALL_STATE_INCOMING = 5; //对方来电
        public static final int PRECISE_CALL_STATE_WAITING = 6; //第三方来电等待(例如我正在和某人通话,而其他人打入时就会就进入等待状态)
        public static final int PRECISE_CALL_STATE_DISCONNECTED = 7; //挂断完成
        public static final int PRECISE_CALL_STATE_DISCONNECTING = 8; //正在挂断
    }
}
