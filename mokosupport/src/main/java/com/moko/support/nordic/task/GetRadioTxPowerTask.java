package com.moko.support.nordic.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.nordic.entity.OrderCHAR;

public class GetRadioTxPowerTask extends OrderTask {

    public byte[] data;

    public GetRadioTxPowerTask() {
        super(OrderCHAR.CHAR_RADIO_TX_POWER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
