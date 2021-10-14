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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.activity.SlotDataActivity;
import com.moko.bxp.nordic.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TriggerLightDetectedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = TriggerLightDetectedFragment.class.getSimpleName();


    @BindView(R2.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @BindView(R2.id.rb_always_start)
    RadioButton rbAlwaysStart;
    @BindView(R2.id.rb_start_advertising)
    RadioButton rbStartAdvertising;
    @BindView(R2.id.rb_stop_advertising)
    RadioButton rbStopAdvertising;
    @BindView(R2.id.rg_light_detected)
    RadioGroup rgLightDetected;
    @BindView(R2.id.et_start)
    EditText etStart;
    @BindView(R2.id.et_stop)
    EditText etStop;


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
        View view = inflater.inflate(R.layout.fragment_trigger_light, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_1));
        if (mIsAlways) {
            rbAlwaysStart.setChecked(true);
        } else {
            if (mIsStart) {
                rbStartAdvertising.setChecked(true);
                etStart.setText(mDuration + "");
                etStart.setSelection((mDuration + "").length());
                tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
            } else {
                rbStopAdvertising.setChecked(true);
                etStop.setText(mDuration + "");
                etStop.setSelection((mDuration + "").length());
                tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
            }
        }
        rgLightDetected.setOnCheckedChangeListener(this);
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
                    tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
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
                    tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
                }
            }
        });
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

    private boolean mIsStart = true;
    private int mDuration = 30;
    private boolean mIsAlways = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_always_start) {
            mIsAlways = true;
            mIsStart = false;
            mDuration = 0;
            tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1));
        } else if (checkedId == R.id.rb_start_advertising) {
            mIsAlways = false;
            mIsStart = true;
            String startDuration = etStart.getText().toString();
            if (TextUtils.isEmpty(startDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(startDuration);
            }
            tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "start advertising", String.format("%ds", mDuration)));
        } else if (checkedId == R.id.rb_stop_advertising) {
            mIsAlways = false;
            mIsStart = false;
            String stopDuration = etStop.getText().toString();
            if (TextUtils.isEmpty(stopDuration)) {
                mDuration = 0;
            } else {
                mDuration = Integer.parseInt(stopDuration);
            }
            tvTriggerTips.setText(getString(R.string.trigger_light_detected_tips_2, "stop advertising", String.format("%ds", mDuration)));
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

    public void setAlwaysAdv(boolean isAlways) {
        mIsAlways = isAlways;
    }

    public boolean isAlways() {
        return mIsAlways;
    }
}
