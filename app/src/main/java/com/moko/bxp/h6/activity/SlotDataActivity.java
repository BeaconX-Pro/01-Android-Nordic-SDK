package com.moko.bxp.h6.activity;

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
import com.moko.bxp.h6.AppConstants;
import com.moko.bxp.h6.R;
import com.moko.bxp.h6.able.ISlotDataAction;
import com.moko.bxp.h6.dialog.BottomDialog;
import com.moko.bxp.h6.dialog.LoadingMessageDialog;
import com.moko.bxp.h6.fragment.AxisFragment;
import com.moko.bxp.h6.fragment.DeviceInfoFragment;
import com.moko.bxp.h6.fragment.IBeaconFragment;
import com.moko.bxp.h6.fragment.THFragment;
import com.moko.bxp.h6.fragment.TlmFragment;
import com.moko.bxp.h6.fragment.TriggerHumidityFragment;
import com.moko.bxp.h6.fragment.TriggerMovesFragment;
import com.moko.bxp.h6.fragment.TriggerTappedFragment;
import com.moko.bxp.h6.fragment.TriggerTempFragment;
import com.moko.bxp.h6.fragment.UidFragment;
import com.moko.bxp.h6.fragment.UrlFragment;
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
import com.moko.support.h6.entity.SlotData;
import com.moko.support.h6.entity.SlotFrameTypeEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends FragmentActivity implements NumberPickerView.OnValueChangeListener {
    @BindView(R.id.tv_slot_title)
    TextView tvSlotTitle;
    @BindView(R.id.iv_save)
    ImageView ivSave;
    @BindView(R.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @BindView(R.id.npv_slot_type)
    NumberPickerView npvSlotType;
    @BindView(R.id.iv_trigger)
    ImageView ivTrigger;
    @BindView(R.id.tv_trigger_type)
    TextView tvTriggerType;
    @BindView(R.id.frame_trigger_container)
    FrameLayout frameTriggerContainer;
    @BindView(R.id.rl_trigger)
    RelativeLayout rlTrigger;
    @BindView(R.id.rl_trigger_switch)
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
    private boolean mReceiverTag = false;
    private int triggerType;
    private byte[] triggerData;
    private ArrayList<String> triggerTypes;
    private int triggerTypeSelected;
    public SlotFrameTypeEnum currentFrameTypeEnum;

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
        if (deviceType == 0) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_no_sensor));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(5);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
        } else if (deviceType == 1) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_axis));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(6);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Device moves");
        } else if (deviceType == 2) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_th));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(6);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
        } else if (deviceType == 3) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_all));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(7);
            triggerTypes.add("Press button twice");
            triggerTypes.add("Press button three times");
            triggerTypes.add("Temperature above");
            triggerTypes.add("Temperature below");
            triggerTypes.add("Humidity above");
            triggerTypes.add("Humidity below");
            triggerTypes.add("Device moves");
        }
        npvSlotType.setOnValueChangedListener(this);
        if (deviceType != 2) {
            npvSlotType.setValue(slotData.frameTypeEnum.ordinal());
            showFragment(slotData.frameTypeEnum.ordinal());
        } else {
            if (slotData.frameTypeEnum.ordinal() == 7) {
                npvSlotType.setValue(6);
                showFragment(6);
            } else {
                npvSlotType.setValue(slotData.frameTypeEnum.ordinal());
                showFragment(slotData.frameTypeEnum.ordinal());
            }
        }
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        if (slotData.frameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
        if (triggerType > 0) {
            ivTrigger.setImageResource(R.drawable.connectable_checked);
            rlTrigger.setVisibility(View.VISIBLE);
        } else {
            ivTrigger.setImageResource(R.drawable.connectable_unchecked);
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
            case 1:
                boolean isTempAbove = (triggerData[0] & 0xff) == 1;
                tvTriggerType.setText(isTempAbove ? triggerTypes.get(2) : triggerTypes.get(3));

                triggerTypeSelected = isTempAbove ? 2 : 3;
                tempFragment.setTempType(isTempAbove);
                tempFragment.setData(MokoUtils.byte2short(Arrays.copyOfRange(triggerData, 1, 3)));
                tempFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case 2:
                boolean isHumidityAbove = (triggerData[0] & 0xff) == 1;
                tvTriggerType.setText(isHumidityAbove ? triggerTypes.get(4) : triggerTypes.get(5));

                triggerTypeSelected = isHumidityAbove ? 4 : 5;
                humidityFragment.setHumidityType(isHumidityAbove);
                byte[] humidityBytes = Arrays.copyOfRange(triggerData, 1, 3);
                humidityFragment.setData((MokoUtils.toInt(humidityBytes)));
                humidityFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case 3:
                tvTriggerType.setText(triggerTypes.get(0));

                triggerTypeSelected = 0;
                tappedFragment.setIsDouble(true);
                byte[] tappedDoubleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedDoubleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case 4:
                tvTriggerType.setText(triggerTypes.get(1));

                triggerTypeSelected = 1;
                tappedFragment.setIsDouble(false);
                byte[] tappedTrapleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedTrapleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case 5:
                if (deviceType == 1) {
                    tvTriggerType.setText(triggerTypes.get(2));
                    triggerTypeSelected = 2;
                } else {
                    tvTriggerType.setText(triggerTypes.get(6));
                    triggerTypeSelected = 6;
                }
                byte[] movesBytes = Arrays.copyOfRange(triggerData, 0, 2);
                movesFragment.setData(MokoUtils.toInt(movesBytes));
                movesFragment.setStart((triggerData[2] & 0xff) == 2);
                break;
        }
    }

    private void showTriggerFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (triggerType) {
            case 1:
                fragmentTransaction.show(tempFragment).hide(humidityFragment).hide(tappedFragment).hide(movesFragment).commit();
                break;
            case 2:
                fragmentTransaction.hide(tempFragment).show(humidityFragment).hide(tappedFragment).hide(movesFragment).commit();
                break;
            case 3:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(tappedFragment).hide(movesFragment).commit();
                break;
            case 4:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(tappedFragment).hide(movesFragment).commit();
                break;
            case 5:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(tappedFragment).show(movesFragment).commit();
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
        fragmentTransaction.commit();
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (deviceType == 0) {
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
        } else if (deviceType == 1) {
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
        } else if (deviceType == 2) {
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
        } else if (deviceType == 3) {
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
                ToastUtils.showToast(SlotDataActivity.this, "Successfully configure");
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
                    case CHAR_ADV_INTERVAL:
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

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.tv_trigger_type, R.id.iv_trigger})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_save:
                OrderTask orderTask = null;
                // 发送触发条件
                switch (triggerType) {
                    case 0:
                        orderTask = OrderTaskAssembler.setTriggerClose();
                        break;
                    case 1:
                        orderTask = OrderTaskAssembler.setTHTrigger(triggerType, tempFragment.getTempType(), tempFragment.getData(), tempFragment.isStart());
                        break;
                    case 2:
                        orderTask = OrderTaskAssembler.setTHTrigger(triggerType, humidityFragment.getHumidityType(), humidityFragment.getData(), humidityFragment.isStart());
                        break;
                    case 3:
                    case 4:
                        if (tappedFragment.getData() < 0) {
                            return;
                        }
                        orderTask = OrderTaskAssembler.setTappedMovesTrigger(triggerType, tappedFragment.getData(), tappedFragment.isStart());
                        break;
                    case 5:
                        if (movesFragment.getData() < 0) {
                            return;
                        }
                        orderTask = OrderTaskAssembler.setTappedMovesTrigger(triggerType, movesFragment.getData(), movesFragment.isStart());
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
                break;
            case R.id.tv_trigger_type:
                // 选择触发条件
                BottomDialog dialog = new BottomDialog();
                dialog.setDatas(triggerTypes, triggerTypeSelected);
                dialog.setListener(value -> {
                    triggerTypeSelected = value;
                    switch (triggerTypeSelected) {
                        case 0:
                            triggerType = 3;
                            break;
                        case 1:
                            triggerType = 4;
                            break;
                        case 2:
                            if (deviceType == 1) {
                                triggerType = 5;
                            } else {
                                triggerType = 1;
                            }
                            break;
                        case 3:
                            triggerType = 1;
                            break;
                        case 4:
                            triggerType = 2;
                            break;
                        case 5:
                            triggerType = 2;
                            break;
                        case 6:
                            triggerType = 5;
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
                            if (deviceType != 1) {
                                tempFragment.setTempTypeAndRefresh(true);
                            }
                            break;
                        case 3:
                            tempFragment.setTempTypeAndRefresh(false);
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
                break;
            case R.id.iv_trigger:
                if (triggerType > 0) {
                    triggerType = 0;
                    ivTrigger.setImageResource(R.drawable.connectable_unchecked);
                    rlTrigger.setVisibility(View.GONE);
                } else {
                    ivTrigger.setImageResource(R.drawable.connectable_checked);
                    rlTrigger.setVisibility(View.VISIBLE);
                    triggerType = 3;
                    showTriggerFragment();
                }
                break;
        }
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        XLog.i(newVal + "");
        XLog.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (slotDataActionImpl != null) {
            slotDataActionImpl.resetParams();
        }
        if (SlotFrameTypeEnum.fromEnumOrdinal(newVal) != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
    }

    private void showFragment(int newVal) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (newVal) {
            case 0:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                }
                break;
            case 1:
                if (deviceType == 0) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                }
                break;
            case 2:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                }
                break;
            case 3:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                }
                break;
            case 4:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                }
                break;
            case 5:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                }
                break;
            case 6:
                if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).commit();
                    slotDataActionImpl = axisFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).commit();
                    slotDataActionImpl = thFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = axisFragment;
                }
                break;
            case 7:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).hide(axisFragment).commit();
                slotDataActionImpl = thFragment;
                break;

        }
        if (deviceType == 2 && newVal == 6) {
            slotData.frameTypeEnum = SlotFrameTypeEnum.TH;
        } else {
            slotData.frameTypeEnum = SlotFrameTypeEnum.fromEnumOrdinal(newVal);
        }
    }
}
