package com.moko.support.h6.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.h6.entity.OrderCHAR;

public class GetBatteryTask extends OrderTask {

    public byte[] data;

    public GetBatteryTask() {
        super(OrderCHAR.CHAR_BATTERY, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
