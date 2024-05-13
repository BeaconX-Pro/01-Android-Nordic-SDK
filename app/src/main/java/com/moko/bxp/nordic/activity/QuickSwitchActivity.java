package com.moko.bxp.nordic.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.dialog.AlertMessageDialog;
import com.moko.bxp.nordic.dialog.LoadingMessageDialog;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickSwitchActivity extends BaseActivity {

    @BindView(R2.id.iv_connectable)
    ImageView ivConnectable;
    @BindView(R2.id.tv_connectable_status)
    TextView tvConnectableStatus;
    @BindView(R2.id.iv_trigger_led_notify)
    ImageView ivTriggerLedNotify;
    @BindView(R2.id.tv_trigger_led_notify)
    TextView tvTriggerLedNotify;
    @BindView(R2.id.iv_button_power)
    ImageView ivButtonPower;
    @BindView(R2.id.tv_button_power)
    TextView tvButtonPower;
    @BindView(R2.id.iv_hw_reset)
    ImageView ivHwReset;
    @BindView(R2.id.tv_hw_reset)
    TextView tvHwReset;
    @BindView(R2.id.iv_password_verify)
    ImageView ivPasswordVerify;
    @BindView(R2.id.tv_password_verify)
    TextView tvPasswordVerify;
    @BindView(R2.id.cv_hw_reset)
    CardView cvHwReset;
    @BindView(R2.id.cv_trigger_led_notify)
    CardView cvTriggerLedNotify;
    @BindView(R2.id.cv_scan_response_indicator)
    CardView cvResponseSwitch;
    @BindView(R2.id.iv_scan_response_indicator)
    ImageView ivScanResponseIndicator;
    @BindView(R2.id.tv_scan_response_indicator)
    TextView tvScanResponseIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_switch);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        boolean isNewVersion = getIntent().getBooleanExtra(AppConstants.IS_NEW_VERSION,true);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        if (isNewVersion) cvResponseSwitch.setVisibility(View.VISIBLE);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getConnectable());
            orderTasks.add(OrderTaskAssembler.getTriggerLEDNotifyEnable());
            orderTasks.add(OrderTaskAssembler.getButtonPower());
            orderTasks.add(OrderTaskAssembler.getHWResetEnable());
            if (isNewVersion) orderTasks.add(OrderTaskAssembler.getResponsePackageSwitch());
            orderTasks.add(OrderTaskAssembler.getLockState());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                                case GET_BUTTON_POWER:
                                    if (value.length >= 5) {
                                        int enable = value[4] & 0xFF;
                                        setButtonPower(enable);
                                    }
                                    break;
                                case GET_HW_RESET_ENABLE:
                                    if (value.length >= 4) {
                                        cvHwReset.setVisibility(View.VISIBLE);
                                        int enable = value[4] & 0xFF;
                                        setHWResetEnable(enable);
                                    }
                                    break;
                                case GET_TRIGGER_LED_NOTIFICATION:
                                    if (value.length >= 4) {
                                        cvTriggerLedNotify.setVisibility(View.VISIBLE);
                                        int enable = value[4] & 0xFF;
                                        setTriggerLEDNotifyEnable(enable);
                                    }
                                    break;
                                case GET_RESPONSE_PACKAGE_SWITCH:
                                    if (value.length >= 4) {
                                        setScanResponseIndicator(value[4] & 0xff);
                                    }
                                    break;
                                case SET_BUTTON_POWER:
                                case SET_HW_RESET_ENABLE:
                                case SET_TRIGGER_LED_NOTIFICATION:
                                case SET_RESPONSE_PACKAGE_SWITCH:
                                    ToastUtils.showToast(this, "Success!");
                                    break;
                                case SET_ERROR:
                                    if (isWindowLocked()) return;
                                    ToastUtils.showToast(this, "Failed");
                                    break;
                            }
                        }
                        break;
                    case CHAR_LOCK_STATE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            int enable = MokoUtils.toInt(value);
                            setPasswordVerify(enable);
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(this, "Success!");
                        }
                        break;
                    case CHAR_CONNECTABLE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            int enable = MokoUtils.toInt(value);
                            setConnectable(enable);
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(this, "Success!");
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
                    case CHAR_LOCK_STATE:
                        String valueHexStr = MokoUtils.bytesToHexString(value);
                        if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                            ToastUtils.showToast(QuickSwitchActivity.this, "Device Locked!");
                            finish();
                        }
                        break;
                }
            }
        });
    }

    private boolean enablePasswordVerify;

    public void setPasswordVerify(int enable) {
        this.enablePasswordVerify = enable == 1;
        ivPasswordVerify.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvPasswordVerify.setText(enablePasswordVerify ? "Enable" : "Disable");
        tvPasswordVerify.setEnabled(enablePasswordVerify);
    }

    boolean enableConnected;

    public void setConnectable(int enable) {
        enableConnected = enable == 1;
        ivConnectable.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvConnectableStatus.setText(enableConnected ? "Enable" : "Disable");
        tvConnectableStatus.setEnabled(enableConnected);
    }

    private boolean enableButtonPower;

    public void setButtonPower(int enable) {
        this.enableButtonPower = enable == 1;
        ivButtonPower.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvButtonPower.setText(enableButtonPower ? "Enable" : "Disable");
        tvButtonPower.setEnabled(enableButtonPower);
    }

    private boolean enableHWReset;

    public void setHWResetEnable(int enable) {
        this.enableHWReset = enable == 1;
        ivHwReset.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvHwReset.setText(enableHWReset ? "Enable" : "Disable");
        tvHwReset.setEnabled(enableHWReset);
    }

    private boolean enableTriggerLEDNotify;

    public void setTriggerLEDNotifyEnable(int enable) {
        this.enableTriggerLEDNotify = enable == 1;
        ivTriggerLedNotify.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvTriggerLedNotify.setText(enableTriggerLEDNotify ? "Enable" : "Disable");
        tvTriggerLedNotify.setEnabled(enableTriggerLEDNotify);
    }

    public void onChangeConnectable(View view) {
        if (isWindowLocked())
            return;
        if (enableConnected) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("Are you sure to set the Beacon non-connectable？");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setConnectable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setConnectable(true);
        }
    }

    public void onChangeTriggerLEDNotify(View view) {
        if (isWindowLocked())
            return;
        setTriggerLEDNotify(!enableTriggerLEDNotify);
    }

    public void onChangeButtonPower(View view) {
        if (isWindowLocked())
            return;
        if (enableButtonPower) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If this function is disabled, you cannot power off the Beacon by button.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setButtonPower(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setButtonPower(true);
        }
    }

    public void onChangeHWReset(View view) {
        if (isWindowLocked())
            return;
        if (enableHWReset) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Button reset is disabled, you cannot reset the Beacon by button operation.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setHWResetEnable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setHWResetEnable(true);
        }
    }

    public void onChangePasswordVerify(View view) {
        if (isWindowLocked())
            return;
        if (enablePasswordVerify) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Password verification is disabled, it will not need password to connect the Beacon.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setDirectedConnectable(true);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setDirectedConnectable(false);
        }
    }

    private boolean enableScanResponse;

    public void setScanResponseIndicator(int enable) {
        enableScanResponse = enable == 1;
        ivScanResponseIndicator.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvScanResponseIndicator.setText(enable == 1 ? "Enable" : "Disable");
        tvScanResponseIndicator.setEnabled(enable == 1);
    }

    public void onChangeScanResponseIndicator(View view) {
        if (isWindowLocked()) return;
        setChangeScanResponseIndicator(!enableScanResponse);
    }

    private void setChangeScanResponseIndicator(boolean enable) {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setResponsePackageSwitch(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getResponsePackageSwitch());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    public void setConnectable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setConnectable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setTriggerLEDNotify(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setTriggerLEDNotifyEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getTriggerLEDNotifyEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setButtonPower(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonPower(enable));
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setHWResetEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setHWResetEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getHWResetEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setDirectedConnectable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLockStateDirected(enable ? 2 : 1));
        orderTasks.add(OrderTaskAssembler.getLockState());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
        // 注销广播
        unregisterReceiver(mReceiver);
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

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, enablePasswordVerify);
        setResult(RESULT_OK, intent);
        finish();
    }
}
