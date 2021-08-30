package com.moko.support.h6.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.h6.entity.OrderCHAR;

public class GetLockStateTask extends OrderTask {

    public byte[] data;

    public GetLockStateTask() {
        super(OrderCHAR.CHAR_LOCK_STATE, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
