package com.moko.support.nordic.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.OrderServices;

import java.util.HashMap;

public class MokoCharacteristicHandler {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    public MokoCharacteristicHandler() {
        //no instance
        mCharacteristicMap = new HashMap<>();
    }

    public HashMap<OrderCHAR, BluetoothGattCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mCharacteristicMap != null && !mCharacteristicMap.isEmpty()) {
            mCharacteristicMap.clear();
        }
        if (gatt.getService(OrderServices.SERVICE_DEVICE_INFO.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_DEVICE_INFO.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_MODEL_NUMBER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MODEL_NUMBER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MODEL_NUMBER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SERIAL_NUMBER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SERIAL_NUMBER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SERIAL_NUMBER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_FIRMWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_FIRMWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_FIRMWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_HARDWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_HARDWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_HARDWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SOFTWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SOFTWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SOFTWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MANUFACTURER_NAME.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MANUFACTURER_NAME.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MANUFACTURER_NAME, characteristic);
            }
        }
        if (gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_LOCKED_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_LOCKED_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_LOCKED_NOTIFY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_THREE_AXIS_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_THREE_AXIS_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_THREE_AXIS_NOTIFY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_TH_NOTIFY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_STORE_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_STORE_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_STORE_NOTIFY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_DEVICE_TYPE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_DEVICE_TYPE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_DEVICE_TYPE, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SLOT_TYPE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SLOT_TYPE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SLOT_TYPE, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_BATTERY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_BATTERY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_BATTERY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_DISCONNECT, characteristic);
            }
        }
        if (gatt.getService(OrderServices.SERVICE_EDDYSTONE.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_EDDYSTONE.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_SLOT.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_SLOT.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_SLOT, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_INTERVAL.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_INTERVAL.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_INTERVAL, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_RADIO_TX_POWER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_RADIO_TX_POWER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_RADIO_TX_POWER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_TX_POWER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_TX_POWER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_TX_POWER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_LOCK_STATE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_LOCK_STATE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_LOCK_STATE, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_UNLOCK.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_UNLOCK.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_UNLOCK, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_SLOT_DATA.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_SLOT_DATA.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_SLOT_DATA, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_RESET_DEVICE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_RESET_DEVICE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_RESET_DEVICE, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_CONNECTABLE.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_CONNECTABLE.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_CONNECTABLE, characteristic);
            }
        }
        return mCharacteristicMap;
    }
}
