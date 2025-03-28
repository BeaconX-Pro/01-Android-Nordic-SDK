package com.moko.bxp.nordic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.databinding.ActivitySensorConfigBinding;


public class SensorConfigActivity extends BaseActivity {

    private ActivitySensorConfigBinding mBind;

    private int mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySensorConfigBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mDeviceType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0);
        mBind.rlAccelerationSensor.setVisibility((mDeviceType & 1) == 1 ? View.VISIBLE : View.GONE);
        mBind.rlThSensor.setVisibility((mDeviceType & 2) == 2 ? View.VISIBLE : View.GONE);
        mBind.rlLightSensor.setVisibility((mDeviceType & 4) == 4 ? View.VISIBLE : View.GONE);
    }


    public void onBack(View view) {
        finish();
    }

    public void onAccelerationSensor(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, AxisDataActivity.class));
    }

    public void onTHSensor(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, THDataActivity.class));
    }

    public void onLightSensor(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, LightSensorDataActivity.class));
    }
}
