package com.moko.support.h6.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.h6.entity.OrderCHAR;

public class SetRadioTxPowerTask extends OrderTask {

    public byte[] data;

    public SetRadioTxPowerTask() {
        super(OrderCHAR.CHAR_RADIO_TX_POWER, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
