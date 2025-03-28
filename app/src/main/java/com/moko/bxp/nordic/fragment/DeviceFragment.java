package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.databinding.FragmentDeviceBinding;

public class DeviceFragment extends Fragment {

    private static final String TAG = "DeviceFragment";
    private FragmentDeviceBinding mBind;


    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
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
        mBind = FragmentDeviceBinding.inflate(inflater, container, false);
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

    public void setDeviceMac(String macShow) {
        mBind.tvMacAddress.setText(macShow);
    }

    public void setManufacturer(byte[] value) {
        String manufacturer = new String(value).trim();
        mBind.tvManufacturer.setText(manufacturer);
    }

    public void setDeviceModel(byte[] value) {
        String deviceModel = new String(value).trim();
        mBind.tvDeviceModel.setText(deviceModel);
    }

    public void setProductDate(byte[] value) {
        String productDate = new String(value).trim();
        mBind.tvProductDate.setText(productDate);
    }

    public void setProductDateStr(String dateStr) {
        mBind.tvProductDate.setText(dateStr);
    }

    public void setHardwareVersion(byte[] value) {
        String hardwareVersion = new String(value).trim();
        mBind.tvHardwareVersion.setText(hardwareVersion);
    }

    public void setFirmwareVersion(byte[] value) {
        String firmwareVersion = new String(value).trim();
        mBind.tvFirmwareVersion.setText(firmwareVersion);
    }

    public void setSoftwareVersion(byte[] value) {
        String softwareVersion = new String(value).trim();
        mBind.tvSoftwareVersion.setText(softwareVersion);
    }

    public void setBattery(byte[] value) {
        String battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "mV";
        mBind.tvSoc.setText(battery);
    }
}
