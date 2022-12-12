package com.moko.bxp.nordic.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.AppConstants;
import com.moko.bxp.nordic.BuildConfig;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.adapter.THDataListAdapter;
import com.moko.bxp.nordic.dialog.AlertMessageDialog;
import com.moko.bxp.nordic.dialog.LoadingMessageDialog;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.bxp.nordic.utils.Utils;
import com.moko.bxp.nordic.view.THChartView;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.ParamsKeyEnum;
import com.moko.support.nordic.entity.THStoreData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ExportDataActivity extends BaseActivity {

    private static final String TRACKED_FILE = "T&HDatas.txt";

    private static String PATH_LOGCAT;
    @BindView(R2.id.iv_sync)
    ImageView ivSync;
    @BindView(R2.id.tv_export)
    TextView tvExport;
    @BindView(R2.id.tv_sync)
    TextView tvSync;
    @BindView(R2.id.cb_data_show)
    CheckBox cbDataShow;
    @BindView(R2.id.ll_th_chart_view)
    LinearLayout llTHChartView;
    @BindView(R2.id.temp_chart_view)
    THChartView tempChartView;
    @BindView(R2.id.humi_chart_view)
    THChartView humiChartView;
    @BindView(R2.id.th_chart_total)
    TextView thChartTotal;
    @BindView(R2.id.th_chart_display)
    TextView thChartDisplay;
    @BindView(R2.id.rv_th_data)
    RecyclerView rvThData;
    @BindView(R2.id.ll_th_data)
    LinearLayout llThData;

    private boolean mReceiverTag = false;
    private boolean mIsShown;
    private boolean isSync;
    private Handler mHandler;
    private List<Float> mHumiList;
    private List<Float> mTempList;
    private StringBuilder thStoreString;
    private ArrayList<THStoreData> thStoreData;
    private THDataListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
        ButterKnife.bind(this);

        PATH_LOGCAT = NordicMainActivity.PATH_LOGCAT + File.separator + TRACKED_FILE;

        mHumiList = new ArrayList<>();
        mTempList = new ArrayList<>();
        cbDataShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 绘制折线图并展示
                llThData.setVisibility(View.GONE);
                llTHChartView.setVisibility(View.VISIBLE);
                tempChartView.setxValue(mTempList);
                humiChartView.setxValue(mHumiList);
                int length = mTempList.size();
                thChartTotal.setText(getString(R.string.th_chart_total, length));
                thChartDisplay.setText(getString(R.string.th_chart_display, length > 1000 ? 1000 : length));
            } else {
                // 隐藏折线图
                llThData.setVisibility(View.VISIBLE);
                llTHChartView.setVisibility(View.GONE);
            }
        });
        mAdapter = new THDataListAdapter();
        thStoreData = MokoSupport.getInstance().thStoreData;
        thStoreString = MokoSupport.getInstance().thStoreString;
        if (thStoreData != null && thStoreData.size() > 0 && thStoreString != null) {
            tvExport.setEnabled(true);
            if (!mIsShown) {
                mIsShown = true;
                llThData.setVisibility(View.VISIBLE);
                Drawable top = getResources().getDrawable(R.drawable.ic_download_checked);
                tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            }
            for (THStoreData item : thStoreData) {
                float temp = Float.parseFloat(item.temp);
                float humidity = Float.parseFloat(item.humidity);
                mTempList.add(0, temp);
                mHumiList.add(0, humidity);
            }

        } else {
            thStoreData = new ArrayList<>();
            thStoreString = new StringBuilder();
        }
        mAdapter.replaceData(thStoreData);
        rvThData.setLayoutManager(new LinearLayoutManager(this));
        rvThData.setAdapter(mAdapter);

        mHandler = new Handler();
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            MokoSupport.getInstance().enableStoreNotify();
            Animation animation = AnimationUtils.loadAnimation(ExportDataActivity.this, R.anim.rotate_refresh);
            ivSync.startAnimation(animation);
            tvSync.setText("Stop");
            isSync = true;
            cbDataShow.setEnabled(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    finish();
                }
                if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                    // 设备连接成功，通知页面更新
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case SET_TH_EMPTY:
                                    if (value.length > 3 && value[3] == 0) {
                                        thStoreString = new StringBuilder();
                                        writeTHFile("");
                                        mIsShown = false;
                                        thStoreData.clear();
                                        mAdapter.replaceData(thStoreData);
                                        llThData.setVisibility(View.GONE);
                                        Drawable top = getResources().getDrawable(R.drawable.ic_download);
                                        tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                        ToastUtils.showToast(ExportDataActivity.this, "Erase success!");
                                    } else {
                                        ToastUtils.showToast(ExportDataActivity.this, "Failed");
                                    }
                                    break;
                                case SET_ERROR:
                                    if (isWindowLocked()) return;
                                    ToastUtils.showToast(this, "Failed");
                                    break;
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_LOCKED_NOTIFY:
                        String valueHexStr = MokoUtils.bytesToHexString(value);
                        if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                            ToastUtils.showToast(ExportDataActivity.this, "Device Locked!");
                            back();
                        }
                        break;
                    case CHAR_STORE_NOTIFY:
                        if (!mIsShown) {
                            mIsShown = true;
                            llThData.setVisibility(View.VISIBLE);
                            Drawable top = getResources().getDrawable(R.drawable.ic_download_checked);
                            tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                        }

                        if (value.length > 19) {
                            byte[] value1 = Arrays.copyOfRange(value, 0, 10);
                            byte[] value2 = Arrays.copyOfRange(value, 10, 20);
                            int year1 = value1[0] & 0xff;
                            int month1 = value1[1] & 0xff;
                            int day1 = value1[2] & 0xff;
                            int hour1 = value1[3] & 0xff;
                            int minute1 = value1[4] & 0xff;
                            int second1 = value1[5] & 0xff;
                            byte[] tempBytes1 = Arrays.copyOfRange(value1, 6, 8);
                            float temp1 = MokoUtils.byte2short(tempBytes1) * 0.1f;
                            byte[] humidityBytes1 = Arrays.copyOfRange(value1, 8, 10);
                            float humidity1 = MokoUtils.toInt(humidityBytes1) * 0.1f;
                            mTempList.add(temp1);
                            mHumiList.add(humidity1);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.set(Calendar.YEAR, 2000 + year1);
                            calendar1.set(Calendar.MONTH, month1 - 1);
                            calendar1.set(Calendar.DAY_OF_MONTH, day1);
                            calendar1.set(Calendar.HOUR_OF_DAY, hour1);
                            calendar1.set(Calendar.MINUTE, minute1);
                            calendar1.set(Calendar.SECOND, second1);
                            String time1 = Utils.calendar2strDate(calendar1, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                            String tempStr1 = MokoUtils.getDecimalFormat("0.0").format(temp1);
                            String humidityStr1 = MokoUtils.getDecimalFormat("0.0").format(humidity1);
                            THStoreData data1 = new THStoreData();
                            data1.time = time1;
                            data1.temp = tempStr1;
                            data1.humidity = humidityStr1;
                            thStoreData.add(0, data1);
                            thStoreString.append(String.format("%s T%s H%s", time1, tempStr1, humidityStr1));
                            thStoreString.append("\n");

                            int year2 = value2[0] & 0xff;
                            int month2 = value2[1] & 0xff;
                            int day2 = value2[2] & 0xff;
                            int hour2 = value2[3] & 0xff;
                            int minute2 = value2[4] & 0xff;
                            int second2 = value2[5] & 0xff;
                            byte[] tempBytes2 = Arrays.copyOfRange(value2, 6, 8);
                            float temp2 = MokoUtils.byte2short(tempBytes2) * 0.1f;
                            byte[] humidityBytes2 = Arrays.copyOfRange(value2, 8, 10);
                            float humidity2 = MokoUtils.toInt(humidityBytes2) * 0.1f;
                            mTempList.add(temp2);
                            mHumiList.add(humidity2);
                            Calendar calendar2 = Calendar.getInstance();
                            calendar2.set(Calendar.YEAR, 2000 + year2);
                            calendar2.set(Calendar.MONTH, month2 - 1);
                            calendar2.set(Calendar.DAY_OF_MONTH, day2);
                            calendar2.set(Calendar.HOUR_OF_DAY, hour2);
                            calendar2.set(Calendar.MINUTE, minute2);
                            calendar2.set(Calendar.SECOND, second2);
                            String time2 = Utils.calendar2strDate(calendar2, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                            String tempStr2 = MokoUtils.getDecimalFormat("0.0").format(temp2);
                            String humidityStr2 = MokoUtils.getDecimalFormat("0.0").format(humidity2);
                            THStoreData data2 = new THStoreData();
                            data2.time = time2;
                            data2.temp = tempStr2;
                            data2.humidity = humidityStr2;
                            thStoreData.add(0, data2);
                            mAdapter.replaceData(thStoreData);
                            thStoreString.append(String.format("%s T%s H%s", time2, tempStr2, humidityStr2));
                            thStoreString.append("\n");
                        } else if (value.length > 9) {
                            int year = value[0] & 0xff;
                            int month = value[1] & 0xff;
                            int day = value[2] & 0xff;
                            int hour = value[3] & 0xff;
                            int minute = value[4] & 0xff;
                            int second = value[5] & 0xff;
                            byte[] tempBytes = Arrays.copyOfRange(value, 6, 8);
                            float temp = MokoUtils.byte2short(tempBytes) * 0.1f;
                            byte[] humidityBytes = Arrays.copyOfRange(value, 8, 10);
                            float humidity = MokoUtils.toInt(humidityBytes) * 0.1f;
                            mTempList.add(temp);
                            mHumiList.add(humidity);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, 2000 + year);
                            calendar.set(Calendar.MONTH, month - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, second);
                            String time = Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                            String tempStr = MokoUtils.getDecimalFormat("0.0").format(temp);
                            String humidityStr = MokoUtils.getDecimalFormat("0.0").format(humidity);
                            THStoreData data = new THStoreData();
                            data.time = time;
                            data.temp = tempStr;
                            data.humidity = humidityStr;
                            thStoreData.add(0, data);
                            mAdapter.replaceData(thStoreData);
                            thStoreString.append(String.format("%s T%s H%s", time, tempStr, humidityStr));
                            thStoreString.append("\n");
                        }
                        if (mHandler.hasMessages(0))
                            mHandler.removeMessages(0);
                        mHandler.postDelayed(() -> {
                            XLog.i("Timeout");
                            MokoSupport.getInstance().disableStoreNotify();
                            isSync = false;
                            ivSync.clearAnimation();
                            tvSync.setText("Sync");
                            cbDataShow.setEnabled(true);
                        }, 10 * 1000);
                        break;
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTempList.clear();
        mTempList = null;
        mHumiList.clear();
        mHumiList = null;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().disableStoreNotify();
        MokoSupport.getInstance().thStoreData = thStoreData;
        MokoSupport.getInstance().thStoreString = thStoreString;
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public static void writeTHFile(String thLog) {
        File file = new File(PATH_LOGCAT);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(thLog);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getTHFile() {
        File file = new File(PATH_LOGCAT);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void onBack(View view) {
        back();
    }

    public void onSync(View view) {
        if (isWindowLocked())
            return;
        if (!isSync) {
            isSync = true;
            MokoSupport.getInstance().enableStoreNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            ivSync.startAnimation(animation);
            tvSync.setText("Stop");
            cbDataShow.setChecked(false);
            cbDataShow.setEnabled(false);
        } else {
            if (mHandler.hasMessages(0))
                mHandler.removeMessages(0);
            MokoSupport.getInstance().disableStoreNotify();
            isSync = false;
            ivSync.clearAnimation();
            tvSync.setText("Sync");
            cbDataShow.setEnabled(true);
        }
    }

    public void onExport(View view) {
        if (isWindowLocked())
            return;
        if (mIsShown) {
            showSyncingProgressDialog();
            writeTHFile("");
            tvExport.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissSyncProgressDialog();
                    String log = thStoreString.toString();
                    if (!TextUtils.isEmpty(log)) {
                        writeTHFile(log);
                        File file = getTHFile();
                        // 发送邮件
                        String address = "Development@mokotechnology.com";
                        String title = "T&H Log";
                        String content = title;
                        Utils.sendEmail(ExportDataActivity.this, address, content, title, "Choose Email Client", file);
                    }
                }
            }, 500);
        }
    }

    public void onEmpty(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning!");
        dialog.setMessage("Are you sure to erase all the saved T&H data?");
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                showSyncingProgressDialog();
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.setTHEmpty());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }
        });
        dialog.show(getSupportFragmentManager());
    }
}
