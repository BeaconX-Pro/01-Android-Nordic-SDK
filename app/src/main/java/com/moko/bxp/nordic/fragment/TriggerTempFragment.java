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
import com.moko.bxp.nordic.databinding.FragmentTriggerTempBinding;

public class TriggerTempFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TempFragment";
    private FragmentTriggerTempBinding mBind;


    private SlotDataActivity activity;


    public TriggerTempFragment() {
    }

    public static TriggerTempFragment newInstance() {
        TriggerTempFragment fragment = new TriggerTempFragment();
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
        mBind = FragmentTriggerTempBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.rgAdvertising.setOnCheckedChangeListener(this);
        mBind.sbTriggerTemp.setOnSeekBarChangeListener(this);
        if (mIsStart) {
            mBind.rbStart.setChecked(true);
        } else {
            mBind.rbStop.setChecked(true);
        }
        mBind.sbTriggerTemp.setProgress(mProgress);
        String tempStr = String.format("%d℃", mProgress - 20);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "temperature", mIsAbove ? "above" : "below", tempStr));
//        triggerTemp.setText(mIsAbove ? "Temperature Above" : "Temperature Below");
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
        String tempStr = String.format("%d℃", progress - 20);
        mBind.tvTriggerTemp.setText(tempStr);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "temperature", mIsAbove ? "above" : "below", tempStr));
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
        String tempStr = String.format("%d℃", mProgress - 20);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "temperature", mIsAbove ? "above" : "below", tempStr));
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }

    public void setData(int data) {
        mProgress = (int) (data * 0.1f) + 20;
    }

    public int getData() {
        return (mProgress - 20) * 10;
    }

    public void setTempType(boolean isAbove) {
        mIsAbove = isAbove;
    }

    public void setTempTypeAndRefresh(boolean isAbove) {
        mIsAbove = isAbove;
        String tempStr = String.format("%d℃", mProgress - 20);
        mBind.tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start advertising" : "stop advertising", "temperature", mIsAbove ? "above" : "below", tempStr));
    }

    public boolean getTempType() {
        return mIsAbove;
    }
}
