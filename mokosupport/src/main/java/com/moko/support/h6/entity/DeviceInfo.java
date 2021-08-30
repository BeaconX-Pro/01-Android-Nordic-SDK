package com.moko.support.h6.entity;

import java.io.Serializable;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DeviceInfo implements Serializable {
    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public ScanResult scanResult;

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", mac='" + mac + '\'' +
                ", scanRecord='" + scanRecord + '\'' +
                '}';
    }
}
