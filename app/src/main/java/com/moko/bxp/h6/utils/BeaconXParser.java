package com.moko.bxp.h6.utils;

import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.h6.entity.BeaconXAxis;
import com.moko.bxp.h6.entity.BeaconXDevice;
import com.moko.bxp.h6.entity.BeaconXTH;
import com.moko.bxp.h6.entity.BeaconXTLM;
import com.moko.bxp.h6.entity.BeaconXUID;
import com.moko.bxp.h6.entity.BeaconXURL;
import com.moko.bxp.h6.entity.BeaconXiBeacon;
import com.moko.support.h6.entity.AxisRateEnum;
import com.moko.support.h6.entity.AxisScaleEnum;
import com.moko.support.h6.entity.UrlExpansionEnum;
import com.moko.support.h6.entity.UrlSchemeEnum;


public class BeaconXParser {

    public static BeaconXUID getUID(String data) {
        // 00ee0102030405060708090a0102030405060000
        BeaconXUID uid = new BeaconXUID();
        int rssi_0m = Integer.parseInt(data.substring(2, 4), 16);
        uid.rangingData = (byte) rssi_0m + "";
        uid.namespace = data.substring(4, 24).toUpperCase();
        uid.instanceId = data.substring(24, 36).toUpperCase();
        return uid;
    }

    public static BeaconXURL getURL(String data) {
        // 100c0141424344454609
        BeaconXURL url = new BeaconXURL();
        int rssi_0m = Integer.parseInt(data.substring(2, 4), 16);
        url.rangingData = (byte) rssi_0m + "";

        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.parseInt(data.substring(4, 6), 16));
        String urlSchemeStr = "";
        if (urlSchemeEnum != null) {
            urlSchemeStr = urlSchemeEnum.getUrlDesc();
        }
        String urlExpansionStr = "";
        UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanType(Integer.parseInt(data.substring(data.length() - 2), 16));
        if (urlExpansionEnum != null) {
            urlExpansionStr = urlExpansionEnum.getUrlExpanDesc();
        }
        String urlStr;
        if (TextUtils.isEmpty(urlExpansionStr)) {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6));
        } else {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6, data.length() - 2)) + urlExpansionStr;
        }
        url.url = urlStr;
        return url;
    }

    public static BeaconXTLM getTLM(String data) {
        // 20000d18158000017eb20002e754
        BeaconXTLM tlm = new BeaconXTLM();
        tlm.vbatt = Integer.parseInt(data.substring(4, 8), 16) + "";
        int temp1 = Integer.parseInt(data.substring(8, 10), 16);
        int temp2 = Integer.parseInt(data.substring(10, 12), 16);
        int tempInt = temp1 > 128 ? temp1 - 256 : temp1;
        float tempDecimal = temp2 / 256.0f;
        float temperature = tempInt + tempDecimal;
        String tempStr = MokoUtils.getDecimalFormat("0.#").format(temperature);
        tlm.temp = String.format("%s°C", tempStr);
        tlm.adv_cnt = Long.parseLong(data.substring(12, 20), 16) + "";
        long seconds = Long.parseLong(data.substring(20, 28), 16) / 10;
        int day = 0, hours = 0, minutes = 0;
        day = (int) (seconds / (60 * 60 * 24));
        seconds -= day * 60 * 60 * 24;
        hours = (int) (seconds / (60 * 60));
        seconds -= hours * 60 * 60;
        minutes = (int) (seconds / 60);
        seconds -= minutes * 60;
        tlm.sec_cnt = String.format("%dd%dh%dm%ds", day, hours, minutes, seconds);
        return tlm;
    }

    public static BeaconXiBeacon getiBeacon(int rssi, String data) {
        // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
        BeaconXiBeacon iBeacon = new BeaconXiBeacon();
        int rssi_1m = Integer.parseInt(data.substring(2, 4), 16);
        iBeacon.rangingData = (byte) rssi_1m + "";
        StringBuilder stringBuilder = new StringBuilder(data.substring(6, 38).toLowerCase());
        stringBuilder.insert(8, "-");
        stringBuilder.insert(13, "-");
        stringBuilder.insert(18, "-");
        stringBuilder.insert(23, "-");
        iBeacon.uuid = stringBuilder.toString();
        iBeacon.major = Integer.parseInt(data.substring(38, 42), 16) + "";
        iBeacon.minor = Integer.parseInt(data.substring(42, 46), 16) + "";
        double distance = MokoUtils.getDistance(rssi, Math.abs((byte) rssi_1m));
        String distanceDesc = "Unknown";
        if (distance <= 0.1) {
            distanceDesc = "Immediate";
        } else if (distance > 0.1 && distance <= 1.0) {
            distanceDesc = "Near";
        } else if (distance > 1.0) {
            distanceDesc = "Far";
        }
        iBeacon.distanceDesc = distanceDesc;
        return iBeacon;
    }

    public static BeaconXTH getTH(String data) {
        // 700b1000fb02f5
        BeaconXTH beaconXTH = new BeaconXTH();
        int rssi_0m = Integer.parseInt(data.substring(2, 4), 16);
        beaconXTH.rangingData = (byte) rssi_0m + "";
        beaconXTH.temperature = MokoUtils.getDecimalFormat("0.0").format(((short) Integer.parseInt(data.substring(6, 10), 16)) * 0.1f);
        beaconXTH.humidity = MokoUtils.getDecimalFormat("0.0").format(Integer.parseInt(data.substring(10, 14), 16) * 0.1f);
        return beaconXTH;
    }

    public static BeaconXAxis getAxis(String data) {
        // 60f60e010007f600d5002e00
        BeaconXAxis beaconXAxis = new BeaconXAxis();
        int rssi_0m = Integer.parseInt(data.substring(2, 4), 16);
        beaconXAxis.rangingData = (byte) rssi_0m + "";
        beaconXAxis.dataRate = AxisRateEnum.fromEnumOrdinal(Integer.parseInt(data.substring(6, 8), 16)).getRate();
        int scaleIndex = Integer.parseInt(data.substring(8, 10), 16);
        beaconXAxis.scale = AxisScaleEnum.fromEnumOrdinal(scaleIndex).getScale();
        beaconXAxis.sensitivity = MokoUtils.getDecimalFormat("#.#g").format(Integer.parseInt(data.substring(10, 12), 16) * 0.1);
        double scale = Math.pow(2, scaleIndex + 1);
        int x = Integer.parseInt(data.substring(12, 16), 16);
        short x_short = (short) (x >> 4);
        double x_d = x_short * (scale / 2048);
        int y = Integer.parseInt(data.substring(16, 20), 16);
        short y_short = (short) (y >> 4);
        double y_d = y_short * (scale / 2048);
        int z = Integer.parseInt(data.substring(20, 24), 16);
        short z_short = (short) (z >> 4);
        double z_d = z_short * (scale / 2048);
        beaconXAxis.x_data = String.valueOf(Math.round(x_d * 1000));
        beaconXAxis.y_data = String.valueOf(Math.round(y_d * 1000));
        beaconXAxis.z_data = String.valueOf(Math.round(z_d * 1000));
        return beaconXAxis;
    }


    public static BeaconXDevice getDevice(String data) {
        // 40000a0d0d0001ff02030405063001
        BeaconXDevice device = new BeaconXDevice();
        return device;
    }
}
