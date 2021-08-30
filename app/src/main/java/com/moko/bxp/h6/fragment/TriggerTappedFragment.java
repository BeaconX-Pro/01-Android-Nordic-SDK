package com.moko.bxp.h6.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.bxp.h6.R;
import com.moko.bxp.h6.activity.SlotDataActivity;
import com.moko.bxp.h6.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TriggerTappedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TappedFragment";


    @BindView(R.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @BindView(R.id.rb_always_start)
    RadioButton rbAlwaysStart;
    @BindView(R.id.rb_start_advertising)
    RadioButton rbStartAdvertising;
    @BindView(R.id.rb_stop_advertising)
    RadioButton rbStopAdvertising;
    @BindView(R.id.rg_tapped)
    RadioGroup rgTapped;
    @BindView(R.id.et_start)
    EditText etStart;
    @BindView(R.id.et_stop)
    EditText etStop;


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
        View view = inflater.inflate(R.layout.fragment_trigger_tapped, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        if (mDuration == 0) {
            if (mIsStart) {
                rbAlwaysStart.setChecked(true);
            }
        } else {
            if (mIsStart) {
                rbStartAdvertising.setChecked(true);
                etStart.setText(mDuration + "");
                etStart.setSelection((mDuration + "").length());
            } else {
                rbStopAdvertising.setChecked(true);
                etStop.setText(mDuration + "");
                etStop.setSelection((mDuration + "").length());
            }
        }
        rgTapped.setOnCheckedChangeListener(this);
        updateTips();
        etStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (rbStartAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start to broadcast", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
                }
            }
        });
        etStop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String duration = s.toString();
                if (rbStopAdvertising.isChecked() && !TextUtils.isEmpty(duration)) {
                    mDuration = Integer.parseInt(duration);
                    tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop broadcasting", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
                }
            }
        });
        return view;
    }

    public void updateTips() {
        if (rbAlwaysStart.isChecked()) {
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_1, mIsDouble ? "twice" : "three times"));
        } else if (rbStartAdvertising.isChecked()) {
            mDuration = Integer.parseInt(etStart.getText().toString());
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start to broadcast", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
        } else {
            mDuration = Integer.parseInt(etStop.getText().toString());
            tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop broadcasting", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
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
    private boolean mIsDouble = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_always_start:
                mIsStart = true;
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_1, mIsDouble ? "twice" : "three times"));
                break;
            case R.id.rb_start_advertising:
                mIsStart = true;
                String startDuration = etStart.getText().toString();
                if (TextUtils.isEmpty(startDuration)) {
                    mDuration = 0;
                } else {
                    mDuration = Integer.parseInt(startDuration);
                }
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "start to broadcast", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
                break;
            case R.id.rb_stop_advertising:
                mIsStart = false;
                String stopDuration = etStop.getText().toString();
                if (TextUtils.isEmpty(stopDuration)) {
                    mDuration = 0;
                } else {
                    mDuration = Integer.parseInt(stopDuration);
                }
                tvTriggerTips.setText(getString(R.string.trigger_tapped_tips_2, "stop broadcasting", String.format("%ds", mDuration), mIsDouble ? "twice" : "three times"));
                break;
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
        if (rbStartAdvertising.isChecked()) {
            duration = etStart.getText().toString();
        } else if (rbStopAdvertising.isChecked()) {
            duration = etStop.getText().toString();
        } else {
            duration = "0";
        }
        if (TextUtils.isEmpty(duration)) {
            ToastUtils.showToast(getActivity(), "The advertising can not be empty.");
            return -1;
        }
        mDuration = Integer.parseInt(duration);
        if ((rbStartAdvertising.isChecked() || rbStopAdvertising.isChecked()) && (mDuration < 1 || mDuration > 65535)) {
            ToastUtils.showToast(activity, "The advertising range is 1~65535");
            return -1;
        }
        return mDuration;
    }

    public boolean isDouble() {
        return mIsDouble;
    }

    public void setIsDouble(boolean isDouble) {
        this.mIsDouble = isDouble;
    }
}
