package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.activity.DeviceInfoActivity;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    @BindView(R2.id.rl_password)
    RelativeLayout rlPassword;
    @BindView(R2.id.rl_reset_factory)
    RelativeLayout rlResetFactory;
    @BindView(R2.id.rl_sensor_config)
    RelativeLayout rlSensorConfig;
    @BindView(R2.id.et_effective_click_interval)
    EditText etEffectiveClickInterval;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
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
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
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

    public void setModifyPasswordShown(boolean isSupportModifyPassword) {
        rlPassword.setVisibility(isSupportModifyPassword ? View.VISIBLE : View.GONE);
    }

    public void setResetShown(int enable) {
        rlResetFactory.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
    }

    public void setDeviceType(int deviceType) {
        switch (deviceType) {
            case 0:
                rlSensorConfig.setVisibility(View.GONE);
                break;
            case 1:
                rlSensorConfig.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void setEffectiveClickInterval(int interval) {
        etEffectiveClickInterval.setText(String.valueOf(interval / 100));
    }

    public boolean isValid() {
        String intervalStr = etEffectiveClickInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        int interval = Integer.parseInt(intervalStr);
        if (interval < 5 || interval > 15)
            return false;
        return true;
    }

    public void saveParams() {
        String intervalStr = etEffectiveClickInterval.getText().toString();
        int interval = Integer.parseInt(intervalStr) * 100;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setEffectiveClickInterval(interval));
        orderTasks.add(OrderTaskAssembler.getEffectiveClickInterval());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
