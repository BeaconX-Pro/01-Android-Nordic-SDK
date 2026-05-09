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
import com.moko.bxp.nordic.databinding.FragmentTriggerMovesBinding;
import com.moko.lib.bxpui.utils.ToastUtils;

public class TriggerMovesFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MovesFragment";


    private FragmentTriggerMovesBinding mBind;


    private SlotDataActivity activity;


    public TriggerMovesFragment() {
    }

    public static TriggerMovesFragment newInstance() {
        TriggerMovesFragment fragment = new TriggerMovesFragment();
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
        mBind = FragmentTriggerMovesBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1));
        if (mDuration == 0) {
            if (!mIsStart) {
                mBind.rbAlwaysStart.setChecked(true);
            }
        } else {
            if (mIsStart) {
                mBind.rbStartAdvertising.setChecked(true);
                mBind.etStop.setText(mDuration + "");
                mBind.etStop.setSelection((mDuration + "").length());
                mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "start advertising", String.format("%ds", mDuration), "stops advertising"));
            } else {
                mBind.rbStopAdvertising.setChecked(true);
                mBind.etStart.setText(mDuration + "");
                mBind.etStart.setSelection((mDuration + "").length());
                mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "stop advertising", String.format("%ds", mDuration), "starts advertising"));
            }
        }
        mBind.rgMoves.setOnCheckedChangeListener(this);
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
                if (mBind.rbStopAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "stop advertising", String.format("%ds", mDuration), "starts advertising"));
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
                if (mBind.rbStartAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "start advertising", String.format("%ds", mDuration), "stops advertising"));
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_always_start) {
            mIsStart = false;
            mDuration = 0;
            mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1));
        } else if (checkedId == R.id.rb_start_advertising) {
            mIsStart = true;
            String startDuration = mBind.etStop.getText().toString();
            if (TextUtils.isEmpty(startDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(startDuration);
            }
            mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "start advertising", String.format("%ds", mDuration), "stops advertising"));
        } else if (checkedId == R.id.rb_stop_advertising) {
            mIsStart = false;
            String stopDuration = mBind.etStart.getText().toString();
            if (TextUtils.isEmpty(stopDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(stopDuration);
            }
            mBind.tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "stop advertising", String.format("%ds", mDuration), "starts advertising"));
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
            duration = mBind.etStop.getText().toString();
        } else if (mBind.rbStopAdvertising.isChecked()) {
            duration = mBind.etStart.getText().toString();
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
}
