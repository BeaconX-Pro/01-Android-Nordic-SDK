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
import com.moko.bxp.nordic.databinding.FragmentTriggerLightBinding;
import com.moko.lib.bxpui.utils.ToastUtils;

public class TriggerLightDetectedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = TriggerLightDetectedFragment.class.getSimpleName();


    private FragmentTriggerLightBinding mBind;


    private SlotDataActivity activity;


    public TriggerLightDetectedFragment() {
    }

    public static TriggerLightDetectedFragment newInstance() {
        TriggerLightDetectedFragment fragment = new TriggerLightDetectedFragment();
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
        mBind = FragmentTriggerLightBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_1));
        if (mIsAlways) {
            mBind.rbAlwaysStart.setChecked(true);
        } else {
            if (mIsStart) {
                mBind.rbStartAdvertising.setChecked(true);
                mBind.etStart.setText(mDuration + "");
                mBind.etStart.setSelection((mDuration + "").length());
                mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
            } else {
                mBind.rbStopAdvertising.setChecked(true);
                mBind.etStop.setText(mDuration + "");
                mBind.etStop.setSelection((mDuration + "").length());
                mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
            }
        }
        mBind.rgLightDetected.setOnCheckedChangeListener(this);
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
                    mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
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
                    mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
                }
            }
        });
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

    private boolean mIsStart = true;
    private int mDuration = 30;
    private boolean mIsAlways = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_always_start) {
            mIsAlways = true;
            mIsStart = false;
            mDuration = 0;
            mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1));
        } else if (checkedId == R.id.rb_start_advertising) {
            mIsAlways = false;
            mIsStart = true;
            String startDuration = mBind.etStart.getText().toString();
            if (TextUtils.isEmpty(startDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(startDuration);
            }
            mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
        } else if (checkedId == R.id.rb_stop_advertising) {
            mIsAlways = false;
            mIsStart = false;
            String stopDuration = mBind.etStop.getText().toString();
            if (TextUtils.isEmpty(stopDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(stopDuration);
            }
            mBind.tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
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

    public void setAlwaysAdv(boolean isAlways) {
        mIsAlways = isAlways;
    }

    public boolean isAlways() {
        return mIsAlways;
    }
}
