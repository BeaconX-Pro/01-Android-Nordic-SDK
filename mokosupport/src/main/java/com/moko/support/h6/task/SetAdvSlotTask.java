package com.moko.support.h6.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.h6.entity.OrderCHAR;
import com.moko.support.h6.entity.SlotEnum;

public class SetAdvSlotTask extends OrderTask {

    public byte[] data;

    public SetAdvSlotTask() {
        super(OrderCHAR.CHAR_ADV_SLOT, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(SlotEnum slot) {
        this.data = new byte[]{(byte) slot.getSlot()};
    }
}
