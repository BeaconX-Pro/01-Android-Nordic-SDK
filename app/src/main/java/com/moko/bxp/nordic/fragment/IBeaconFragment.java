package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.able.ISlotDataAction;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.databinding.FragmentIbeaconBinding;
import com.moko.lib.bxpui.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.SlotFrameTypeEnum;
import com.moko.support.nordic.entity.TxPowerEnum;

import java.util.ArrayList;

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";

    private FragmentIbeaconBinding mBind;


    private SlotDataActivity activity;

    public IBeaconFragment() {
    }

    public static IBeaconFragment newInstance() {
        IBeaconFragment fragment = new IBeaconFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentIbeaconBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbAdvTxPower.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        mBind.etUuid.setTransformationMethod(new A2bigA());
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            mBind.sbAdvTxPower.setProgress(41);
            mBind.sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            mBind.etAdvInterval.setText(advIntervalProgress + "");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int advTxPowerProgress = activity.slotData.rssi_1m + 100;
                mBind.sbAdvTxPower.setProgress(advTxPowerProgress);
                rssiBytes = MokoUtils.toByteArray(activity.slotData.rssi_1m, 1);
                mBind.tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_1m));
            } else if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbAdvTxPower.setProgress(41);
                rssiBytes = MokoUtils.toByteArray(-59, 1);
                mBind.tvAdvTxPower.setText(String.format("%ddBm", -59));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                mBind.sbAdvTxPower.setProgress(advTxPowerProgress);
                rssiBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
                mBind.tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            mBind.tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            mBind.etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            mBind.etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            mBind.etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
        }
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

    private byte[] advIntervalBytes;
    private byte[] rssiBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        upgdateData(seekBar.getId(), progress);
    }


    public void upgdateData(int viewId, int progress) {
        if (viewId == R.id.sb_adv_tx_power) {
            int advTxPower = progress - 100;
            mBind.tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
            rssiBytes = MokoUtils.toByteArray(advTxPower, 1);
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            mBind.tvTxPower.setText(String.format("%ddBm", txPower));
            txPowerBytes = MokoUtils.toByteArray(txPower, 1);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private String major;
    private String minor;
    private String uuidHex;
    private byte[] iBeaconParamsBytes;

    @Override
    public boolean isValid() {
        String majorStr = mBind.etMajor.getText().toString();
        String minorStr = mBind.etMinor.getText().toString();
        String uuidStr = mBind.etUuid.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(majorStr) || TextUtils.isEmpty(minorStr) || TextUtils.isEmpty(uuidStr)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (Integer.valueOf(majorStr) > 65535 || Integer.valueOf(minorStr) > 65535 || uuidStr.length() != 32) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv interval range is 1~100");
            return false;
        }
        major = String.format("%04X", Integer.valueOf(majorStr));
        minor = String.format("%04X", Integer.valueOf(minorStr));
        uuidHex = uuidStr;
        String iBeaconParamsHex = SlotFrameTypeEnum.IBEACON.getFrameType() + uuidHex + major + minor;
        iBeaconParamsBytes = MokoUtils.hex2bytes(iBeaconParamsHex);
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt * 100, 2);
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setSlotData(iBeaconParamsBytes));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setRssi(rssiBytes));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advIntervalBytes));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public class A2bigA extends ReplacementTransformationMethod {

        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F'};
            return cc;
        }
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            mBind.etAdvInterval.setText(advIntervalProgress + "");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int rssiProgress = activity.slotData.rssi_1m + 100;
            mBind.sbAdvTxPower.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);

            mBind.etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            mBind.etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            mBind.etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            mBind.sbAdvTxPower.setProgress(41);
            mBind.sbTxPower.setProgress(6);

            mBind.etMajor.setText("");
            mBind.etMinor.setText("");
            mBind.etUuid.setText("");
        }
    }
}
