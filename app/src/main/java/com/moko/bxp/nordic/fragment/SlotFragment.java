package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.activity.DeviceInfoActivity;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.databinding.FragmentSlotBinding;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.SlotData;
import com.moko.support.nordic.entity.SlotEnum;
import com.moko.support.nordic.entity.SlotFrameTypeEnum;
import com.moko.support.nordic.entity.UrlSchemeEnum;

import java.util.ArrayList;
import java.util.Arrays;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    private FragmentSlotBinding mBind;

    private DeviceInfoActivity activity;
    private SlotData slotData;
    private int deviceType;
    private boolean mSupportTamperDetect;
    private boolean mNoSingleTrigger;
    private int triggerType;
    private String triggerData;
    private int slotEnable;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSlotBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData = new SlotData();
        slotData.frameTypeEnum = frameType;
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_ENABLE, slotEnable);
                intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, deviceType);
                intent.putExtra(AppConstants.EXTRA_KEY_TAMPER_DETECT, mSupportTamperDetect);
                intent.putExtra(AppConstants.EXTRA_KEY_NO_SINGLE_TRIGGER, mNoSingleTrigger);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
                break;
            case IBEACON:
            case TLM:
            case URL:
            case UID:
            case DEVICE:
            case TH:
            case AXIS:
                getSlotData(slot);
                break;
        }
    }

    private void getSlotData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(slotEnum));
        orderTasks.add(OrderTaskAssembler.getSlotData());
        orderTasks.add(OrderTaskAssembler.getTrigger());
        orderTasks.add(OrderTaskAssembler.getRssi());
        orderTasks.add(OrderTaskAssembler.getRadioTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

//    private void getiBeaconData(SlotEnum slotEnum) {
//        activity.showSyncingProgressDialog();
//        MokoSupport.getInstance().sendOrder(
//                activity.mMokoService.setSlot(slotEnum),
//                activity.mMokoService.getiBeaconUUID(),
//                activity.mMokoService.getiBeaconInfo(),
//                activity.mMokoService.getRadioTxPower(),
//                activity.mMokoService.getAdvInterval()
//        );
//    }

    // 10 20 50 40 FF FF
    public void updateSlotType(byte[] value) {
        changeView((int) value[0] & 0xff, 0, mBind.tvSlot1, mBind.rlSlot1);
        changeView((int) value[1] & 0xff, 1, mBind.tvSlot2, mBind.rlSlot2);
        changeView((int) value[2] & 0xff, 2, mBind.tvSlot3, mBind.rlSlot3);
        changeView((int) value[3] & 0xff, 3, mBind.tvSlot4, mBind.rlSlot4);
        changeView((int) value[4] & 0xff, 4, mBind.tvSlot5, mBind.rlSlot5);
        changeView((int) value[5] & 0xff, 5, mBind.tvSlot6, mBind.rlSlot6);
    }

    private void changeView(int frameType, int slot, TextView tvSlot, RelativeLayout rlSlot) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum == null) {
            return;
        }
        if (frameType != 0xFF) slotEnable += 1 << slot;
        tvSlot.setText(slotFrameTypeEnum.getShowName());
        rlSlot.setTag(slotFrameTypeEnum);
    }

    //    private String iBeaconUUID;
//    private String major;
//    private String minor;
//    private int rssi_1m;
    private int txPower;
    private int advInterval;

    // eb640010e2c56db5dffb48d2b060d0f5a71096e0
//    public void setiBeaconUUID(byte[] value) {
//        String valueHex = MokoUtils.bytesToHexString(value);
//        iBeaconUUID = valueHex.substring(8);
//        slotData.iBeaconUUID = iBeaconUUID;
//    }

    // eb6600050000000000
//    public void setiBeaconInfo(byte[] value) {
//        String valueHex = MokoUtils.bytesToHexString(value);
//        major = valueHex.substring(8, 12);
//        minor = valueHex.substring(12, 16);
//        rssi_1m = Integer.parseInt(valueHex.substring(16), 16);
//        slotData.major = major;
//        slotData.minor = minor;
//        slotData.rssi_1m = 0 - rssi_1m;
//    }

    // 00
    public void setTxPower(byte[] value) {
        txPower = value[0];
        slotData.txPower = txPower;
    }

    // 00
    public void setAdvTxPower(byte[] value) {
        switch (mSlotFrameTypeEnum) {
            case IBEACON:
                slotData.rssi_1m = value[0];
                break;
            default:
                slotData.rssi_0m = value[0];
                break;
        }
    }

    // 0064
    public void setAdvInterval(byte[] value) {
        advInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
        slotData.advInterval = advInterval;
    }

    public void gotoSlotDataDetail() {
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_ENABLE, slotEnable);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, deviceType);
        intent.putExtra(AppConstants.EXTRA_KEY_TRIGGER_TYPE, triggerType);
        intent.putExtra(AppConstants.EXTRA_KEY_TRIGGER_DATA, triggerData);
        intent.putExtra(AppConstants.EXTRA_KEY_TAMPER_DETECT, mSupportTamperDetect);
        intent.putExtra(AppConstants.EXTRA_KEY_NO_SINGLE_TRIGGER, mNoSingleTrigger);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_SLOT_DATA) {
                Log.i(TAG, "onActivityResult: ");
                slotEnable = 0;
                activity.getSlotType();
            }
        }
    }

    private SlotFrameTypeEnum mSlotFrameTypeEnum;

    // 不同类型的数据长度不同
    public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            mSlotFrameTypeEnum = slotFrameTypeEnum;
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    if (value.length > 3) {
                        int urlType = (int) value[2] & 0xff;
                        slotData.urlSchemeEnum = UrlSchemeEnum.fromUrlType(urlType);
                        slotData.urlContent = MokoUtils.bytesToHexString(value).substring(6);
                    }
                    break;
                case UID:
                    if (value.length >= 18) {
                        slotData.namespace = MokoUtils.bytesToHexString(value).substring(4, 24);
                        slotData.instanceId = MokoUtils.bytesToHexString(value).substring(24);
                    }
                    break;
                case DEVICE:
                    byte[] deviceName = Arrays.copyOfRange(value, 1, value.length);
                    slotData.deviceName = new String(deviceName);
                    break;
                case IBEACON:
                    byte[] uuid = Arrays.copyOfRange(value, 1, 17);
                    byte[] major = Arrays.copyOfRange(value, 17, 19);
                    byte[] minor = Arrays.copyOfRange(value, 19, 21);
                    slotData.iBeaconUUID = MokoUtils.bytesToHexString(uuid);
                    slotData.major = MokoUtils.bytesToHexString(major);
                    slotData.minor = MokoUtils.bytesToHexString(minor);
                    break;
            }
        }
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public void setSupportTamperDetect(boolean supportTamperDetect) {
        this.mSupportTamperDetect = supportTamperDetect;
    }

    public void setNoSingleTrigger(boolean noSingleTrigger) {
        this.mNoSingleTrigger = noSingleTrigger;
    }


    public void setTriggerData(byte[] value) {
        triggerType = value[4] & 0xff;
        if (triggerType != 0) {
            triggerData = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 5, value.length));
        }
    }
}
