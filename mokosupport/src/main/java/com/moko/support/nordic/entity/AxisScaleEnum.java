package com.moko.support.nordic.entity;


import java.io.Serializable;

public enum AxisScaleEnum implements Serializable {
    SCALE_2(0, "±2g"),
    SCALE_4(1, "±4g"),
    SCALE_8(2, "±8g"),
    SCALE_16(3, "±16g"),
    ;
    private String scale;
    private int index;

    AxisScaleEnum(int index, String scale) {
        this.index = index;
        this.scale = scale;
    }

    public String getScale() {
        return scale;
    }

    public int getSlot() {
        return index;
    }

    public static AxisScaleEnum fromEnumOrdinal(int ordinal) {
        for (AxisScaleEnum axisScaleEnum : AxisScaleEnum.values()) {
            if (axisScaleEnum.ordinal() == ordinal) {
                return axisScaleEnum;
            }
        }
        return null;
    }
}
