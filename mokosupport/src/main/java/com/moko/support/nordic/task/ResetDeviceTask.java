package com.moko.support.nordic.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.nordic.entity.OrderCHAR;


public class ResetDeviceTask extends OrderTask {

    public byte[] data = new byte[]{0x0b};

    public ResetDeviceTask() {
        super(OrderCHAR.CHAR_RESET_DEVICE, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
