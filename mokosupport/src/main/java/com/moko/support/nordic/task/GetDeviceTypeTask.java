package com.moko.support.nordic.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.nordic.entity.OrderCHAR;

public class GetDeviceTypeTask extends OrderTask {

    public byte[] data;

    public GetDeviceTypeTask() {
        super(OrderCHAR.CHAR_DEVICE_TYPE, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
