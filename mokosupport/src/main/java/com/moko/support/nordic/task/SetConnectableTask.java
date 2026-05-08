package com.moko.support.nordic.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.nordic.entity.OrderCHAR;

public class SetConnectableTask extends OrderTask {

    public byte[] data;

    public SetConnectableTask(String address) {
        super(OrderCHAR.CHAR_CONNECTABLE, OrderTask.RESPONSE_TYPE_WRITE, address);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
