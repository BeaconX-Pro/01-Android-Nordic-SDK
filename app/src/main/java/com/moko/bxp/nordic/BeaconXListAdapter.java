package com.moko.bxp.nordic;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.elvishew.xlog.XLog;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.entity.BeaconXAxis;
import com.moko.bxp.nordic.entity.BeaconXInfo;
import com.moko.bxp.nordic.entity.BeaconXTH;
import com.moko.bxp.nordic.entity.BeaconXTLM;
import com.moko.bxp.nordic.entity.BeaconXUID;
import com.moko.bxp.nordic.entity.BeaconXURL;
import com.moko.bxp.nordic.entity.BeaconXiBeacon;
import com.moko.bxp.nordic.utils.BeaconXParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BeaconXListAdapter extends BaseQuickAdapter<BeaconXInfo, BaseViewHolder> {
    public BeaconXListAdapter() {
        super(R.layout.list_item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, BeaconXInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, item.rssi + "");
//        helper.setText(R.id.tv_conn_state, item.connectState < 0 ? "N/A" : item.connectState == 0 ? "Unconnectable" : "Connectable");
//        helper.setText(R.id.tv_lock_state, item.lockState < 0 ? "Lock State:N/A" : String.format("Lock State:0x%02x", item.lockState));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : String.format("%dmV", item.battery));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState > 0);
        LinearLayout parent = helper.getView(R.id.ll_data);
        parent.removeAllViews();
        ArrayList<BeaconXInfo.ValidData> validDatas = new ArrayList<>(item.validDataHashMap.values());
        Collections.sort(validDatas, new Comparator<BeaconXInfo.ValidData>() {
            @Override
            public int compare(BeaconXInfo.ValidData lhs, BeaconXInfo.ValidData rhs) {
                if (lhs.type > rhs.type) {
                    return 1;
                } else if (lhs.type < rhs.type) {
                    return -1;
                }
                return 0;
            }
        });

        for (BeaconXInfo.ValidData validData : validDatas) {
            XLog.i(validData.toString());
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_UID) {
                parent.addView(createUIDView(BeaconXParser.getUID(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_URL) {
                parent.addView(createURLView(BeaconXParser.getURL(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
                parent.addView(createTLMView(BeaconXParser.getTLM(validData.data)));
                if (validData.data.length() > 7) {
                    int battery = Integer.parseInt(validData.data.substring(4, 8), 16);
                    helper.setText(R.id.tv_battery, String.format("%dmV", battery));
                }
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                BeaconXiBeacon beaconXiBeacon = BeaconXParser.getiBeacon(item.rssi, validData.data);
                beaconXiBeacon.txPower = validData.txPower + "";
                parent.addView(createiBeaconView(beaconXiBeacon));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TH) {
                BeaconXTH beaconXTH = BeaconXParser.getTH(validData.data);
                if (validData.data.length() > 17) {
                    int battery = Integer.parseInt(validData.data.substring(14, 18), 16);
                    helper.setText(R.id.tv_battery, String.format("%dmV", battery));
                }
                beaconXTH.txPower = validData.txPower + "";
                parent.addView(createTHView(beaconXTH));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS) {
                BeaconXAxis beaconXAxis = BeaconXParser.getAxis(validData.data);
                if (validData.data.length() > 27) {
                    int battery = Integer.parseInt(validData.data.substring(24, 28), 16);
                    helper.setText(R.id.tv_battery, String.format("%dmV", battery));
                }
                beaconXAxis.txPower = validData.txPower + "";
                parent.addView(createAxisView(beaconXAxis));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO) {
                helper.setText(R.id.tv_tx_power, String.format("Tx power:%ddBm", validData.txPower));
                int rangingData = Integer.parseInt(validData.data.substring(2, 4), 16);
                helper.setText(R.id.tv_ranging_data, String.format("Ranging data:%sdBm", String.valueOf((byte) rangingData)));
            }
        }
    }

    private View createUIDView(BeaconXUID uid) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_uid, null);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvNameSpace = view.findViewById(R.id.tv_namespace);
        TextView tvInstanceId = view.findViewById(R.id.tv_instance_id);
        tvRSSI0M.setText(String.format("%sdBm", uid.rangingData));
        tvNameSpace.setText("0x" + uid.namespace.toUpperCase());
        tvInstanceId.setText("0x" + uid.instanceId.toUpperCase());
        return view;
    }

    private View createURLView(final BeaconXURL url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_url, null);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvUrl = view.findViewById(R.id.tv_url);
        tvRSSI0M.setText(String.format("%sdBm", url.rangingData));
        tvUrl.setText(url.url);
        tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvUrl.getPaint().setAntiAlias(true);//抗锯齿
        tvUrl.setOnClickListener(v -> {
            Uri uri = Uri.parse(url.url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        });
        return view;
    }

    private View createTLMView(BeaconXTLM tlm) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_tlm, null);
        TextView tv_vbatt = view.findViewById(R.id.tv_vbatt);
        TextView tv_temp = view.findViewById(R.id.tv_temp);
        TextView tv_adv_cnt = view.findViewById(R.id.tv_adv_cnt);
        TextView tv_sec_cnt = view.findViewById(R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createiBeaconView(BeaconXiBeacon iBeacon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_ibeacon, null);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_rssi_1m = view.findViewById(R.id.tv_rssi_1m);
        TextView tv_uuid = view.findViewById(R.id.tv_uuid);
        TextView tv_major = view.findViewById(R.id.tv_major);
        TextView tv_minor = view.findViewById(R.id.tv_minor);
        TextView tv_proximity_state = view.findViewById(R.id.tv_proximity_state);

        tv_rssi_1m.setText(String.format("%sdBm", iBeacon.rangingData));
        tv_tx_power.setText(String.format("%sdBm", iBeacon.txPower));
        tv_proximity_state.setText(iBeacon.distanceDesc);
        tv_uuid.setText(iBeacon.uuid);
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }

    private View createTHView(BeaconXTH beaconXTH) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_th, null);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_rssi_0m = view.findViewById(R.id.tv_rssi_0m);
        TextView tv_temperature = view.findViewById(R.id.tv_temperature);
        TextView tv_humidity = view.findViewById(R.id.tv_humidity);

        tv_rssi_0m.setText(String.format("%sdBm", beaconXTH.rangingData));
        tv_tx_power.setText(String.format("%sdBm", beaconXTH.txPower));
        tv_temperature.setText(String.format("%s°C", beaconXTH.temperature));
        tv_humidity.setText(String.format("%s%%RH", beaconXTH.humidity));
        return view;
    }

    private View createAxisView(BeaconXAxis beaconXAxis) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_axis, null);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_rssi_0m = view.findViewById(R.id.tv_rssi_0m);
        TextView tv_data_rate = view.findViewById(R.id.tv_data_rate);
        TextView tv_scale = view.findViewById(R.id.tv_scale);
        TextView tv_sensitivity = view.findViewById(R.id.tv_sensitivity);
        TextView tv_sampled_data = view.findViewById(R.id.tv_sampled_data);

        tv_rssi_0m.setText(String.format("%sdBm", beaconXAxis.rangingData));
        tv_tx_power.setText(String.format("%sdBm", beaconXAxis.txPower));
        tv_data_rate.setText(beaconXAxis.dataRate);
        tv_scale.setText(beaconXAxis.scale);
        tv_sensitivity.setText(beaconXAxis.sensitivity);
        tv_sampled_data.setText(String.format("X:%smg Y:%smg Z:%smg", beaconXAxis.x_data, beaconXAxis.y_data, beaconXAxis.z_data));
        return view;
    }
}
