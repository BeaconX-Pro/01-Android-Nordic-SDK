package com.moko.bxp.h6.fragment;

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

import com.moko.bxp.h6.R;
import com.moko.bxp.h6.able.ISlotDataAction;
import com.moko.bxp.h6.activity.SlotDataActivity;
import com.moko.bxp.h6.utils.ToastUtils;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.h6.MokoSupport;
import com.moko.support.h6.OrderTaskAssembler;
import com.moko.support.h6.entity.SlotFrameTypeEnum;
import com.moko.support.h6.entity.TxPowerEnum;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UidFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UidFragment";
    @BindView(R.id.et_namespace)
    EditText etNamespace;
    @BindView(R.id.et_instance_id)
    EditText etInstanceId;
    @BindView(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R.id.et_adv_interval)
    EditText etAdvInterval;

    private SlotDataActivity activity;

    public UidFragment() {
    }

    public static UidFragment newInstance() {
        UidFragment fragment = new UidFragment();
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
        View view = inflater.inflate(R.layout.fragment_uid, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        etNamespace.setTransformationMethod(new A2bigA());
        etInstanceId.setTransformationMethod(new A2bigA());
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbAdvTxPower.setProgress(100);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbAdvTxPower.setProgress(100);
                advTxPowerBytes = MokoUtils.toByteArray(0, 1);
                tvAdvTxPower.setText(String.format("%ddBm", 0));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                sbAdvTxPower.setProgress(advTxPowerProgress);
                advTxPowerBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
                tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
            etNamespace.setText(activity.slotData.namespace);
            etInstanceId.setText(activity.slotData.instanceId);
            etNamespace.setSelection(etNamespace.getText().toString().length());
            etInstanceId.setSelection(etInstanceId.getText().toString().length());
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
    private byte[] advTxPowerBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
//            upgdateData(seekBar.getId(), progress);
//            activity.onProgressChanged(seekBar.getId(), progress);
//        }
//        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
        upgdateData(seekBar.getId(), progress);
//        }
    }

    public void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_tx_power:
                int advTxPower = progress - 100;
                tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
                advTxPowerBytes = MokoUtils.toByteArray(advTxPower, 1);
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private byte[] uidParamsBytes;

    @Override
    public boolean isValid() {
        String namespace = etNamespace.getText().toString();
        String instanceId = etInstanceId.getText().toString();
        String advInterval = etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(namespace) || TextUtils.isEmpty(instanceId)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (namespace.length() != 20 || instanceId.length() != 12) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv Interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv Interval range is 1~100");
            return false;
        }
        String uidParamsStr = activity.slotData.frameTypeEnum.getFrameType() + namespace + instanceId;
        uidParamsBytes = MokoUtils.hex2bytes(uidParamsStr);
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt * 100, 2);
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setSlotData(uidParamsBytes));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvTxPower(advTxPowerBytes));
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

            int advTxPowerProgress = activity.slotData.rssi_0m + 100;
            sbAdvTxPower.setProgress(advTxPowerProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);

            etNamespace.setText(activity.slotData.namespace);
            etInstanceId.setText(activity.slotData.instanceId);
            etNamespace.setSelection(etNamespace.getText().toString().length());
            etInstanceId.setSelection(etInstanceId.getText().toString().length());
        } else {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbAdvTxPower.setProgress(100);
            sbTxPower.setProgress(6);

            etNamespace.setText("");
            etInstanceId.setText("");
        }
    }
}
