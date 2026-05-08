package com.moko.support.nordic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleLib;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.support.nordic.entity.LightSensorStoreData;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.THStoreData;
import com.moko.support.nordic.handler.MokoCharacteristicHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MokoSupport extends MokoBleLib {
    private Map<String, Map<OrderCHAR, BluetoothGattCharacteristic>> mCharacteristicMap = new LinkedHashMap<>();

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoBleConfig mBleConfig;

    private MokoSupport() {
        //no instance
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        super.init(context);
    }


    @Override
    public MokoBleManager getMokoBleManager() {
        mBleConfig = new MokoBleConfig(mContext, this);
        return mBleConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    // connect
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        if (mCharacteristicMap.get(gatt.getDevice().getAddress()) == null) {
            mCharacteristicMap.put(gatt.getDevice().getAddress(), new MokoCharacteristicHandler().getCharacteristics(gatt));
        }
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        connectStatusEvent.setBluetoothDevice(gatt.getDevice());
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        mCharacteristicMap.remove(device.getAddress());
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCONNECTED);
        connectStatusEvent.setBluetoothDevice(device);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(String address, Enum orderCHAR) {
        return mCharacteristicMap.get(address).get(orderCHAR);
    }

    public ArrayList<String> getConnectedDeviceList() {
        return new ArrayList<>(mCharacteristicMap.keySet());
    }

    ///////////////////////////////////////////////////////////////////////////
    // order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCHARNull(String address) {
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            disConnectBle(address);
            return true;
        }
        return false;
    }

    @Override
    public void orderFinish() {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_FINISH);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderTimeout(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderResult(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public boolean orderResponseValid(BluetoothGattCharacteristic characteristic, OrderTask orderTask) {
        final UUID responseUUID = characteristic.getUuid();
        final OrderCHAR orderCHAR = (OrderCHAR) orderTask.orderCHAR;
        if (responseUUID.equals(OrderCHAR.CHAR_LOCKED_NOTIFY.getUuid()))
            return true;
        return responseUUID.equals(orderCHAR.getUuid());
    }


    @Override
    public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
        final UUID responseUUID = characteristic.getUuid();
        OrderCHAR orderCHAR = null;
        if (responseUUID.equals(OrderCHAR.CHAR_LOCKED_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_LOCKED_NOTIFY;
            int key = value[1] & 0xff;
            if (key != 0x63) {
                return false;
            }
        }
        if (responseUUID.equals(OrderCHAR.CHAR_TH_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_TH_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_STORE_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_STORE_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_THREE_AXIS_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_THREE_AXIS_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_DISCONNECT.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_DISCONNECT;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_LIGHT_SENSOR_CURRENT;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_LIGHT_SENSOR_NOTIFY;
        }
        if (orderCHAR == null)
            return false;
        XLog.i(orderCHAR.name());
        OrderTaskResponse response = new OrderTaskResponse();
        response.orderCHAR = orderCHAR;
        response.responseValue = value;
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_CURRENT_DATA);
        event.setResponse(response);
        EventBus.getDefault().post(event);
        return true;
    }

    public void enableTHNotify() {
        if (mBleConfig != null)
            mBleConfig.enableTHNotify();
    }

    public void disableTHNotify() {
        if (mBleConfig != null)
            mBleConfig.disableTHNotify();
    }

    public void enableStoreNotify() {
        if (mBleConfig != null)
            mBleConfig.enableStoreNotify();
    }

    public void disableStoreNotify() {
        if (mBleConfig != null)
            mBleConfig.disableStoreNotify();
    }

    public void enableThreeAxisNotify() {
        if (mBleConfig != null)
            mBleConfig.enableThreeAxisNotify();
    }

    public void disableThreeAxisNotify() {
        if (mBleConfig != null)
            mBleConfig.disableThreeAxisNotify();
    }

    public void enableLightSensorNotify() {
        if (mBleConfig != null)
            mBleConfig.enableLightSensorNotify();
    }

    public void disableLightSensorNotify() {
        if (mBleConfig != null)
            mBleConfig.disableLightSensorNotify();
    }

    public void enableLightSensorCurrentNotify() {
        if (mBleConfig != null)
            mBleConfig.enableLightSensorCurrentNotify();
    }

    public void disableLightSensorCurrentNotify() {
        if (mBleConfig != null)
            mBleConfig.disableLightSensorCurrentNotify();
    }

    public HashMap<String, Boolean> isNewVersion = new HashMap<>();
    public HashMap<String, ArrayList<THStoreData>> thStoreData = new HashMap<>();
    public HashMap<String, StringBuilder> thStoreString = new HashMap<>();
    public HashMap<String, ArrayList<LightSensorStoreData>> lightSensorStoreData = new HashMap<>();
    public HashMap<String, StringBuilder> lightSensorStoreString = new HashMap<>();

}
