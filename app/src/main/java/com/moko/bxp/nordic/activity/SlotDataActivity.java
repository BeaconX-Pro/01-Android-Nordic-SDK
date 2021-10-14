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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.able.ISlotDataAction;
import com.moko.bxp.nordic.dialog.BottomDialog;
import com.moko.bxp.nordic.dialog.LoadingMessageDialog;
import com.moko.bxp.nordic.fragment.AxisFragment;
import com.moko.bxp.nordic.fragment.DeviceInfoFragment;
import com.moko.bxp.nordic.fragment.IBeaconFragment;
import com.moko.bxp.nordic.fragment.THFragment;
import com.moko.bxp.nordic.fragment.TlmFragment;
import com.moko.bxp.nordic.fragment.TriggerHumidityFragment;
import com.moko.bxp.nordic.fragment.TriggerLightDetectedFragment;
import com.moko.bxp.nordic.fragment.TriggerMovesFragment;
import com.moko.bxp.nordic.fragment.TriggerTappedFragment;
import com.moko.bxp.nordic.fragment.TriggerTempFragment;
import com.moko.bxp.nordic.fragment.UidFragment;
import com.moko.bxp.nordic.fragment.UrlFragment;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.SlotData;
import com.moko.support.nordic.entity.SlotFrameTypeEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {
    private static final int TRIGGER_TYPE_NULL = 0;
    private static final int TRIGGER_TYPE_TEMPERATURE = 1;
    private static final int TRIGGER_TYPE_HUMIDITY = 2;
    private static final int TRIGGER_TYPE_TRAP_DOUBLE = 3;
    private static final int TRIGGER_TYPE_TRAP_TRIPLE = 4;
    private static final int TRIGGER_TYPE_MOVE = 5;
    private static final int TRIGGER_TYPE_LIGHT = 6;

    private static final int DEVICE_TYPE_SENSOR_NULL = 0;
    private static final int DEVICE_TYPE_SENSOR_AXIS = 1;
    private static final int DEVICE_TYPE_SENSOR_TH = 2;
    private static final int DEVICE_TYPE_SENSOR_AXIS_TH = 3;
    private static final int DEVICE_TYPE_SENSOR_LIGHT = 4;
    private static final int DEVICE_TYPE_SENSOR_AXIS_LIGHT = 5;
    private static final int DEVICE_TYPE_SENSOR_TH_LIGHT = 6;
    private static final int DEVICE_TYPE_SENSOR_AXIS_TH_LIGHT = 7;

    @BindView(R2.id.tv_slot_title)
    TextView tvSlotTitle;
    @BindView(R2.id.iv_save)
    ImageView ivSave;
    @BindView(R2.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @BindView(R2.id.npv_slot_type)
    NumberPickerView npvSlotType;
    @BindView(R2.id.iv_trigger)
    ImageView ivTrigger;
    @BindView(R2.id.tv_trigger_type)
    TextView tvTriggerType;
    @BindView(R2.id.frame_trigger_container)
    FrameLayout frameTriggerContainer;
    @BindView(R2.id.rl_trigger)
    RelativeLayout rlTrigger;
    @BindView(R2.id.rl_trigger_switch)
    RelativeLayout rlTriggerSwitch;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private DeviceInfoFragment deviceInfoFragment;
    private AxisFragment axisFragment;
    private THFragment thFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;
    public int deviceType;
    private TriggerTempFragment tempFragment;
    private TriggerHumidityFragment humidityFragment;
    private TriggerTappedFragment tappedFragment;
    private TriggerMovesFragment movesFragment;
    private TriggerLightDetectedFragment lightDetectedFragment;
    private boolean mReceiverTag = false;
    private int triggerType;
    private byte[] triggerData;
    private String[] slotTypeArray;
    private ArrayList<String> triggerTypes;
    private int triggerTypeSelected;
    public SlotFrameTypeEnum currentFrameTypeEnum;
    public boolean isConfigError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_data);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            currentFrameTypeEnum = slotData.frameTypeEnum;
            deviceType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0);
            triggerType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_TRIGGER_TYPE, 0);
            String triggerDataStr = getIntent().getStringExtra(AppConstants.EXTRA_KEY_TRIGGER_DATA);
            if (!TextUtils.isEmpty(triggerDataStr)) {
                triggerData = MokoUtils.hex2bytes(triggerDataStr);
            }
            XLog.i(slotData.toString());
        }
        fragmentManager = getFragmentManager();
        createFragments();
        triggerTypes = new ArrayList<>();
        if (deviceType == DEVICE_TYPE_SENSOR_NULL) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_no_sensor);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
        } else if (deviceType == DEVICE_TYPE_SENSOR_AXIS) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_axis);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Device moves");
        } else if (deviceType == DEVICE_TYPE_SENSOR_TH) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_th);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
        } else if (deviceType == DEVICE_TYPE_SENSOR_AXIS_TH) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_all);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
            triggerTypes.add("Device moves");
        } else if (deviceType == DEVICE_TYPE_SENSOR_LIGHT) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_no_sensor);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Ambient light detected");
        } else if (deviceType == DEVICE_TYPE_SENSOR_AXIS_LIGHT) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_axis);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Device moves");
            triggerTypes.add("Ambient light detected");
        } else if (deviceType == DEVICE_TYPE_SENSOR_TH_LIGHT) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_th);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
            triggerTypes.add("Ambient light detected");
        } else if (deviceType == DEVICE_TYPE_SENSOR_AXIS_TH_LIGHT) {
            slotTypeArray = getResources().getStringArray(R.array.slot_type_all);
            npvSlotType.setDisplayedValues(slotTypeArray);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
            triggerTypes.add("Device moves");
            triggerTypes.add("Ambient light detected");
        }
        final int length = slotTypeArray.length;
        npvSlotType.setMinValue(0);
        npvSlotType.setMaxValue(length - 1);
        npvSlotType.setOnValueChangedListener(this);
        for (int i = 0; i < length; i++) {
            if (slotData.frameTypeEnum.getShowName().equals(slotTypeArray[i])) {
                npvSlotType.setValue(i);
                showFragment(i);
                break;
            }
        }
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        if (slotData.frameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
        if (triggerType > 0) {
            ivTrigger.setImageResource(R.drawable.ic_checked);
            rlTrigger.setVisibility(View.VISIBLE);
        } else {
            ivTrigger.setImageResource(R.drawable.ic_unchecked);
            rlTrigger.setVisibility(View.GONE);
        }
        createTriggerFragments();
        showTriggerFragment();
        setTriggerData();

        EventBus.getDefault().register(this);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        }
    }

    private void setTriggerData() {
        switch (triggerType) {
            case TRIGGER_TYPE_TEMPERATURE:
                boolean isTempAbove = (triggerData[0] & 0xff) == 1;
                triggerTypeSelected = isTempAbove ? 2 : 3;

                tempFragment.setTempType(isTempAbove);
                tempFragment.setData(MokoUtils.byte2short(Arrays.copyOfRange(triggerData, 1, 3)));
                tempFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case TRIGGER_TYPE_HUMIDITY:
                boolean isHumidityAbove = (triggerData[0] & 0xff) == 1;
                triggerTypeSelected = isHumidityAbove ? 4 : 5;

                humidityFragment.setHumidityType(isHumidityAbove);
                byte[] humidityBytes = Arrays.copyOfRange(triggerData, 1, 3);
                humidityFragment.setData((MokoUtils.toInt(humidityBytes)));
                humidityFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case TRIGGER_TYPE_TRAP_DOUBLE:
                triggerTypeSelected = 0;

                tappedFragment.setIsDouble(true);
                byte[] tappedDoubleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedDoubleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case TRIGGER_TYPE_TRAP_TRIPLE:
                triggerTypeSelected = 1;

                tappedFragment.setIsDouble(false);
                byte[] tappedTripleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedTripleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case TRIGGER_TYPE_MOVE:
                if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    triggerTypeSelected = 2;
                }
                if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    triggerTypeSelected = 6;
                }
                byte[] movesBytes = Arrays.copyOfRange(triggerData, 0, 2);
                movesFragment.setData(MokoUtils.toInt(movesBytes));
                movesFragment.setStart((triggerData[2] & 0xff) == 2);
                break;
            case TRIGGER_TYPE_LIGHT:
                if (deviceType == DEVICE_TYPE_SENSOR_LIGHT) {
                    triggerTypeSelected = 2;
                }
                if (deviceType == DEVICE_TYPE_SENSOR_AXIS_LIGHT) {
                    triggerTypeSelected = 3;
                }
                if (deviceType == DEVICE_TYPE_SENSOR_TH_LIGHT) {
                    triggerTypeSelected = 6;
                }
                if (deviceType == DEVICE_TYPE_SENSOR_AXIS_TH_LIGHT) {
                    triggerTypeSelected = 7;
                }

                byte[] lightBytes = Arrays.copyOfRange(triggerData, 0, 2);
                lightDetectedFragment.setData(MokoUtils.toInt(lightBytes));
                lightDetectedFragment.setAlwaysAdv((triggerData[2] & 0xff) == 0);
                lightDetectedFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
        }
        tvTriggerType.setText(triggerTypes.get(triggerTypeSelected));
    }

    private void showTriggerFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (triggerType) {
            case TRIGGER_TYPE_TEMPERATURE:
                fragmentTransaction.show(tempFragment).hide(humidityFragment).hide(tappedFragment).hide(movesFragment).hide(lightDetectedFragment).commit();
                break;
            case TRIGGER_TYPE_HUMIDITY:
                fragmentTransaction.hide(tempFragment).show(humidityFragment).hide(tappedFragment).hide(movesFragment).hide(lightDetectedFragment).commit();
                break;
            case TRIGGER_TYPE_TRAP_DOUBLE:
            case TRIGGER_TYPE_TRAP_TRIPLE:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(tappedFragment).hide(movesFragment).hide(lightDetectedFragment).commit();
                break;
            case TRIGGER_TYPE_MOVE:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(tappedFragment).show(movesFragment).hide(lightDetectedFragment).commit();
                break;
            case TRIGGER_TYPE_LIGHT:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(tappedFragment).hide(movesFragment).show(lightDetectedFragment).commit();
                break;
        }
    }

    private void createTriggerFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        tempFragment = TriggerTempFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, tempFragment);
        humidityFragment = TriggerHumidityFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, humidityFragment);
        tappedFragment = TriggerTappedFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, tappedFragment);
        movesFragment = TriggerMovesFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, movesFragment);
        lightDetectedFragment = TriggerLightDetectedFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, lightDetectedFragment);
        fragmentTransaction.commit();
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {// 0、4
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
        } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {// 1、5
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            axisFragment = AxisFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, axisFragment);
        } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {// 2、6
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            thFragment = THFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, thFragment);
        } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {// 3、7
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            axisFragment = AxisFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, axisFragment);
            thFragment = THFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, thFragment);
        }
        fragmentTransaction.commit();

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    SlotDataActivity.this.finish();
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
                ToastUtils.showToast(SlotDataActivity.this, isConfigError ? "Error" : "Successfully configure");
                isConfigError = false;
                dismissSyncProgressDialog();
                SlotDataActivity.this.setResult(SlotDataActivity.this.RESULT_OK);
                SlotDataActivity.this.finish();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        int length = value.length;
                        if (length > 1) {
                            int key = value[1] & 0xFF;
                            if (key == 0x0D) {
                                isConfigError = true;
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
                            // 设备上锁
                            ToastUtils.showToast(SlotDataActivity.this, "Locked");
                            SlotDataActivity.this.finish();
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
                            // 蓝牙断开
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

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        XLog.i(newVal + "");
        XLog.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (slotDataActionImpl != null) {
            slotDataActionImpl.resetParams();
        }
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(slotTypeArray[newVal]);
        if (slotFrameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
    }

    private void showFragment(int index) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(slotTypeArray[index]);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (slotFrameTypeEnum) {
            case TLM:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                }
                break;
            case UID:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                }
                break;
            case URL:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                }
                break;
            case IBEACON:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                }
                break;
            case DEVICE:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                }
                break;
            case NO_DATA:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).commit();
                    slotDataActionImpl = null;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = null;
                } else if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                }
                break;
            case AXIS:
                if ((deviceType & 1) == 1 && (deviceType & 2) == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).commit();
                    slotDataActionImpl = axisFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = axisFragment;
                }
                break;
            case TH:
                if ((deviceType & 1) == 0 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).commit();
                    slotDataActionImpl = thFragment;
                } else if ((deviceType & 1) == 1 && (deviceType & 2) == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).hide(axisFragment).commit();
                    slotDataActionImpl = thFragment;
                }
                break;

        }
        slotData.frameTypeEnum = slotFrameTypeEnum;
    }

    public void onTrigger(View view) {
        if (isWindowLocked())
            return;
        if (triggerType > 0) {
            triggerType = 0;
            ivTrigger.setImageResource(R.drawable.ic_unchecked);
            rlTrigger.setVisibility(View.GONE);
        } else {
            ivTrigger.setImageResource(R.drawable.ic_checked);
            rlTrigger.setVisibility(View.VISIBLE);
            triggerType = 3;
            showTriggerFragment();
        }
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        OrderTask orderTask = null;
        // 发送触发条件
        switch (triggerType) {
            case TRIGGER_TYPE_NULL:
                orderTask = OrderTaskAssembler.setTriggerClose();
                break;
            case TRIGGER_TYPE_TEMPERATURE:
                orderTask = OrderTaskAssembler.setTHTrigger(triggerType, tempFragment.getTempType(), tempFragment.getData(), tempFragment.isStart());
                break;
            case TRIGGER_TYPE_HUMIDITY:
                orderTask = OrderTaskAssembler.setTHTrigger(triggerType, humidityFragment.getHumidityType(), humidityFragment.getData(), humidityFragment.isStart());
                break;
            case TRIGGER_TYPE_TRAP_DOUBLE:
            case TRIGGER_TYPE_TRAP_TRIPLE:
                if (tappedFragment.getData() < 0) {
                    return;
                }
                orderTask = OrderTaskAssembler.setTappedMovesTrigger(triggerType, tappedFragment.getData(), tappedFragment.isStart());
                break;
            case TRIGGER_TYPE_MOVE:
                if (movesFragment.getData() < 0) {
                    return;
                }
                orderTask = OrderTaskAssembler.setTappedMovesTrigger(triggerType, movesFragment.getData(), movesFragment.isStart());
                break;
            case TRIGGER_TYPE_LIGHT:
                if (movesFragment.getData() < 0) {
                    return;
                }
                orderTask = OrderTaskAssembler.setLightTrigger(triggerType, lightDetectedFragment.getData(), lightDetectedFragment.isAlways(), lightDetectedFragment.isStart());
                break;
        }
        if (slotDataActionImpl == null) {
            byte[] noData = new byte[]{(byte) 0xFF};
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setSlot(slotData.slotEnum));
            orderTasks.add(OrderTaskAssembler.setSlotData(noData));
            orderTasks.add(orderTask);
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            return;
        }
        if (!slotDataActionImpl.isValid()) {
            return;
        }
        showSyncingProgressDialog();
        slotDataActionImpl.sendData();
        if (orderTask != null) {
            MokoSupport.getInstance().sendOrder(orderTask);
        }
    }

    public void onTriggerType(View view) {
        if (isWindowLocked())
            return;
        // 选择触发条件
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(triggerTypes, triggerTypeSelected);
        dialog.setListener(value -> {
            triggerTypeSelected = value;
            switch (triggerTypeSelected) {
                case 0:
                    triggerType = TRIGGER_TYPE_TRAP_DOUBLE;
                    break;
                case 1:
                    triggerType = TRIGGER_TYPE_TRAP_TRIPLE;
                    break;
                case 2:
                    if (deviceType == DEVICE_TYPE_SENSOR_AXIS
                            || deviceType == DEVICE_TYPE_SENSOR_AXIS_LIGHT) {
                        triggerType = TRIGGER_TYPE_MOVE;
                    } else if (deviceType == DEVICE_TYPE_SENSOR_LIGHT) {
                        triggerType = TRIGGER_TYPE_LIGHT;
                    } else {
                        triggerType = TRIGGER_TYPE_TEMPERATURE;
                    }
                    break;
                case 3:
                    if (deviceType == DEVICE_TYPE_SENSOR_AXIS_LIGHT) {
                        triggerType = TRIGGER_TYPE_LIGHT;
                    } else {
                        triggerType = TRIGGER_TYPE_TEMPERATURE;
                    }
                    break;
                case 4:
                case 5:
                    triggerType = TRIGGER_TYPE_HUMIDITY;
                    break;
                case 6:
                    if (deviceType == DEVICE_TYPE_SENSOR_TH_LIGHT) {
                        triggerType = TRIGGER_TYPE_LIGHT;
                    } else {
                        triggerType = TRIGGER_TYPE_MOVE;
                    }
                    break;
                case 7:
                    triggerType = TRIGGER_TYPE_LIGHT;
                    break;
            }
            showTriggerFragment();
            switch (triggerTypeSelected) {
                case 0:
                    tappedFragment.setIsDouble(true);
                    tappedFragment.updateTips();
                    break;
                case 1:
                    tappedFragment.setIsDouble(false);
                    tappedFragment.updateTips();
                    break;
                case 2:
                    if ((deviceType & 2) == 2) {
                        tempFragment.setTempTypeAndRefresh(true);
                    }
                    break;
                case 3:
                    if ((deviceType & 2) == 2) {
                        tempFragment.setTempTypeAndRefresh(false);
                    }
                    break;
                case 4:
                    humidityFragment.setHumidityTypeAndRefresh(true);
                    break;
                case 5:
                    humidityFragment.setHumidityTypeAndRefresh(false);
                    break;
            }
            tvTriggerType.setText(triggerTypes.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSelectUrlScheme(View view) {
        if (isWindowLocked())
            return;
        if (urlFragment != null) {
            urlFragment.selectUrlScheme();
        }
    }
}
