package com.moko.bxp.h6.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.bxp.h6.R;
import com.moko.bxp.h6.activity.SlotDataActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TriggerTempFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TempFragment";
    @BindView(R.id.sb_trigger_temp)
    SeekBar sbTriggerTemp;
    @BindView(R.id.tv_trigger_temp)
    TextView tvTriggerTemp;
    @BindView(R.id.rb_start)
    RadioButton rbStart;
    @BindView(R.id.rb_stop)
    RadioButton rbStop;
    @BindView(R.id.rg_advertising)
    RadioGroup rgAdvertising;
    @BindView(R.id.tv_trigger_tips)
    TextView tvTriggerTips;
//    @BindView(R.id.trigger_temp)
//    TextView triggerTemp;


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
        View view = inflater.inflate(R.layout.fragment_trigger_temp, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        rgAdvertising.setOnCheckedChangeListener(this);
        sbTriggerTemp.setOnSeekBarChangeListener(this);
        if (mIsStart) {
            rbStart.setChecked(true);
        } else {
            rbStop.setChecked(true);
        }
        sbTriggerTemp.setProgress(mProgress);
        String tempStr = String.format("%d℃", mProgress - 20);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start to broadcast" : "stop broadcasting", "temperature", mIsAbove ? "above" : "below", tempStr));
//        triggerTemp.setText(mIsAbove ? "Temperature Above" : "Temperature Below");
        return view;
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
        tvTriggerTemp.setText(tempStr);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start to broadcast" : "stop broadcasting", "temperature", mIsAbove ? "above" : "below", tempStr));
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
        switch (checkedId) {
            case R.id.rb_start:
                mIsStart = true;
                break;
            case R.id.rb_stop:
                mIsStart = false;
                break;
        }
        String tempStr = String.format("%d℃", mProgress - 20);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start to broadcast" : "stop broadcasting", "temperature", mIsAbove ? "above" : "below", tempStr));
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
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start to broadcast" : "stop broadcasting", "temperature", mIsAbove ? "above" : "below", tempStr));
    }

    public boolean getTempType() {
        return mIsAbove;
    }
}
