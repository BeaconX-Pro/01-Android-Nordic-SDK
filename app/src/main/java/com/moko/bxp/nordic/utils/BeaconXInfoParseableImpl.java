package com.moko.bxp.nordic.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.entity.BeaconXInfo;
import com.moko.support.nordic.entity.DeviceInfo;
import com.moko.support.nordic.service.DeviceInfoParseable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2019/5/22
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.bxp.nordic.utils.BeaconXInfoParseableImpl
 */
public class BeaconXInfoParseableImpl implements DeviceInfoParseable<BeaconXInfo> {
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap;

    public BeaconXInfoParseableImpl() {
        this.beaconXInfoHashMap = new HashMap<>();
    }

    @Override
    public BeaconXInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        int battery = -1;
//        int connectState = -1;
        int lockState = -1;
//        int ambientLightSupport = -1;
        int ambientLightState = -1;
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        // filter
        boolean isEddystone = false;
        boolean isBeaconXPro = false;
        boolean isBeacon = false;
        byte[] values = null;
        int type = -1;
        int needParseData = -1;
        byte[] manufacturerBytes = record.getManufacturerSpecificData(0x004C);
        if (null != manufacturerBytes && manufacturerBytes.length ==23) {
            isBeacon = true;
            type = BeaconXInfo.VALID_DATA_TYPE_IBEACON_APPLE;
            values = manufacturerBytes;
        }

        if (map != null && !map.isEmpty()) {
            Iterator iterator = map.keySet().iterator();
            if (iterator.hasNext()) {
                ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
                if (parcelUuid.toString().startsWith("0000feaa")) {
                    isEddystone = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_UID:
                                if (bytes.length != 20)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                                // 00ee0102030405060708090a0102030405060000
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_URL:
                                if (bytes.length < 4 || bytes.length > 20)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                                // 100c0141424344454609
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM:
                                if (bytes.length != 14)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                                // 20000d18158000017eb20002e754
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000feab")) {
                    isBeaconXPro = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO:
                                if (bytes.length != 15)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                                battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5));
                                lockState = bytes[5] & 2;// 0 or 2
                                int ambientLightSupport = bytes[5] & 4;// 0 or 4
                                if (ambientLightSupport == 4) {
                                    ambientLightState = bytes[6] & 2;
                                }
//                                connectState = bytes[6] & 0xff;
                                // 40000a0d0d0001ff02030405063001
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON:
                                if (bytes.length != 23)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                                // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS:
                                if (bytes.length != 12 && bytes.length != 21)
                                    return null;
                                if (bytes.length == 12) {
                                    needParseData = 0;
                                }
                                if (bytes.length == 21) {
                                    needParseData = 1;
                                    battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 12, 14));
                                }
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS;
                                // 60f60e010007f600d5002e00
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TH:
                                if (bytes.length != 7 && bytes.length != 16)
                                    return null;
                                if (bytes.length == 16) {
                                    battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 7, 9));
                                }
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TH;
                                // 700b1000fb02f5
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000feac")) {
                    isBeaconXPro = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO:
                                if (bytes.length != 15)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                                battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5));
                                lockState = bytes[5] & 0xff;
//                                connectState = bytes[6] & 0xff;
                                // 40000a0d0d0001ff02030405063001
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000eb01")) {
                    isBeaconXPro = true;
                    type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null && bytes.length == 8) {
                        battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 0, 2));
                    }
                    SparseArray<byte[]> manufacturer = result.getScanRecord().getManufacturerSpecificData();
                    if (manufacturer == null || manufacturer.size() == 0) {
                        return null;
                    }
                    String manufacturerSpecificData = MokoUtils.bytesToHexString(result.getScanRecord().getManufacturerSpecificData(manufacturer.keyAt(0)));
                    if (TextUtils.isEmpty(manufacturerSpecificData) || !manufacturerSpecificData.startsWith("0215")) {
                        return null;
                    }
                    String iBeaconBytesHex = "50" +
                            manufacturerSpecificData.substring(manufacturerSpecificData.length() - 2) +
                            "0A" + manufacturerSpecificData.substring(4, manufacturerSpecificData.length() - 2);
                    values = MokoUtils.hex2bytes(iBeaconBytesHex);
                }
            }
        }
        if ((!isEddystone && !isBeaconXPro && !isBeacon) || values == null || type == -1) {
            return null;
        }
        // avoid repeat
        BeaconXInfo beaconXInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconXInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            if (!TextUtils.isEmpty(deviceInfo.name)) {
                beaconXInfo.name = deviceInfo.name;
            }
            beaconXInfo.rssi = deviceInfo.rssi;
            if (battery >= 0) {
                beaconXInfo.battery = battery;
            }
            if (lockState >= 0) {
                beaconXInfo.lockState = lockState;
            }
            if (needParseData >= 0) {
                beaconXInfo.needParseData = needParseData;
            }
            if (ambientLightState >= 0) {
                beaconXInfo.ambientLightState = ambientLightState;
            }
            if (result.isConnectable())
                beaconXInfo.connectState = 1;
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - beaconXInfo.scanTime;
            beaconXInfo.intervalTime = intervalTime;
            beaconXInfo.scanTime = currentTime;
        } else {
            beaconXInfo = new BeaconXInfo();
            beaconXInfo.name = deviceInfo.name;
            beaconXInfo.mac = deviceInfo.mac;
            beaconXInfo.rssi = deviceInfo.rssi;
            if (battery < 0) {
                beaconXInfo.battery = -1;
            } else {
                beaconXInfo.battery = battery;
            }
            if (lockState < 0) {
                beaconXInfo.lockState = -1;
            } else {
                beaconXInfo.lockState = lockState;
            }
            if (ambientLightState < 0) {
                beaconXInfo.ambientLightState = -1;
            } else {
                beaconXInfo.ambientLightState = ambientLightState;
            }
            if (result.isConnectable()) {
                beaconXInfo.connectState = 1;
            } else {
                beaconXInfo.connectState = 0;
            }
            if (needParseData < 0) {
                beaconXInfo.needParseData = -1;
            } else {
                beaconXInfo.needParseData = needParseData;
            }
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            beaconXInfo.scanTime = SystemClock.elapsedRealtime();
            beaconXInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconXInfo);
        }
        String data = MokoUtils.bytesToHexString(values);
        BeaconXInfo.ValidData validData = new BeaconXInfo.ValidData();
        validData.data = data;
        validData.type = type;
        if (type != BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO) {
            validData.txPower = record.getTxPowerLevel();
        } else {
            beaconXInfo.txPower = record.getTxPowerLevel();
            beaconXInfo.rangingData = Integer.parseInt(data.substring(2, 4), 16);
        }
        if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
            beaconXInfo.validDataHashMap.put(type + "", validData);
            return beaconXInfo;
        }
        if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TH) {
            beaconXInfo.validDataHashMap.put(type + "", validData);
            return beaconXInfo;
        }
        if (type == BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS) {
            beaconXInfo.validDataHashMap.put(type + "", validData);
            return beaconXInfo;
        }
        beaconXInfo.validDataHashMap.put(data, validData);
        return beaconXInfo;
    }
}
