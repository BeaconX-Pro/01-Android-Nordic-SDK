package com.moko.support.nordic.service;

import com.moko.support.nordic.entity.DeviceInfo;

/**
 * @Date 2018/1/11
 * @Author wenzheng.liu
 * @Description 设备解析接口
 * @ClassPath com.moko.support.nordic.service.DeviceInfoParseable
 */
public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
