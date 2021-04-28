package com.mob.sms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mob.sms.activity.SetSecretInfoActivity;
import com.mob.sms.activity.VipActivity;
import com.mob.sms.auto.SingleAutoTaskActivity;
import com.mob.sms.bean.CloudPermissionBean;
import com.mob.sms.bean.HomeFuncBean;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.network.bean.BaseResponse;
import com.mob.sms.pns.BaiduPnsServiceImpl;
import com.mob.sms.utils.FreeCheckUtils;
import com.mob.sms.utils.MyItemDecoration;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.ToastUtil;
import com.mob.sms.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DialKeyBoard extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final List<DialKeyBean> keys = new ArrayList<>();
    private TextView tvInput;
    private View secretLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dial_keyboard_layout, container, false);
        tvInput = rootView.findViewById(R.id.dial_keyboard_input);
        RecyclerView recyclerView = rootView.findViewById(R.id.dial_keyboard_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.addItemDecoration(new MyItemDecoration(20));
        initData();
        DialKeyAdapter adapter = new DialKeyAdapter();
        adapter.setCallBack(new KeyCallBack() {
            @Override
            public void onKeyClick(String number, int type) {
                if (type == DialKeyBean.TYPE_DELETE) {
                    delete();
                } else if (type == DialKeyBean.TYPE_EMPTY) {

                } else {
                    input(number);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        rootView.findViewById(R.id.dial_sim_1).setOnClickListener(this);
        rootView.findViewById(R.id.dial_sim_2).setOnClickListener(this);
        secretLayout = rootView.findViewById(R.id.dial_sim_secret);
        secretLayout.setOnClickListener(this);

        return rootView;
    }

    private void delete() {
        String s = tvInput.getText().toString();
        if (!TextUtils.isEmpty(s)) {
            tvInput.setText(s.substring(0, s.length() - 1));
        }
    }

    private void input(String number) {
        String s = tvInput.getText().toString();
        tvInput.setText(s + number);
    }

    @Override
    public void onClick(View v) {
        String number = tvInput.getText().toString();
        if (TextUtils.isEmpty(number)) {
            ToastUtil.show("请输入号码");
            return;
        } else if (number.length() != 11) {
            ToastUtil.show("手机号正确");
            return;
        }
        FreeCheckUtils.check(getActivity(), new FreeCheckUtils.OnCheckCallback() {
            @Override
            public void onResult(boolean free) {
                if (free) {
                    switch (v.getId()) {
                        case R.id.dial_sim_1:
                            callPhone(number, 0);
                            break;
                        case R.id.dial_sim_2:
                            callPhone(number, 1);
                            break;
                        case R.id.dial_sim_secret:
                            callPhoneSecret();
                            break;
                    }
                } else {
                    startActivity(new Intent(getContext(), VipActivity.class));
                }
            }
        });

    }

    private void callPhoneSecret() {
        String phone = (String) SPUtils.get(SPConstant.SP_USER_PHONE, "");
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.show("请先设置隐私信息");
            Intent intent = new Intent(getContext(), SetSecretInfoActivity.class);
            startActivityForResult(intent, 1234);
        } else {
            bindSecretNumber();
        }
    }

    private void checkPermission() {
        RetrofitHelper.getApi().cloudDial()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CloudPermissionBean>() {
                    @Override
                    public void call(CloudPermissionBean permissionBean) {
                        if (permissionBean != null && "200".equals(permissionBean.code)) {
                            // 有权限

                        } else if ("500".equals(permissionBean.code)) {
                            ToastUtil.show(permissionBean.msg);
                        } else {
                            startActivity(new Intent(getContext(), VipActivity.class));
                        }
                    }
                });
    }

    /**
     * 获取隐私号
     */
    private void bindSecretNumber() {
        String phone = SPUtils.getString(SPConstant.SP_USER_PHONE, "");
        if (TextUtils.isEmpty(phone)) {
            Utils.showDialog(getActivity(), "请先设置隐私拨号信息", "提示",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getActivity(), SetSecretInfoActivity.class));
                        }
                    },
                    null);
        } else {
            showProgress("获取隐私号码...");
            new Thread() {
                @Override
                public void run() {
                    BaiduPnsServiceImpl impl = new BaiduPnsServiceImpl();
                    String callNumber = tvInput.getText().toString();
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

    private String secretNumber;

    private void bindTelxSuccess(String telX) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                secretNumber = telX;
                ToastUtil.show("隐私号获取成功");
                int sim = SPUtils.getInt(SPConstant.SP_SECRET_SIM_NO, 0);
                callPhone(secretNumber, sim);
            }
        });
    }

    private void bindTelxFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showDialog(getActivity(), "隐私号码获取失败，请重试",
                        "提示", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bindSecretNumber();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
            }
        });
    }

    private ProgressDialog progressDialog;

    private void showProgress(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(msg);
            progressDialog.show();
        }
    }

    private void hideProgress() {
        getActivity().runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }


    /**
     * 打电话
     */
    private void callPhone(String number, int sim) {
        try {
            if (TextUtils.isEmpty(number)) {
                ToastUtil.show("请输入号码");
            } else if (number.length() != 11) {
                ToastUtil.show("手机号正确");
            } else {
                // 1. 获取拨打的sim卡
                TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + number));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    List<PhoneAccountHandle> phoneAccountHandleList = Utils.getAccountHandles(getContext());
                    if (phoneAccountHandleList.size() > sim) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList.get(sim));
                        startActivityForResult(intent, 888);
                    } else {
                        ToastUtil.show("请插入sim卡");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface KeyCallBack {
        void onKeyClick(String number, int type);
    }

    private static class DialKeyAdapter extends RecyclerView.Adapter<KeyHolder> {
        private KeyCallBack callBack;

        public void setCallBack(KeyCallBack callBack) {
            this.callBack = callBack;
        }

        @NonNull
        @Override
        public KeyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dial_key_layout, parent, false);

            return new KeyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull KeyHolder holder, int position) {
            DialKeyBean keyBean = keys.get(position);
            holder.tvNumber.setVisibility(View.VISIBLE);
            holder.tvEn.setVisibility(View.VISIBLE);
            holder.btDelete.setVisibility(View.GONE);

            if (keyBean.type == DialKeyBean.TYPE_EMPTY) {
                holder.tvNumber.setText("");
                holder.tvEn.setText("");
            } else if (keyBean.type == DialKeyBean.TYPE_ZERO) {
                holder.tvNumber.setText("0");
                holder.tvEn.setText("");
            } else if (keyBean.type == DialKeyBean.TYPE_DELETE) {
                holder.tvNumber.setVisibility(View.GONE);
                holder.tvEn.setVisibility(View.GONE);
                holder.btDelete.setVisibility(View.VISIBLE);
            } else {
                holder.tvNumber.setText(keyBean.number);
                holder.tvEn.setText(keyBean.en);
            }
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.onKeyClick(keyBean.number, keyBean.type);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return keys.size();
        }
    }

    private static class KeyHolder extends RecyclerView.ViewHolder {

        private final TextView tvNumber;
        private final TextView tvEn;
        private final View btDelete;

        public KeyHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.dial_key_number);
            tvEn = itemView.findViewById(R.id.dial_key_en);
            btDelete = itemView.findViewById(R.id.dial_key_delete);
        }
    }


    private String[] Ens = {"", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUY", "WXYZ", "", "", ""};

    private void initData() {
        keys.clear();
        for (int i = 0; i < 12; i++) {
            DialKeyBean key = new DialKeyBean();
            key.type = 1;
            key.number = String.valueOf(i + 1);
            key.en = Ens[i];
            keys.add(key);
        }

        keys.get(9).number = "";
        keys.get(9).type = DialKeyBean.TYPE_EMPTY;
        keys.get(10).number = "0";
        keys.get(10).type = DialKeyBean.TYPE_ZERO;
        keys.get(11).type = DialKeyBean.TYPE_DELETE;

        RetrofitHelper.getApi().getThreadInfo().subscribe(new Action1<BaseResponse<HomeFuncBean>>() {
            @Override
            public void call(BaseResponse<HomeFuncBean> response) {
                if (response != null && response.data != null && TextUtils.equals(response.data.status, "1")) {
                    secretLayout.setVisibility(View.VISIBLE);
                } else {
                    secretLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private static class DialKeyBean {
        public String number = null;
        public String en = null;
        // 888: 正常号码，-1：空白，2：删除
        public int type = 888;

        public static int TYPE_EMPTY = -1;
        public static int TYPE_ZERO = 0;
        public static int TYPE_DELETE = 2;
    }
}
