package com.moko.support.nordic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.callback.MokoResponseCallback;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.OrderServices;

import androidx.annotation.NonNull;

final class MokoBleConfig extends MokoBleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private BluetoothGattCharacteristic thCharacteristic;
    private BluetoothGattCharacteristic lockedCharacteristic;
    private BluetoothGattCharacteristic threeAxisCharacteristic;
    private BluetoothGattCharacteristic storeCharacteristic;
    private BluetoothGattCharacteristic lightSensorNotifyCharacteristic;
    private BluetoothGattCharacteristic lightSensorCurrentCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;
    private BluetoothGatt gatt;

    public MokoBleConfig(@NonNull Context context, MokoResponseCallback callback) {
        super(context);
        mMokoResponseCallback = callback;
    }

    @Override
    public boolean checkServiceCharacteristicSupported(BluetoothGatt gatt) {
        final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
        if (service != null) {
            this.gatt = gatt;
            thCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid());
            lockedCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_LOCKED_NOTIFY.getUuid());
            threeAxisCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_THREE_AXIS_NOTIFY.getUuid());
            storeCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_STORE_NOTIFY.getUuid());
            disconnectCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
            lightSensorNotifyCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY.getUuid());
            lightSensorCurrentCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT.getUuid());
            return disconnectCharacteristic != null
                    && lockedCharacteristic != null;
        }
        return false;
    }

    @Override
    public void init() {
        requestMtu(247).with(((device, mtu) -> {
        })).then((device -> {
            enableDisconnectNotify();
            enableLockedNotify();
        })).enqueue();
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicWrite(characteristic, value);
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicRead(characteristic, value);
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
    }


    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    public void enableTHNotify() {
        setNotificationCallback(thCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(thCharacteristic, value);
        });
        enableNotifications(thCharacteristic).enqueue();
    }

    public void disableTHNotify() {
        disableNotifications(thCharacteristic).enqueue();
    }

    public void enableStoreNotify() {
        setNotificationCallback(storeCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(storeCharacteristic, value);
        });
        enableNotifications(storeCharacteristic).enqueue();
    }

    public void disableStoreNotify() {
        disableNotifications(storeCharacteristic).enqueue();
    }

    public void enableThreeAxisNotify() {
        setNotificationCallback(threeAxisCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(threeAxisCharacteristic, value);
        });
        enableNotifications(threeAxisCharacteristic).enqueue();
    }

    public void disableThreeAxisNotify() {
        disableNotifications(threeAxisCharacteristic).enqueue();
    }


    public void enableLockedNotify() {
        setNotificationCallback(lockedCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(lockedCharacteristic, value);
        });
        enableNotifications(lockedCharacteristic).done(device -> mMokoResponseCallback.onServicesDiscovered(gatt)).enqueue();
    }

    public void disableLockedNotify() {
        disableNotifications(lockedCharacteristic).enqueue();
    }

    public void enableDisconnectNotify() {
        setNotificationCallback(disconnectCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(disconnectCharacteristic, value);
        });
        enableNotifications(disconnectCharacteristic).enqueue();
    }

    public void disableDisconnectNotify() {
        disableNotifications(disconnectCharacteristic).enqueue();
    }

    public void enableLightSensorNotify() {
        if (lightSensorNotifyCharacteristic == null)
            return;
        setNotificationCallback(lightSensorNotifyCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(lightSensorNotifyCharacteristic, value);
        });
        enableNotifications(lightSensorNotifyCharacteristic).enqueue();
    }

    public void disableLightSensorNotify() {
        if (lightSensorNotifyCharacteristic == null)
            return;
        disableNotifications(lightSensorNotifyCharacteristic).enqueue();
    }

    public void enableLightSensorCurrentNotify() {
        if (lightSensorCurrentCharacteristic == null)
            return;
        setNotificationCallback(lightSensorCurrentCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(lightSensorCurrentCharacteristic, value);
        });
        enableNotifications(lightSensorCurrentCharacteristic).enqueue();
    }

    public void disableLightSensorCurrentNotify() {
        if (lightSensorCurrentCharacteristic == null)
            return;
        disableNotifications(lightSensorCurrentCharacteristic).enqueue();
    }
}