package com.moko.bxp.nordic.activity;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.dialog.LoadingMessageDialog;
import com.moko.bxp.nordic.fragment.StorageHumidityFragment;
import com.moko.bxp.nordic.fragment.StorageTHFragment;
import com.moko.bxp.nordic.fragment.StorageTempFragment;
import com.moko.bxp.nordic.fragment.StorageTimeFragment;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.bxp.nordic.utils.Utils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class THDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {

    @BindView(R2.id.tv_temp)
    TextView tvTemp;
    @BindView(R2.id.tv_humidity)
    TextView tvHumidity;
    @BindView(R2.id.npv_storage_condition)
    NumberPickerView npvStorageCondition;
    //    @BindView(R2.id.frame_storage_condition)
//    FrameLayout frameStorageCondition;
    @BindView(R2.id.tv_update_date)
    TextView tvUpdateDate;
    @BindView(R2.id.et_period)
    EditText etPeriod;

    private FragmentManager fragmentManager;
    private StorageTempFragment tempFragment;
    private StorageHumidityFragment humidityFragment;
    private StorageTHFragment thFragment;
    private StorageTimeFragment timeFragment;

    private boolean mReceiverTag = false;

    private boolean mIsPeriodSuccess;
    private boolean mIsStorageSuccess;

    private int mStorageType;
    private int mSelectedTemp;
    private int mSelectedHumidity;
    private int mSelectedTime = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_th);
        ButterKnife.bind(this);

        fragmentManager = getFragmentManager();
        createFragments();
        npvStorageCondition.setOnValueChangedListener(this);
        npvStorageCondition.setValue(0);
        npvStorageCondition.setMinValue(0);
        npvStorageCondition.setMaxValue(3);
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
            MokoSupport.getInstance().enableTHNotify();
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getTHPeriod());
            orderTasks.add(OrderTaskAssembler.getStorageCondition());
            orderTasks.add(OrderTaskAssembler.getDeviceTime());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        tempFragment = StorageTempFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, tempFragment);
        humidityFragment = StorageHumidityFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, humidityFragment);
        thFragment = StorageTHFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, thFragment);
        timeFragment = StorageTimeFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, timeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        XLog.i(newVal + "");
        mStorageType = newVal;
        XLog.i(picker.getContentByCurrValue());
        switch (newVal) {
            case 0:
                mSelectedTemp = 0;
                tempFragment.setTempData(mSelectedTemp);
                break;
            case 1:
                mSelectedHumidity = 0;
                humidityFragment.setHumidityData(mSelectedHumidity);
                break;
            case 2:
                mSelectedTemp = 1;
                mSelectedHumidity = 1;
                thFragment.setTempData(mSelectedTemp);
                thFragment.setHumidityData(mSelectedHumidity);
                break;
            case 3:
                mSelectedTime = 1;
                timeFragment.setTimeData(mSelectedTime);
                break;
        }
        showFragment(newVal);
    }

    private void showFragment(int newVal) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (newVal) {
            case 0:
                fragmentTransaction.show(tempFragment).hide(humidityFragment).hide(thFragment).hide(timeFragment).commit();
                break;
            case 1:
                fragmentTransaction.hide(tempFragment).show(humidityFragment).hide(thFragment).hide(timeFragment).commit();
                break;
            case 2:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(thFragment).hide(timeFragment).commit();
                break;
            case 3:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(thFragment).show(timeFragment).commit();
                break;
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
                                case GET_TH_PERIOD:
                                    if (value.length > 4) {
                                        byte[] period = Arrays.copyOfRange(value, 4, 6);
                                        String periodStr = MokoUtils.toInt(period) + "";
                                        etPeriod.setText(periodStr);
                                        etPeriod.setSelection(periodStr.length());
                                    }
                                    break;
                                case GET_STORAGE_CONDITION:
                                    if (value.length > 6 && (value[4] & 0xff) == 0) {
                                        mStorageType = 0;
                                        npvStorageCondition.setValue(0);
                                        byte[] temp = Arrays.copyOfRange(value, 5, 7);
                                        mSelectedTemp = MokoUtils.toInt(temp) / 5;
                                        tempFragment.setTempData(mSelectedTemp);
                                    } else if (value.length > 6 && (value[4] & 0xff) == 1) {
                                        mStorageType = 1;
                                        npvStorageCondition.setValue(1);
                                        byte[] humidity = Arrays.copyOfRange(value, 5, 7);
                                        mSelectedHumidity = MokoUtils.toInt(humidity) / 5;
                                        humidityFragment.setHumidityData(mSelectedHumidity);
                                    } else if (value.length > 8 && (value[4] & 0xff) == 2) {
                                        mStorageType = 2;
                                        npvStorageCondition.setValue(2);
                                        byte[] temp = Arrays.copyOfRange(value, 5, 7);
                                        byte[] humidity = Arrays.copyOfRange(value, 7, 9);
                                        mSelectedTemp = MokoUtils.toInt(temp) / 5;
                                        thFragment.setTempData(mSelectedTemp);
                                        mSelectedHumidity = MokoUtils.toInt(humidity) / 5;
                                        thFragment.setHumidityData(mSelectedHumidity);
                                    } else if (value.length > 5 && (value[4] & 0xff) == 3) {
                                        mStorageType = 3;
                                        npvStorageCondition.setValue(3);
                                        mSelectedTime = value[5] & 0xff;
                                        timeFragment.setTimeData(mSelectedTime);
                                    }
                                    showFragment(mStorageType);
                                    break;
                                case GET_DEVICE_TIME:
                                    if (value.length > 9) {
                                        int year = value[4] & 0xff;
                                        int month = value[5] & 0xff;
                                        int day = value[6] & 0xff;
                                        int hour = value[7] & 0xff;
                                        int minute = value[8] & 0xff;
                                        int second = value[9] & 0xff;
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(Calendar.YEAR, 2000 + year);
                                        calendar.set(Calendar.MONTH, month - 1);
                                        calendar.set(Calendar.DAY_OF_MONTH, day);
                                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                                        calendar.set(Calendar.MINUTE, minute);
                                        calendar.set(Calendar.SECOND, second);
                                        tvUpdateDate.setText(Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS));
                                    }
                                    break;
                                case SET_TH_PERIOD:
                                    if (value.length > 3 && value[3] == 0) {
                                        mIsPeriodSuccess = true;
                                    }
                                    break;
                                case SET_DEVICE_TIME:
                                    if (value.length > 3 && value[3] == 0) {
                                        ToastUtils.showToast(THDataActivity.this, "Success");
                                    } else {
                                        ToastUtils.showToast(THDataActivity.this, "Failed");
                                    }
                                    break;
                                case SET_STORAGE_CONDITION:
                                    if (value.length > 3 && value[3] == 0) {
                                        mIsStorageSuccess = true;
                                    }
                                    if (mIsPeriodSuccess && mIsStorageSuccess) {
                                        ToastUtils.showToast(THDataActivity.this, "Success");
                                    } else {
                                        ToastUtils.showToast(THDataActivity.this, "Failed");
                                    }
                                    break;
                                case SET_ERROR:
                                    if (isWindowLocked()) return;
                                    ToastUtils.showToast(this, "Failed");
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
                            ToastUtils.showToast(THDataActivity.this, "Device Locked!");
                            back();
                        }
                        break;
                    case CHAR_TH_NOTIFY:
                        if (value.length > 3) {
                            byte[] tempBytes = Arrays.copyOfRange(value, 0, 2);
                            float temp = MokoUtils.byte2short(tempBytes) * 0.1f;
                            tvTemp.setText(MokoUtils.getDecimalFormat("0.0").format(temp));
                            byte[] humidityBytes = Arrays.copyOfRange(value, 2, 4);
                            float humidity = MokoUtils.toInt(humidityBytes) * 0.1f;
                            tvHumidity.setText(MokoUtils.getDecimalFormat("0.0").format(humidity));
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

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().disableTHNotify();
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

    public void setSelectedTemp(int selectedTemp) {
        this.mSelectedTemp = selectedTemp;
    }

    public void setSelectedHumidity(int selectedHumidity) {
        this.mSelectedHumidity = selectedHumidity;
    }

    public void setSelectedTime(int selectedTime) {
        this.mSelectedTime = selectedTime;
    }

    public void onExportData(View view) {
        if (isWindowLocked())
            return;
        // 跳转导出数据页面
        startActivity(new Intent(this, ExportDataActivity.class));
    }

    public void onUpdate(View view) {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        Calendar calendar = Calendar.getInstance();
        ArrayList<OrderTask> tasks = new ArrayList<>();
        tasks.add(OrderTaskAssembler.setDeviceTime(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)));
        tasks.add(OrderTaskAssembler.getDeviceTime());
        MokoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        // 保存
        String periodStr = etPeriod.getText().toString();
        if (TextUtils.isEmpty(periodStr)) {
            ToastUtils.showToast(this, "The Sampling Period can not be empty");
            return;
        }
        int period = Integer.parseInt(periodStr);
        if (period < 1 || period > 65535) {
            ToastUtils.showToast(this, "The Sampling Period range is 1~65535");
            return;
        }
        showSyncingProgressDialog();
        String storageData = "";
        switch (mStorageType) {
            case 0:
                storageData = String.format("%04X", mSelectedTemp * 5);
                break;
            case 1:
                storageData = String.format("%04X", mSelectedHumidity * 5);
                break;
            case 2:
                storageData = String.format("%04X", mSelectedTemp * 5) + String.format("%04X", mSelectedHumidity * 5);
                break;
            case 3:
                storageData = String.format("%02X", mSelectedTime);
                break;
        }
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setTHPeriod(period));
        orderTasks.add(OrderTaskAssembler.setStorageCondition(mStorageType, storageData));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onSelectStorageTemp(View view) {
        if (isWindowLocked())
            return;
        if (tempFragment != null) {
            tempFragment.selectStorageTemp();
        }
    }

    public void onSelectStorageHumi(View view) {
        if (isWindowLocked())
            return;
        if (humidityFragment != null) {
            humidityFragment.selectStorageHumi();
        }
    }

    public void onSelectTHStorageTemp(View view) {
        if (isWindowLocked())
            return;
        if (thFragment != null) {
            thFragment.selectStorageTemp();
        }
    }

    public void onSelectTHStorageHumi(View view) {
        if (isWindowLocked())
            return;
        if (thFragment != null) {
            thFragment.selectStorageHumi();
        }
    }

    public void onSelectStorageTime(View view) {
        if (isWindowLocked())
            return;
        if (timeFragment != null) {
            timeFragment.selectStorageTime();
        }
    }
}
