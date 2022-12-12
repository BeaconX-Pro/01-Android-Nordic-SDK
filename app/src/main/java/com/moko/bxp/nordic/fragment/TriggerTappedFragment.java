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

public class TriggerTappedFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TappedFragment";


    @BindView(R2.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @BindView(R2.id.rb_always_start)
    RadioButton rbAlwaysStart;
    @BindView(R2.id.rb_start_advertising)
    RadioButton rbStartAdvertising;
    @BindView(R2.id.rb_stop_advertising)
    RadioButton rbStopAdvertising;
    @BindView(R2.id.rg_tapped)
    RadioGroup rgTapped;
    @BindView(R2.id.et_start)
    EditText etStart;
    @BindView(R2.id.et_stop)
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
                    String tips = "";
                    if (mTrapType == 0)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "single click button");
                    if (mTrapType == 1)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button twice");
                    if (mTrapType == 2)
                        tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button three times");
                    tvTriggerTips.setText(tips);
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
                    String tips = "";
                    if (mTrapType == 0)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "single click button");
                    if (mTrapType == 1)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button twice");
                    if (mTrapType == 2)
                        tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button three times");
                    tvTriggerTips.setText(tips);
                }
            }
        });
        return view;
    }

    public void updateTips() {
        if (rbAlwaysStart.isChecked()) {
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_1, "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_1, "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_1, "press the button three times");
            tvTriggerTips.setText(tips);
        } else if (rbStartAdvertising.isChecked()) {
            mDuration = Integer.parseInt(etStart.getText().toString());
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "start advertising", String.format("%ds", mDuration), "press the button three times");
            tvTriggerTips.setText(tips);
        } else {
            mDuration = Integer.parseInt(etStop.getText().toString());
            String tips = "";
            if (mTrapType == 0)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "single click button");
            if (mTrapType == 1)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button twice");
            if (mTrapType == 2)
                tips = getString(R.string.trigger_tapped_tips_2, "stop advertising", String.format("%ds", mDuration), "press the button three times");
            tvTriggerTips.setText(tips);
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
            tvTriggerTips.setText(tips);
        } else if (checkedId == R.id.rb_start_advertising) {
            mIsStart = true;
            String startDuration = etStart.getText().toString();
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
            tvTriggerTips.setText(tips);
        } else if (checkedId == R.id.rb_stop_advertising) {
            mIsStart = false;
            String stopDuration = etStop.getText().toString();
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
            tvTriggerTips.setText(tips);
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

    public int getTrapType() {
        return mTrapType;
    }

    public void setTrapType(int trapType) {
        this.mTrapType = trapType;
    }
}
