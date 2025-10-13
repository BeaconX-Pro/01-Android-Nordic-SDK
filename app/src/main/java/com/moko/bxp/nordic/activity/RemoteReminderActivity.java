package com.moko.bxp.nordic.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.databinding.ActivityRemoteReminderBinding;
import com.moko.lib.bxpui.dialog.BottomDialog;
import com.moko.lib.bxpui.dialog.LoadingMessageDialog;
import com.moko.lib.bxpui.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

public class RemoteReminderActivity extends BaseActivity {

    private ActivityRemoteReminderBinding mBind;
    public boolean isConfigError;

    private String[] mColors = new String[]{"Red", "Green", "Blue"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityRemoteReminderBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getRemoteLEDAlarmParams());
            orderTasks.add(OrderTaskAssembler.getRemoteBuzzerAlarmParams());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                RemoteReminderActivity.this.finish();
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
                            if (configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            switch (configKeyEnum) {
                                case SET_ERROR:
                                    ToastUtils.showToast(RemoteReminderActivity.this, "Failed");
                                    break;
                                case SET_REMOTE_LED_ALARM_PARAMS:
                                case SET_REMOTE_BUZZER_ALARM_PARAMS:
                                    ToastUtils.showToast(RemoteReminderActivity.this, "Success");
                                    break;
                                case GET_REMOTE_LED_ALARM_PARAMS:
                                    if (length == 5) {
                                        int color = value[4];
                                        mBind.tvLedNotifyColor.setTag(color - 3);
                                        mBind.tvLedNotifyColor.setText(mColors[color - 3]);
                                        int time = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                        mBind.etBlinkingTime.setText(String.valueOf(time));
                                        mBind.etBlinkingInterval.setText(String.valueOf(interval));
                                    }
                                    break;
                                case GET_REMOTE_BUZZER_ALARM_PARAMS:
                                    if (length == 6) {
                                        int time = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 10));
                                        mBind.etRingingTime.setText(String.valueOf(time));
                                        mBind.etRingingInterval.setText(String.valueOf(interval));
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null) mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public void selectLedColor(View view) {
        if (isWindowLocked()) return;
        int selected = (int) view.getTag();
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mColors)), selected);
        dialog.setListener(value -> {
            view.setTag(value);
            mBind.tvLedNotifyColor.setText(mColors[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onLedNotifyRemind(View view) {
        if (isWindowLocked()) return;
        if (isLEDValid()) {
            int color = (int) mBind.tvLedNotifyColor.getTag();
            showSyncingProgressDialog();
            String ledTimeStr = mBind.etBlinkingTime.getText().toString();
            String ledIntervalStr = mBind.etBlinkingInterval.getText().toString();
            int ledTime = Integer.parseInt(ledTimeStr);
            int ledInterval = Integer.parseInt(ledIntervalStr);
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setRemoteLEDAlarmParams(color + 3, ledTime, ledInterval));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } else {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
        }
    }

    public void onBuzzerNotifyRemind(View view) {
        if (isWindowLocked()) return;
        if (isBuzzerValid()) {
            showSyncingProgressDialog();
            String buzzerTimeStr = mBind.etRingingTime.getText().toString();
            String buzzerIntervalStr = mBind.etRingingInterval.getText().toString();
            int buzzerTime = Integer.parseInt(buzzerTimeStr);
            int buzzerInterval = Integer.parseInt(buzzerIntervalStr);
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setRemoteBuzzerAlarmParams(buzzerTime, buzzerInterval));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } else {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
        }
    }

    public void onBack(View view) {
        finish();
    }

    private boolean isBuzzerValid() {
        String buzzerTimeStr = mBind.etRingingTime.getText().toString();
        String buzzerIntervalStr = mBind.etRingingInterval.getText().toString();
        if (TextUtils.isEmpty(buzzerTimeStr) || TextUtils.isEmpty(buzzerIntervalStr)) {
            return false;
        }
        int buzzerTime = Integer.parseInt(buzzerTimeStr);
        if (buzzerTime < 1 || buzzerTime > 600) return false;
        int buzzerInterval = Integer.parseInt(buzzerIntervalStr);
        if (buzzerInterval < 1 || buzzerInterval > 100) return false;
        return true;
    }

    private boolean isLEDValid() {
        String ledTimeStr = mBind.etBlinkingTime.getText().toString();
        String ledIntervalStr = mBind.etBlinkingInterval.getText().toString();
        if (TextUtils.isEmpty(ledTimeStr) || TextUtils.isEmpty(ledIntervalStr)) {
            return false;
        }
        int ledTime = Integer.parseInt(ledTimeStr);
        if (ledTime < 1 || ledTime > 600) return false;
        int ledInterval = Integer.parseInt(ledIntervalStr);
        if (ledInterval < 1 || ledInterval > 100) return false;
        return true;
    }
}
