package com.moko.support.nordic.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.nordic.entity.OrderCHAR;

public class SetUnlockTask extends OrderTask {

    public byte[] data;

    public SetUnlockTask(String address) {
        super(OrderCHAR.CHAR_UNLOCK, OrderTask.RESPONSE_TYPE_WRITE, address);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
