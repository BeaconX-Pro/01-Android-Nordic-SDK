package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.moko.bxp.nordic.databinding.FragmentTlmBinding;
import com.moko.lib.bxpui.utils.ToastUtils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.SlotFrameTypeEnum;
import com.moko.support.nordic.entity.TxPowerEnum;

import java.util.ArrayList;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "TlmFragment";


    private FragmentTlmBinding mBind;

    private SlotDataActivity activity;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        TlmFragment fragment = new TlmFragment();
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
        mBind = FragmentTlmBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            mBind.sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            mBind.etAdvInterval.setText(advIntervalProgress + "");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            mBind.tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
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
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        upgdateData(seekBar.getId(), progress);
    }

    private void upgdateData(int viewId, int progress) {
        if (viewId == R.id.sb_tx_power) {
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

    @Override
    public boolean isValid() {
        String advInterval = mBind.etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv interval range is 1~100");
            return false;
        }
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt * 100, 2);
        return true;
    }

    @Override
    public void sendData() {
        byte[] tlmBytes = MokoUtils.hex2bytes(SlotFrameTypeEnum.TLM.getFrameType());
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setSlotData(tlmBytes));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advIntervalBytes));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            mBind.etAdvInterval.setText(advIntervalProgress + "");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().toString().length());
            mBind.sbTxPower.setProgress(6);
        }
    }
}
