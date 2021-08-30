package com.moko.support.h6.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.h6.entity.OrderCHAR;

public class GetAdvSlotDataTask extends OrderTask {

    public byte[] data;

    public GetAdvSlotDataTask() {
        super(OrderCHAR.CHAR_ADV_SLOT_DATA, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
