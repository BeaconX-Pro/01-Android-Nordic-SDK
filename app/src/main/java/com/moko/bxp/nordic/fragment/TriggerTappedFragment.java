package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.databinding.FragmentTriggerTappedBinding;
import com.moko.bxp.nordic.utils.ToastUtils;

public class TriggerTappedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TappedFragment";


   private FragmentTriggerTappedBinding mBind;


    private SlotDataActivity activity;


    public TriggerTappedFragment() {
    }

    public static TriggerTappedFragment newInstance() {
        TriggerTappedFragment fragment = new TriggerTappedFragment();
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
        mBind = FragmentTriggerTappedBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        if (mDuration == 0) {
            if (mIsStart) {
                mBind.rbAlwaysStart.setChecked(true);
            }
        } else {
            if (mIsStart) {
                mBind.rbStartAdvertising.setChecked(true);
                mBind.etStart.setText(mDuration + "");
                mBind.etStart.setSelection((mDuration + "").length());
            } else {
                mBind.rbStopAdvertising.setChecked(true);
                mBind.etStop.setText(mDuration + "");
                mBind.etStop.setSelection((mDuration + "").length());
            }
        }
        mBind.rgTapped.setOnCheckedChangeListener(this);
        updateTips();
        mBind.etStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (mBind.rbStartAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    String tips = "";
                    if (mTrapType == 0)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "single click button");
                    if (mTrapType == 1)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button twice");
                    if (mTrapType == 2)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button three times");
                    mBind.tvTriggerTips.setText(tips);
                }
            }
        });
        mBind.etStop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (mBind.rbStopAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    String tips = "";
                    if (mTrapType == 0)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "single click button");
                    if (mTrapType == 1)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button twice");
                    if (mTrapType == 2)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button three times");
                    mBind.tvTriggerTips.setText(tips);
                }
            }
        });
        return mBind.getRoot();
    }

    public void updateTips() {
        if (mBind.rbAlwaysStart.isChecked()) {
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_1, "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_1, "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_1, "press the button three times");
            mBind.tvTriggerTips.setText(tips);
        } else if (mBind.rbStartAdvertising.isChecked()) {
            mDuration = Integer.parseInt(mBind.etStart.getText().toString());
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button three times");
            mBind.tvTriggerTips.setText(tips);
        } else {
            mDuration = Integer.parseInt(mBind.etStop.getText().toString());
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button three times");
            mBind.tvTriggerTips.setText(tips);
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

    private boolean mIsStart = true;
    private int mDuration = 30;
    private int mTrapType = 1;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_always_start) {
            mIsStart = true;
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_1, "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_1,  "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_1, "press the button three times");
            mBind.tvTriggerTips.setText(tips);
        } else if (checkedId == R.id.rb_start_advertising) {
            mIsStart = true;
            String startDuration = mBind.etStart.getText().toString();
            if (TextUtils.isEmpty(startDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(startDuration);
            }
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button three times");
            mBind.tvTriggerTips.setText(tips);
        } else if (checkedId == R.id.rb_stop_advertising) {
            mIsStart = false;
            String stopDuration = mBind.etStop.getText().toString();
            if (TextUtils.isEmpty(stopDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(stopDuration);
            }
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button three times");
            mBind.tvTriggerTips.setText(tips);
        }
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }


    public void setData(int data) {
        mDuration = data;
    }

    public int getData() {
        String duration = "";
        if (mBind.rbStartAdvertising.isChecked()) {
            duration = mBind.etStart.getText().toString();
        } else if (mBind.rbStopAdvertising.isChecked()) {
            duration = mBind.etStop.getText().toString();
        } else {
            duration = "0";
        }
        if (TextUtils.isEmpty(duration)) {
            ToastUtils.showToast(getActivity(), "The advertising can not be empty.");
            return -1;
        }
        mDuration = Integer.parseInt(duration);
        if ((mBind.rbStartAdvertising.isChecked() || mBind.rbStopAdvertising.isChecked()) && (mDuration < 1 || mDuration > 65535)) {
            ToastUtils.showToast(activity, "The advertising range is 1~65535");
            return -1;
        }
        return mDuration;
    }

    public int getTrapType() {
        return mTrapType;
    }

    public void setTrapType(int trapType) {
        this.mTrapType = trapType;
    }
}
