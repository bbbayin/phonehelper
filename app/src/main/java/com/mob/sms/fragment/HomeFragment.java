package com.mob.sms.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mob.sms.R;
import com.mob.sms.activity.AutoCallPhoneActivity;
import com.mob.sms.activity.AutoSendSmsActivity;
import com.mob.sms.activity.CallTypeActivity;
import com.mob.sms.activity.ContactsActivity;
import com.mob.sms.activity.CopyImportActivity;
import com.mob.sms.activity.DocImportActivity;
import com.mob.sms.activity.EditSmsActivity;
import com.mob.sms.activity.ImportContactsActivity;
import com.mob.sms.activity.SimSettingActivity;
import com.mob.sms.activity.VipActivity;
import com.mob.sms.auto.SingleAutoTaskActivity;
import com.mob.sms.base.BaseFragment;
import com.mob.sms.bean.CloudPermissionBean;
import com.mob.sms.db.CallContactTable;
import com.mob.sms.db.DatabaseBusiness;
import com.mob.sms.db.SmsContactTable;
import com.mob.sms.dialog.DocImportDialog;
import com.mob.sms.dialog.ImportDialog;
import com.mob.sms.dialog.SetCallIntervalDialog;
import com.mob.sms.dialog.SetCallNumDialog;
import com.mob.sms.dialog.SetCallTimingDialog;
import com.mob.sms.dialog.SetMultiCallIntervalDialog;
import com.mob.sms.network.RetrofitHelper;
import com.mob.sms.utils.Constants;
import com.mob.sms.utils.SPConstant;
import com.mob.sms.utils.SPUtils;
import com.mob.sms.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class HomeFragment extends BaseFragment {
    @BindView(R.id.bddh_ll)
    LinearLayout bddh_ll;
    @BindView(R.id.plbd_ll)
    LinearLayout plbd_ll;
    @BindView(R.id.dxds_ll)
    LinearLayout dxds_ll;
    @BindView(R.id.bddh_iv)
    ImageView bddh_iv;
    @BindView(R.id.plbd_iv)
    ImageView plbd_iv;
    @BindView(R.id.dxds_iv)
    ImageView dxds_iv;
    //电话
    @BindView(R.id.call_mobile_et)
    EditText mCallMobileEt;
    @BindView(R.id.dsbh_tv)
    TextView mCallDsbhTv;
    @BindView(R.id.bdcs_tv)
    TextView mCallBdcsTv;
    @BindView(R.id.bdjg_tv)
    TextView mCallBdjgTv;
    @BindView(R.id.bdfs_tv)
    TextView mCallBdfsTv;
    @BindView(R.id.gd_switch)
    ImageView mGdSwitch;
    @BindView(R.id.gd_switch2)
    ImageView mGdSwitch2;
    @BindView(R.id.home_btn_single_call_now)
    TextView mCallTv;
    //批量拨打
    @BindView(R.id.call_hmdr_tip)
    TextView mCallHmdrTip;
    @BindView(R.id.call_sksz_tip)
    TextView mCallSkszTip;
    @BindView(R.id.call_jgsz_tip)
    TextView mCallJgszTip;
    @BindView(R.id.pl_switch_gd)
    ImageView mPlGdSwitch;
    @BindView(R.id.pl_switch_gd2)
    ImageView mPlGdSwitch2;
    @BindView(R.id.pl_call_tv)
    TextView mPlCallTv;
    //短信
    @BindView(R.id.dhfs_switch)
    ImageView dhfs_switch;
    @BindView(R.id.dhfs_ll)
    LinearLayout dhfs_ll;
    @BindView(R.id.sms_mobile_et)
    EditText mSmsMobileEt;
    @BindView(R.id.sms_dsfs_tip)
    TextView mSmsDsfsTip;
    @BindView(R.id.sms_fscs_tip)
    TextView mSmsFscsTip;
    @BindView(R.id.plfs_switch)
    ImageView plfs_switch;
    @BindView(R.id.plfs_ll)
    LinearLayout plfs_ll;
    @BindView(R.id.sms_hmdr_tip)
    TextView mSmsHmdrTip;
    @BindView(R.id.sms_sksz_tip)
    TextView mSmsSkszTip;
    @BindView(R.id.sms_fsjg_tip)
    TextView mSmsFsjgTip;
    @BindView(R.id.bjdx_tip)
    TextView mBjdxTip;
    @BindView(R.id.sms_ljfs)
    TextView mSmsLjfs;
    @BindView(R.id.home_single_btn_clear_phone)
    ImageView ivClearSinglePhoneNumber;
    @BindView(R.id.home_single_btn_clear_time)
    ImageView ivClearTime;
    @BindView(R.id.multi_iv_clear_interval)
    ImageView ivMultiClearInterval;
    @BindView(R.id.sms_iv_clear_phone)
    ImageView ivClearSmsPhone;
    @BindView(R.id.sms_iv_clear_timeout)
    ImageView ivClearSmsTimeout;

    private final int REQUEST_CODE_TAB1_SRHM = 1;
    private final int REQUEST_CODE_TAB1_CALL_TYPE = 2;
    private boolean sms_dhfs_open = true;

    private int mVisibleTab = 0;//当前切换的tab
    private boolean mCallGd = true;//默认接通后自动挂断
    private boolean mPlCallGd = true;//默认接通后自动挂断

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        mSmsDsfsTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String timeout = s.toString();
                if (TextUtils.isEmpty(timeout)) {
                    ivClearSmsTimeout.setVisibility(View.GONE);
                } else {
                    ivClearSmsTimeout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void setBdfs() {
        String call_type = SPUtils.getString(SPConstant.SP_SIM_CARD_TYPE, Constants.SIM_TYPE_SIM_1);
        if (Constants.SIM_TYPE_SIM_1.equals(call_type)) {
            mCallBdfsTv.setText("使用手机卡1拨打");
        } else if (Constants.SIM_TYPE_SIM_2.equals(call_type)) {
            mCallBdfsTv.setText("使用手机卡2拨打");
        } else if (Constants.SIM_TYPE_SECRET.equals(call_type)) {
            mCallBdfsTv.setText("使用隐私号拨打");
        }
    }

    @OnClick({R.id.top1_ll, R.id.top2_ll, R.id.top3_ll, R.id.txl_iv, R.id.dsbh_rl, R.id.bdcs_rl, R.id.bdjg_rl, R.id.bdfs_rl,
            R.id.gd_switch, R.id.gd_switch2, R.id.home_btn_single_call_now,
            R.id.hmdr_rl, R.id.skzs_rl, R.id.jgsz_rl, R.id.pl_switch_gd, R.id.pl_switch_gd2, R.id.pl_call_tv,
            R.id.dhfs_switch, R.id.sms_txl_iv, R.id.sms_dsfs_rl, R.id.sms_fscs_rl, R.id.plfs_switch,
            R.id.sms_hmdr_rl, R.id.sms_sksz_rl, R.id.sms_fsjg_rl, R.id.bjdx_rl, R.id.sms_ljfs, R.id.home_single_btn_clear_phone,
            R.id.home_single_btn_clear_time, R.id.multi_iv_clear_interval, R.id.sms_iv_clear_phone, R.id.sms_iv_clear_timeout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sms_iv_clear_timeout:// 短信，清除定时发送
                ivClearSmsTimeout.setVisibility(View.GONE);
                mSmsDsfsTip.setText("");
                changeSmsUi();
                break;
            case R.id.sms_iv_clear_phone:// 发短信，清除号码
                mSmsMobileEt.setText("");
                changeSmsUi();
                break;
            case R.id.multi_iv_clear_interval:// 批量拨打，清除间隔
                mCallJgszTip.setText("");
                SPUtils.remove(SPConstant.SP_CALL_JGSZ);
                ivMultiClearInterval.setVisibility(View.GONE);
                changePlCallUi();
                break;
            case R.id.home_single_btn_clear_time:// 清除定时
                SPUtils.remove(SPConstant.SP_CALL_TIMING);
                mCallDsbhTv.setText("");
                ivClearTime.setVisibility(View.GONE);
                changeCallUi();
                break;
            case R.id.home_single_btn_clear_phone:// 清除手机号
                SPUtils.remove(SPConstant.SP_CALL_SRHM);
                mCallMobileEt.setText("");
                ivClearSinglePhoneNumber.setVisibility(View.GONE);
                changeCallUi();
                break;
            case R.id.top1_ll:
                mVisibleTab = 0;
                bddh_ll.setVisibility(View.VISIBLE);
                plbd_ll.setVisibility(View.GONE);
                dxds_ll.setVisibility(View.GONE);
                bddh_iv.setBackgroundResource(R.mipmap.bddh_green);
                plbd_iv.setBackgroundResource(R.mipmap.plbd_grey);
                dxds_iv.setBackgroundResource(R.mipmap.dxds_grey);
                break;
            case R.id.top2_ll:
                mVisibleTab = 1;
                bddh_ll.setVisibility(View.GONE);
                plbd_ll.setVisibility(View.VISIBLE);
                dxds_ll.setVisibility(View.GONE);
                bddh_iv.setBackgroundResource(R.mipmap.bddh_grey);
                plbd_iv.setBackgroundResource(R.mipmap.plbd_green);
                dxds_iv.setBackgroundResource(R.mipmap.dxds_grey);
                break;
            case R.id.top3_ll:
                mVisibleTab = 2;
                bddh_ll.setVisibility(View.GONE);
                plbd_ll.setVisibility(View.GONE);
                dxds_ll.setVisibility(View.VISIBLE);
                bddh_iv.setBackgroundResource(R.mipmap.bddh_grey);
                plbd_iv.setBackgroundResource(R.mipmap.plbd_grey);
                dxds_iv.setBackgroundResource(R.mipmap.dxds_green);
                break;
            case R.id.txl_iv:
                Intent intent = new Intent(getContext(), ContactsActivity.class);
                intent.putExtra("type", "call");
                startActivity(intent);
                break;
            case R.id.dsbh_rl:
                SetCallTimingDialog setCallTimingDialog = new SetCallTimingDialog(getContext(), "call");
                setCallTimingDialog.show();
                setCallTimingDialog.setOnClickListener(new SetCallTimingDialog.OnClickListener() {
                    @Override
                    public void confirm(String value) {
                        SPUtils.put(SPConstant.SP_CALL_TIMING, value);
                        mCallDsbhTv.setText(value + "后拨打");
                        ivClearTime.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.bdcs_rl:
                SetCallNumDialog setCallNumDialog = new SetCallNumDialog(getContext(), "call");
                setCallNumDialog.show();
                setCallNumDialog.setOnClickListener(new SetCallNumDialog.OnClickListener() {
                    @Override
                    public void confirm(int num) {
                        SPUtils.put(SPConstant.SP_CALL_NUM, num);
                        mCallBdcsTv.setText(num + "次");
                        changeCallUi();
                    }
                });
                break;
            case R.id.bdjg_rl:
                // 拨打间隔
                SetCallIntervalDialog setCallIntervalDialog = new SetCallIntervalDialog(getContext(), "call");
                setCallIntervalDialog.show();
                setCallIntervalDialog.setOnClickListener(new SetCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(int second) {
                        SPUtils.put(SPConstant.SP_CALL_INTERVAL, second);
                        mCallBdjgTv.setText(second + "s");
                        changeCallUi();
                    }
                });
                break;
            case R.id.bdfs_rl:
                intent = new Intent(getContext(), CallTypeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TAB1_CALL_TYPE);
                break;
            case R.id.gd_switch:
                mCallGd = !mCallGd;
                mGdSwitch.setImageResource(mCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                mGdSwitch2.setImageResource(mCallGd ? R.mipmap.switch_off : R.mipmap.switch_on);
                SPUtils.put(SPConstant.SP_CALL_GD, mCallGd);
                break;
            case R.id.gd_switch2:
                mCallGd = !mCallGd;
                mGdSwitch.setImageResource(mCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                mGdSwitch2.setImageResource(mCallGd ? R.mipmap.switch_off : R.mipmap.switch_on);
                SPUtils.put(SPConstant.SP_CALL_GD, mCallGd);
                break;
            case R.id.home_btn_single_call_now:
                if (!TextUtils.isEmpty(mCallMobileEt.getText().toString()) &&
                        !TextUtils.isEmpty(mCallBdcsTv.getText().toString()) &&
                        !TextUtils.isEmpty(mCallBdjgTv.getText().toString()) &&
                        !TextUtils.isEmpty(mCallBdfsTv.getText().toString())) {
                    // 判断是否隐私拨号
                    String s = mCallBdfsTv.getText().toString();
                    if (s.contains("隐私")) {
                        checkPermission();
                    } else {
                        intent = new Intent(getContext(), SingleAutoTaskActivity.class);
                        intent.putExtra("type", "dhbd");
                        startActivity(intent);
                    }
                }
                break;
            case R.id.hmdr_rl:
                ImportDialog importDialog = new ImportDialog(getContext());
                importDialog.show();
                importDialog.setOnClickListener(new ImportDialog.OnClickListener() {
                    @Override
                    public void wordImport() {
                        importDialog.dismiss();
                        DocImportDialog docImportDialog = new DocImportDialog(getContext());
                        docImportDialog.show();
                        docImportDialog.setOnClickListener(new DocImportDialog.OnClickListener() {
                            @Override
                            public void wordImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "word");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void excelImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "excel");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void txtImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "txt");
                                intent1.putExtra("type2", "call");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void txlImport() {
                        Intent intent1 = new Intent(getContext(), ImportContactsActivity.class);
                        intent1.putExtra("type", "call");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }

                    @Override
                    public void copyImport() {
                        Intent intent1 = new Intent(getContext(), CopyImportActivity.class);
                        intent1.putExtra("type", "call");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }
                });
                break;
            case R.id.skzs_rl:
                intent = new Intent(getContext(), SimSettingActivity.class);
                intent.putExtra("type", "call");
                startActivity(intent);
                break;
            case R.id.jgsz_rl:
                SetMultiCallIntervalDialog setMultiCallIntervalDialog = new SetMultiCallIntervalDialog(getContext());
                setMultiCallIntervalDialog.show();
                setMultiCallIntervalDialog.setOnClickListener(new SetMultiCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(String time) {
                        SPUtils.put(SPConstant.SP_CALL_JGSZ, time);
                        mCallJgszTip.setText(Utils.getCallInterval());
                        ivMultiClearInterval.setVisibility(View.VISIBLE);
                        changePlCallUi();
                    }
                });
                break;
            case R.id.pl_switch_gd:
                mPlCallGd = !mPlCallGd;
                mPlGdSwitch.setImageResource(mPlCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                mPlGdSwitch2.setImageResource(mPlCallGd ? R.mipmap.switch_off : R.mipmap.switch_on);
                SPUtils.put(SPConstant.SP_CALL_PL_GD, mPlCallGd);
                break;
            case R.id.pl_switch_gd2:
                mPlCallGd = !mPlCallGd;
                mPlGdSwitch.setImageResource(mPlCallGd ? R.mipmap.switch_on : R.mipmap.switch_off);
                mPlGdSwitch2.setImageResource(mPlCallGd ? R.mipmap.switch_off : R.mipmap.switch_on);
                SPUtils.put(SPConstant.SP_CALL_PL_GD, mPlCallGd);
                break;
            case R.id.pl_call_tv:
                if (!TextUtils.isEmpty(mCallHmdrTip.getText().toString()) &&
                        !TextUtils.isEmpty(mCallJgszTip.getText().toString())) {
                    intent = new Intent(getContext(), AutoCallPhoneActivity.class);
                    intent.putExtra("type", "plbd");
                    startActivity(intent);
                }
                break;
            //短信定时
            case R.id.dhfs_switch:
                sms_dhfs_open = !sms_dhfs_open;
                dhfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_on : R.mipmap.switch_off);
                dhfs_ll.setVisibility(sms_dhfs_open ? View.VISIBLE : View.GONE);
                plfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_off : R.mipmap.switch_on);
                plfs_ll.setVisibility(sms_dhfs_open ? View.GONE : View.VISIBLE);
                changeSmsUi();
                break;
            case R.id.sms_txl_iv:
                intent = new Intent(getContext(), ContactsActivity.class);
                intent.putExtra("type", "sms");
                startActivity(intent);
                break;
            case R.id.sms_dsfs_rl:
                setCallTimingDialog = new SetCallTimingDialog(getContext(), "sms");
                setCallTimingDialog.show();
                setCallTimingDialog.setOnClickListener(new SetCallTimingDialog.OnClickListener() {
                    @Override
                    public void confirm(String value) {
                        SPUtils.put(SPConstant.SP_SMS_DSFS, value);
                        mSmsDsfsTip.setText(value + "后发送");
                    }
                });
                break;
            case R.id.sms_fscs_rl:
                setCallNumDialog = new SetCallNumDialog(getContext(), "sms");
                setCallNumDialog.show();
                setCallNumDialog.setOnClickListener(new SetCallNumDialog.OnClickListener() {
                    @Override
                    public void confirm(int num) {
                        SPUtils.put(SPConstant.SP_SMS_FSCS, num);
                        mSmsFscsTip.setText(num + "次");
                        changeSmsUi();
                    }
                });
                break;
            case R.id.plfs_switch:
                sms_dhfs_open = !sms_dhfs_open;
                dhfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_on : R.mipmap.switch_off);
                dhfs_ll.setVisibility(sms_dhfs_open ? View.VISIBLE : View.GONE);
                plfs_switch.setImageResource(sms_dhfs_open ? R.mipmap.switch_off : R.mipmap.switch_on);
                plfs_ll.setVisibility(sms_dhfs_open ? View.GONE : View.VISIBLE);
                changeSmsUi();
                break;
            case R.id.sms_hmdr_rl:
                importDialog = new ImportDialog(getContext());
                importDialog.show();
                importDialog.setOnClickListener(new ImportDialog.OnClickListener() {
                    @Override
                    public void wordImport() {
                        importDialog.dismiss();
                        DocImportDialog docImportDialog = new DocImportDialog(getContext());
                        docImportDialog.show();
                        docImportDialog.setOnClickListener(new DocImportDialog.OnClickListener() {
                            @Override
                            public void wordImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "word");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void excelImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "excel");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }

                            @Override
                            public void txtImport() {
                                Intent intent1 = new Intent(getContext(), DocImportActivity.class);
                                intent1.putExtra("type", "txt");
                                intent1.putExtra("type2", "sms");
                                startActivity(intent1);
                                docImportDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void txlImport() {
                        Intent intent1 = new Intent(getContext(), ImportContactsActivity.class);
                        intent1.putExtra("type", "sms");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }

                    @Override
                    public void copyImport() {
                        Intent intent1 = new Intent(getContext(), CopyImportActivity.class);
                        intent1.putExtra("type", "sms");
                        startActivity(intent1);
                        importDialog.dismiss();
                    }
                });
                break;
            case R.id.sms_sksz_rl:
                intent = new Intent(getContext(), SimSettingActivity.class);
                intent.putExtra("type", "sms");
                startActivity(intent);
                break;
            case R.id.sms_fsjg_rl:
                setCallIntervalDialog = new SetCallIntervalDialog(getContext(), "sms");
                setCallIntervalDialog.show();
                setCallIntervalDialog.setOnClickListener(new SetCallIntervalDialog.OnClickListener() {
                    @Override
                    public void confirm(int second) {
                        SPUtils.put(SPConstant.SP_SMS_FSJG, second);
                        mSmsFsjgTip.setText(second + "s");
                        changeSmsUi();
                    }
                });
                break;
            case R.id.bjdx_rl:
                startActivity(new Intent(getContext(), EditSmsActivity.class));
                break;
            case R.id.sms_ljfs:
                if (sms_dhfs_open) {
                    //单号发送
                    if (!TextUtils.isEmpty(mSmsMobileEt.getText().toString()) &&
                            !TextUtils.isEmpty(mSmsFscsTip.getText().toString()) &&
                            !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                        intent = new Intent(getContext(), AutoSendSmsActivity.class);
                        intent.putExtra("type", "dhfs");
                        startActivity(intent);
                    }
                } else {
                    //批量发送
                    if (!TextUtils.isEmpty(mSmsHmdrTip.getText().toString()) &&
                            !TextUtils.isEmpty(mSmsFsjgTip.getText().toString()) &&
                            !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                        intent = new Intent(getContext(), AutoSendSmsActivity.class);
                        intent.putExtra("type", "plfs");
                        startActivity(intent);
                    }
                }
                break;
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
                            Intent intent = new Intent(getContext(), SingleAutoTaskActivity.class);
                            intent.putExtra("type", "dhbd");
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(getContext(), VipActivity.class));
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        //拨打电话
        String singlePhoneNumber = SPUtils.getString(SPConstant.SP_CALL_SRHM, "");
        if (TextUtils.isEmpty(singlePhoneNumber)) {
            mCallMobileEt.setText("");
            mCallMobileEt.setHint("请输入手机号码");
            ivClearSinglePhoneNumber.setVisibility(View.GONE);
        } else {
            mCallMobileEt.setText(singlePhoneNumber);
            ivClearSinglePhoneNumber.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_CALL_TIMING, ""))) {
            mCallDsbhTv.setText("");
            ivClearTime.setVisibility(View.GONE);
        } else {
            mCallDsbhTv.setText(SPUtils.getString(SPConstant.SP_CALL_TIMING, "") + "后拨打");
            ivClearTime.setVisibility(View.VISIBLE);
        }
        setBdfs();
        // 拨打次数默认1
        if (SPUtils.getInt(SPConstant.SP_CALL_NUM, 1) != 0) {
            mCallBdcsTv.setText(SPUtils.getInt(SPConstant.SP_CALL_NUM, 1) + "次");
        }
        // 拨打间隔
        int callInterval = SPUtils.getInt(SPConstant.SP_CALL_INTERVAL, 20);
        if (callInterval != 0) {
            mCallBdjgTv.setText(callInterval + "s");
        }
        if (SPUtils.getBoolean(SPConstant.SP_CALL_GD, true)) {
            mGdSwitch.setImageResource(R.mipmap.switch_on);
            mGdSwitch2.setImageResource(R.mipmap.switch_off);
        } else {
            mGdSwitch.setImageResource(R.mipmap.switch_off);
            mGdSwitch2.setImageResource(R.mipmap.switch_on);
        }

        //批量拨打电话
        List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
        if (callContactTables.size() == 0) {
            mCallHmdrTip.setText("");
        } else {
            mCallHmdrTip.setText("已导入" + callContactTables.size() + "个联系人");
        }
        String doubleSimSetting = SPUtils.getString(SPConstant.SP_CALL_SKSZ, "sim1");
        if (TextUtils.isEmpty(doubleSimSetting)) {
            mCallSkszTip.setText("");
        } else if ("sim1".equals(doubleSimSetting)) {
            mCallSkszTip.setText("使用卡1拨打");
        } else if ("sim2".equals(doubleSimSetting)) {
            mCallSkszTip.setText("使用卡2拨打");
        } else if ("sim_double".equals(doubleSimSetting)) {
            mCallSkszTip.setText("双卡轮流拨打");
        }
        // 批量间隔
        mCallJgszTip.setText(Utils.getCallInterval());

        //短信
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""))) {
            mSmsMobileEt.setText("");
            mSmsMobileEt.setHint("请输入手机号码");
        } else {
            mSmsMobileEt.setText(SPUtils.getString(SPConstant.SP_SMS_SRHM, ""));
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_DSFS, ""))) {
            mSmsDsfsTip.setText("");
        } else {
            mSmsDsfsTip.setText(SPUtils.getString(SPConstant.SP_SMS_DSFS, "") + "后发送");
        }
        if (SPUtils.getInt(SPConstant.SP_SMS_FSCS, 0) == 0) {
            mSmsFscsTip.setText("");
        } else {
            mSmsFscsTip.setText(SPUtils.getInt(SPConstant.SP_SMS_FSCS, 0) + "");
        }

        List<SmsContactTable> smsContactTables = DatabaseBusiness.getSmsContacts();
        if (smsContactTables.size() == 0) {
            mSmsHmdrTip.setText("");
        } else {
            mSmsHmdrTip.setText("已导入" + smsContactTables.size() + "个联系人");
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("");
        } else if ("sim1".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("使用卡1发送");
        } else if ("sim2".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("使用卡2发送");
        } else if ("sim_double".equals(SPUtils.getString(SPConstant.SP_SMS_SKSZ, ""))) {
            mSmsSkszTip.setText("双卡轮流发送");
        }

        if (SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0) == 0) {
            mSmsFsjgTip.setText("");
        } else {
            mSmsFsjgTip.setText(SPUtils.getInt(SPConstant.SP_SMS_FSJG, 0) + "s");
        }
        if (TextUtils.isEmpty(SPUtils.getString(SPConstant.SP_SMS_CONTENT, ""))) {
            mBjdxTip.setText("");
        } else {
            mBjdxTip.setText("已编辑");
        }

        mCallMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = mCallMobileEt.getText().toString();
                SPUtils.put(SPConstant.SP_CALL_SRHM, phone);
                changeCallUi();
                if (TextUtils.isEmpty(phone)) {
                    ivClearSinglePhoneNumber.setVisibility(View.GONE);
                } else {
                    ivClearSinglePhoneNumber.setVisibility(View.VISIBLE);
                }
            }
        });

        mSmsMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = mSmsMobileEt.getText().toString();
                SPUtils.put(SPConstant.SP_SMS_SRHM, phone);
                if (TextUtils.isEmpty(phone)) {
                    ivClearSmsPhone.setVisibility(View.GONE);
                } else {
                    ivClearSmsPhone.setVisibility(View.VISIBLE);
                }
                changeSmsUi();
            }
        });


        changeCallUi();
        changePlCallUi();
        //短信定时
        changeSmsUi();
    }

    private void changeCallUi() {
        //拨打电话
        if (!TextUtils.isEmpty(mCallMobileEt.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdcsTv.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdjgTv.getText().toString()) &&
                !TextUtils.isEmpty(mCallBdfsTv.getText().toString())) {
            mCallTv.setBackgroundResource(R.drawable.round_36_green);
        } else {
            mCallTv.setBackgroundResource(R.drawable.round_36_grey);
        }
    }

    private void changePlCallUi() {
        //批量拨打电话
        if (!TextUtils.isEmpty(mCallHmdrTip.getText().toString()) &&
                !TextUtils.isEmpty(mCallJgszTip.getText().toString())) {
            mPlCallTv.setBackgroundResource(R.drawable.round_36_green);
        } else {
            mPlCallTv.setBackgroundResource(R.drawable.round_36_grey);
        }
    }

    private void changeSmsUi() {
        //短信定时
        if (sms_dhfs_open) {
            //单号发送
            if (!TextUtils.isEmpty(mSmsMobileEt.getText().toString()) &&
                    !TextUtils.isEmpty(mSmsFscsTip.getText().toString()) &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                mSmsLjfs.setBackgroundResource(R.drawable.round_36_green);
            } else {
                mSmsLjfs.setBackgroundResource(R.drawable.round_36_grey);
            }
        } else {
            //批量发送
            if (!TextUtils.isEmpty(mSmsHmdrTip.getText().toString()) &&
                    !TextUtils.isEmpty(mSmsFsjgTip.getText().toString()) &&
                    !TextUtils.isEmpty(mBjdxTip.getText().toString())) {
                mSmsLjfs.setBackgroundResource(R.drawable.round_36_green);
            } else {
                mSmsLjfs.setBackgroundResource(R.drawable.round_36_grey);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TAB1_CALL_TYPE:
                setBdfs();
                break;
        }
    }
}
