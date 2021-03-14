package com.mob.sms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mob.sms.auto.SingleAutoTaskActivity;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("jqt", "onReceive: " + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 去电，可以用定时挂断
        } else {
            //来电
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.i("jqt", "PhoneStateReceiver onReceive state: " + state);
            if ("IDLE".equals(state)) {
                SingleAutoTaskActivity.phoneIdleFlag = true;
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.i("jqt", "PhoneStateReceiver onReceive endCall");
            }
        }
    }
}
