package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.able.ISlotDataAction;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.SlotFrameTypeEnum;
import com.moko.support.nordic.entity.TxPowerEnum;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";

    @BindView(R2.id.sb_adv_tx_power)
    SeekBar sbRssi;
    @BindView(R2.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R2.id.et_major)
    EditText etMajor;
    @BindView(R2.id.et_minor)
    EditText etMinor;
    @BindView(R2.id.et_uuid)
    EditText etUuid;
    @BindView(R2.id.tv_adv_tx_power)
    TextView tvRssi;
    @BindView(R2.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R2.id.et_adv_interval)
    EditText etAdvInterval;


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
        View view = inflater.inflate(R.layout.fragment_ibeacon, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbRssi.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        etUuid.setTransformationMethod(new A2bigA());
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbRssi.setProgress(41);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int advTxPowerProgress = activity.slotData.rssi_1m + 100;
                sbRssi.setProgress(advTxPowerProgress);
                rssiBytes = MokoUtils.toByteArray(activity.slotData.rssi_1m, 1);
                tvRssi.setText(String.format("%ddBm", activity.slotData.rssi_1m));
            } else if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbRssi.setProgress(41);
                rssiBytes = MokoUtils.toByteArray(-59, 1);
                tvRssi.setText(String.format("%ddBm", -59));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                sbRssi.setProgress(advTxPowerProgress);
                rssiBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
                tvRssi.setText(String.format("%ddBm", activity.slotData.rssi_0m));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            etMajor.setSelection(etMajor.getText().toString().length());
            etMinor.setSelection(etMinor.getText().toString().length());
            etUuid.setSelection(etUuid.getText().toString().length());
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
            tvRssi.setText(String.format("%ddBm", advTxPower));
            rssiBytes = MokoUtils.toByteArray(advTxPower, 1);
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            tvTxPower.setText(String.format("%ddBm", txPower));
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
        String majorStr = etMajor.getText().toString();
        String minorStr = etMinor.getText().toString();
        String uuidStr = etUuid.getText().toString();
        String advInterval = etAdvInterval.getText().toString();
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
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int rssiProgress = activity.slotData.rssi_1m + 100;
            sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);

            etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            etMajor.setSelection(etMajor.getText().toString().length());
            etMinor.setSelection(etMinor.getText().toString().length());
            etUuid.setSelection(etUuid.getText().toString().length());
        } else {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbRssi.setProgress(41);
            sbTxPower.setProgress(6);

            etMajor.setText("");
            etMinor.setText("");
            etUuid.setText("");
        }
    }
}
