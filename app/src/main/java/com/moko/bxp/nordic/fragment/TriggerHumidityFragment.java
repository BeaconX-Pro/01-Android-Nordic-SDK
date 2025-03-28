package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.databinding.FragmentTriggerHumidityBinding;

public class TriggerHumidityFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "HumidityFragment";

    private FragmentTriggerHumidityBinding mBind;


    private SlotDataActivity activity;


    public TriggerHumidityFragment() {
    }

    public static TriggerHumidityFragment newInstance() {
        TriggerHumidityFragment fragment = new TriggerHumidityFragment();
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
        mBind = FragmentTriggerHumidityBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.rgAdvertising.setOnCheckedChangeListener(this);
        mBind.sbTriggerHumidity.setOnSeekBarChangeListener(this);
        if (mIsStart) {
            mBind.rbStart.setChecked(true);
        } else {
            mBind.rbStop.setChecked(true);
        }
        mBind.sbTriggerHumidity.setProgress(mProgress);
        String humidityStr = String.format("%d%%", mProgress);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "humidity", mIsAbove ? "above" : "below", humidityStr));
//        triggerHumidiy.setText(mIsAbove ? "Humidity Above" : "Humidity Below");
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mProgress = progress;
        String humidityStr = String.format("%d%%", progress);
        mBind.tvTriggerHumidiy.setText(humidityStr);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "humidity", mIsAbove ? "above" : "below", humidityStr));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private boolean mIsStart = true;
    private int mProgress;
    private boolean mIsAbove = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_start) {
            mIsStart = true;
        } else if (checkedId == R.id.rb_stop) {
            mIsStart = false;
        }
        String humidityStr = String.format("%d%%", mProgress);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "humidity", mIsAbove ? "above" : "below", humidityStr));
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }

    public void setData(int data) {
        mProgress = (int) (data * 0.1f);
    }

    public int getData() {
        return (mProgress) * 10;
    }

    public void setHumidityType(boolean isAbove) {
        mIsAbove = isAbove;
    }

    public void setHumidityTypeAndRefresh(boolean isAbove) {
        mIsAbove = isAbove;
        String humidityStr = String.format("%d%%", mProgress);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "humidity", mIsAbove ? "above" : "below", humidityStr));
    }

    public boolean getHumidityType() {
        return mIsAbove;
    }
}
