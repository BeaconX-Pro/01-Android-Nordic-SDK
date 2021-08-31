package com.moko.support.nordic.entity;


import java.io.Serializable;

public enum AxisRateEnum implements Serializable {
    RATE_1HZ(0, "1Hz"),
    RATE_10HZ(1, "10Hz"),
    RATE_25HZ(2, "25Hz"),
    RATE_50HZ(3, "50Hz"),
    RATE_100HZ(4, "100Hz"),
    RATE_200HZ(5, "200Hz"),
    RATE_400HZ(6, "400Hz"),
    RATE_1344HZ(7, "1344Hz"),
    RATE_1620HZ(8, "1620Hz"),
    RATE_5376HZ(9, "5376Hz"),
    ;
    private String rate;
    private int index;

    AxisRateEnum(int index, String rate) {
        this.index = index;
        this.rate = rate;
    }

    public String getRate() {
        return rate;
    }

    public int getSlot() {
        return index;
    }

    public static AxisRateEnum fromEnumOrdinal(int ordinal) {
        for (AxisRateEnum axisScaleEnum : AxisRateEnum.values()) {
            if (axisScaleEnum.ordinal() == ordinal) {
                return axisScaleEnum;
            }
        }
        return null;
    }
}
