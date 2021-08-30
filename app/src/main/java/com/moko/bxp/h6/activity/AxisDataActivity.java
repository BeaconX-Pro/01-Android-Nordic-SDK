package com.moko.bxp.h6.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.bxp.h6.R;
import com.moko.bxp.h6.dialog.BottomDialog;
import com.moko.bxp.h6.dialog.LoadingMessageDialog;
import com.moko.bxp.h6.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.h6.MokoSupport;
import com.moko.support.h6.OrderTaskAssembler;
import com.moko.support.h6.entity.OrderCHAR;
import com.moko.support.h6.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AxisDataActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.iv_sync)
    ImageView ivSync;
    @BindView(R.id.tv_sync)
    TextView tvSync;
    @BindView(R.id.tv_x_data)
    TextView tvXData;
    @BindView(R.id.tv_y_data)
    TextView tvYData;
    @BindView(R.id.tv_z_data)
    TextView tvZData;
    @BindView(R.id.tv_axis_scale)
    TextView tvAxisScale;
    @BindView(R.id.tv_axis_data_rate)
    TextView tvAxisDataRate;
    @BindView(R.id.sb_trigger_sensitivity)
    SeekBar sbTriggerSensitivity;
    @BindView(R.id.tv_trigger_sensitivity)
    TextView tvTriggerSensitivity;
    private boolean mReceiverTag = false;
    private ArrayList<String> axisDataRates;
    private ArrayList<String> axisScales;
    private boolean isSync;
    private int mSelectedRate;
    private int mSelectedScale;
    private int mSelectedSensivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_axis);
        ButterKnife.bind(this);
        axisDataRates = new ArrayList<>();
        axisDataRates.add("1Hz");
        axisDataRates.add("10Hz");
        axisDataRates.add("25Hz");
        axisDataRates.add("50Hz");
        axisDataRates.add("100Hz");
        axisScales = new ArrayList<>();
        axisScales.add("±2g");
        axisScales.add("±4g");
        axisScales.add("±8g");
        axisScales.add("±16g");
        sbTriggerSensitivity.setOnSeekBarChangeListener(this);

        EventBus.getDefault().register(this);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getAxisParams());
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAxisParams());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    finish();
                }
                if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                    // 设备连接成功，通知页面更新
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case GET_AXIS_PARAMS:
                                    if (value.length > 6) {
                                        mSelectedRate = value[4] & 0xff;
                                        tvAxisDataRate.setText(axisDataRates.get(mSelectedRate));
                                        mSelectedScale = value[5] & 0xff;
                                        tvAxisScale.setText(axisScales.get(mSelectedScale));
                                        mSelectedSensivity = value[6] & 0xff;
                                        if (MokoSupport.isNewVersion) {
                                            sbTriggerSensitivity.setProgress(mSelectedSensivity - 1);
                                            if (mSelectedScale == 0) {
                                                sbTriggerSensitivity.setMax(18);
                                            }
                                            if (mSelectedScale == 1) {
                                                sbTriggerSensitivity.setMax(38);
                                            }
                                            if (mSelectedScale == 2) {
                                                sbTriggerSensitivity.setMax(78);
                                            }
                                            if (mSelectedScale == 3) {
                                                sbTriggerSensitivity.setMax(158);
                                            }
                                            tvTriggerSensitivity.setText(MokoUtils.getDecimalFormat("#.#g").format(mSelectedSensivity * 0.1));
                                        } else {
                                            sbTriggerSensitivity.setProgress(mSelectedSensivity - 7);
                                            tvTriggerSensitivity.setText(String.valueOf(mSelectedSensivity));
                                        }
                                    }
                                    break;
                                case SET_AXIS_PARAMS:
                                    if (value.length > 3 && value[3] == 0) {
                                        ToastUtils.showToast(AxisDataActivity.this, "Success");
                                    } else {
                                        ToastUtils.showToast(AxisDataActivity.this, "Failed");
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {

                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_LOCKED_NOTIFY:
                        String valueHexStr = MokoUtils.bytesToHexString(value);
                        if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                            ToastUtils.showToast(AxisDataActivity.this, "Device Locked!");
                            back();
                        }
                        break;
                    case CHAR_THREE_AXIS_NOTIFY:
                        if (value.length > 5) {
                            String axisHexStr = MokoUtils.bytesToHexString(value);
                            int length = axisHexStr.length();
                            tvZData.setText(String.format("Z-axis:0x%s", axisHexStr.substring(length - 4).toUpperCase()));
                            tvYData.setText(String.format("Y-axis:0x%s", axisHexStr.substring(length - 8, length - 4).toUpperCase()));
                            tvXData.setText(String.format("X-axis:0x%s", axisHexStr.substring(length - 12, length - 8).toUpperCase()));
                        }
                        break;
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @OnClick({R.id.tv_back, R.id.ll_sync, R.id.iv_save, R.id.tv_axis_scale, R.id.tv_axis_data_rate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.ll_sync:
                if (!isSync) {
                    isSync = true;
                    MokoSupport.getInstance().enableThreeAxisNotify();
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    ivSync.startAnimation(animation);
                    tvSync.setText("Stop");
                } else {
                    MokoSupport.getInstance().disableThreeAxisNotify();
                    isSync = false;
                    ivSync.clearAnimation();
                    tvSync.setText("Sync");
                }
                break;
            case R.id.tv_axis_data_rate:
                BottomDialog dataRateDialog = new BottomDialog();
                dataRateDialog.setDatas(axisDataRates, mSelectedRate);
                dataRateDialog.setListener(value -> {
                    mSelectedRate = value;
                    tvAxisDataRate.setText(axisDataRates.get(value));
                });
                dataRateDialog.show(getSupportFragmentManager());
                break;
            case R.id.tv_axis_scale:
                BottomDialog scaleDialog = new BottomDialog();
                scaleDialog.setDatas(axisScales, mSelectedScale);
                scaleDialog.setListener(value -> {
                    mSelectedScale = value;
                    tvAxisScale.setText(axisScales.get(value));
                    if (MokoSupport.isNewVersion) {
                        sbTriggerSensitivity.setProgress(0);
                        if (mSelectedScale == 0) {
                            sbTriggerSensitivity.setMax(18);
                        }
                        if (mSelectedScale == 1) {
                            sbTriggerSensitivity.setMax(38);
                        }
                        if (mSelectedScale == 2) {
                            sbTriggerSensitivity.setMax(78);
                        }
                        if (mSelectedScale == 3) {
                            sbTriggerSensitivity.setMax(158);
                        }
                    }
                });
                scaleDialog.show(getSupportFragmentManager());
                break;
            case R.id.iv_save:
                // 保存
                showSyncingProgressDialog();
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.setAxisParams(mSelectedRate, mSelectedScale, mSelectedSensivity));
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                break;
        }
    }

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().disableThreeAxisNotify();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (MokoSupport.isNewVersion) {
            mSelectedSensivity = progress + 1;
            tvTriggerSensitivity.setText(MokoUtils.getDecimalFormat("#.#g").format(mSelectedSensivity * 0.1));
        } else {
            mSelectedSensivity = progress + 7;
            tvTriggerSensitivity.setText(mSelectedSensivity + "");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
