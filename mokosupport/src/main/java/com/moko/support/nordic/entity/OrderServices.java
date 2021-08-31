package com.moko.support.nordic.entity;

import java.util.UUID;

public enum OrderServices {
    SERVICE_DEVICE_INFO(UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")),
    SERVICE_CUSTOM(UUID.fromString("E62A0001-1362-4F28-9327-F5B74E970801")),
    SERVICE_EDDYSTONE(UUID.fromString("A3C87500-8ED3-4BDF-8A39-A01BEBEDE295")),
    ;
    private UUID uuid;

    OrderServices(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
