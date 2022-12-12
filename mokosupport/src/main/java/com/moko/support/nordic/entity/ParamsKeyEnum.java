package com.moko.support.nordic.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    GET_SLOT_TYPE(0x61),
    GET_DEVICE_MAC(0x20),
    GET_AXIS_PARAMS(0x21),
    SET_AXIS_PARAMS(0x31),
    GET_STORAGE_CONDITION(0x22),
    SET_STORAGE_CONDITION(0x32),
    GET_TH_PERIOD(0x23),
    SET_TH_PERIOD(0x33),
    SET_TH_EMPTY(0x24),
    GET_DEVICE_TIME(0x25),
    SET_DEVICE_TIME(0x35),
    GET_BUTTON_POWER(0x28),
    SET_BUTTON_POWER(0x38),
    GET_TRIGGER_DATA(0x29),
    SET_TRIGGER_DATA(0x39),
    GET_IBEACON_UUID(0x64),
    SET_IBEACON_UUID(0x65),
    GET_IBEACON_INFO(0x66),
    SET_IBEACON_INFO(0x67),
    GET_CONNECTABLE(0x90),
    SET_CONNECTABLE(0x89),
    SET_CLOSE(0x26),
    GET_HW_RESET_ENABLE(0x48),
    SET_HW_RESET_ENABLE(0x58),
    SET_LIGHT_SENSOR_EMPTY(0x46),
    GET_TRIGGER_LED_NOTIFICATION(0x47),
    SET_TRIGGER_LED_NOTIFICATION(0x57),
    GET_EFFECTIVE_CLICK_INTERVAL(0x4D),
    SET_EFFECTIVE_CLICK_INTERVAL(0x5D),
    SET_ERROR(0x0D),
    ;

    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
