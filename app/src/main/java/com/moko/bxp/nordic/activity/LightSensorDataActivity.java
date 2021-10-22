package com.moko.bxp.nordic.activity;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.moko.bxp.nordic.adapter.LightSensorDataListAdapter;
import com.moko.bxp.nordic.dialog.AlertMessageDialog;
import com.moko.bxp.nordic.dialog.LoadingMessageDialog;
import com.moko.bxp.nordic.utils.ToastUtils;
import com.moko.bxp.nordic.utils.Utils;
import com.moko.support.nordic.MokoSupport;
import com.moko.support.nordic.OrderTaskAssembler;
import com.moko.support.nordic.entity.LightSensorStoreData;
import com.moko.support.nordic.entity.OrderCHAR;
import com.moko.support.nordic.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LightSensorDataActivity extends BaseActivity {

    private static final String TRACKED_FILE = "light_sensor.txt";

    private static String PATH_LOGCAT;
    @BindView(R2.id.tv_light_sensor_status)
    TextView tvLightSensorStatus;
    @BindView(R2.id.tv_update_date)
    TextView tvUpdateDate;
    @BindView(R2.id.iv_sync)
    ImageView ivSync;
    @BindView(R2.id.tv_sync)
    TextView tvSync;
    @BindView(R2.id.tv_export)
    TextView tvExport;
    @BindView(R2.id.ll_data)
    LinearLayout llData;
    @BindView(R2.id.rv_light_data)
    RecyclerView rvLightData;


    private boolean mIsShown;
    private boolean isSync;
    private Handler mHandler;
    private StringBuilder lightSensorStoreString;
    private ArrayList<LightSensorStoreData> lightSensorStoreData;
    private LightSensorDataListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_sensor);
        ButterKnife.bind(this);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "BXP-NORDIC") + File.separator + TRACKED_FILE;
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "BXP-NORDIC") + File.separator + TRACKED_FILE;
        }

        mAdapter = new LightSensorDataListAdapter();
        lightSensorStoreData = MokoSupport.getInstance().lightSensorStoreData;
        lightSensorStoreString = MokoSupport.getInstance().lightSensorStoreString;
        if (lightSensorStoreData != null && lightSensorStoreData.size() > 0 && lightSensorStoreString != null) {
            tvExport.setEnabled(true);
            if (!mIsShown) {
                mIsShown = true;
                Drawable top = getResources().getDrawable(R.drawable.ic_download_checked);
                tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            }
            llData.setVisibility(View.VISIBLE);
        } else {
            lightSensorStoreData = new ArrayList<>();
            lightSensorStoreString = new StringBuilder();
        }
        mAdapter.replaceData(lightSensorStoreData);
        rvLightData.setLayoutManager(new LinearLayoutManager(this));
        rvLightData.setAdapter(mAdapter);

        mHandler = new Handler();
        EventBus.getDefault().register(this);
        MokoSupport.getInstance().enableLightSensorCurrentNotify();
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getLightSensorCurrent());
        orderTasks.add(OrderTaskAssembler.getStorageCondition());
        orderTasks.add(OrderTaskAssembler.getDeviceTime());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
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
                    case CHAR_LIGHT_SENSOR_CURRENT:
                        if (value.length == 1) {
                            int status = MokoUtils.toInt(value);
                            tvLightSensorStatus.setText(status == 1 ? "Ambient light detected" : "Ambient light NOT detected");
                        }
                        break;
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case GET_DEVICE_TIME:
                                    if (value.length > 9) {
                                        int year = value[4] & 0xff;
                                        int month = value[5] & 0xff;
                                        int day = value[6] & 0xff;
                                        int hour = value[7] & 0xff;
                                        int minute = value[8] & 0xff;
                                        int second = value[9] & 0xff;
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(Calendar.YEAR, 2000 + year);
                                        calendar.set(Calendar.MONTH, month - 1);
                                        calendar.set(Calendar.DAY_OF_MONTH, day);
                                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                                        calendar.set(Calendar.MINUTE, minute);
                                        calendar.set(Calendar.SECOND, second);
                                        tvUpdateDate.setText(Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS));
                                    }
                                    break;
                                case SET_DEVICE_TIME:
                                    if (value.length > 3 && value[3] == 0) {
                                        ToastUtils.showToast(LightSensorDataActivity.this, "Success");
                                    } else {
                                        ToastUtils.showToast(LightSensorDataActivity.this, "Failed");
                                    }
                                    break;
                                case SET_LIGHT_SENSOR_EMPTY:
                                    if (value.length > 3 && value[3] == 0) {
                                        lightSensorStoreString = new StringBuilder();
                                        writeLightSensorFile("");
                                        mIsShown = false;
                                        lightSensorStoreData.clear();
                                        mAdapter.replaceData(lightSensorStoreData);
                                        llData.setVisibility(View.GONE);
                                        Drawable top = getResources().getDrawable(R.drawable.ic_download);
                                        tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                        ToastUtils.showToast(this, "Erase success!");
                                    } else {
                                        ToastUtils.showToast(this, "Failed");
                                    }
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
                            ToastUtils.showToast(LightSensorDataActivity.this, "Device Locked!");
                            back();
                        }
                        break;
                    case CHAR_LIGHT_SENSOR_CURRENT:
                        if (value.length == 1) {
                            int status = MokoUtils.toInt(value);
                            tvLightSensorStatus.setText(status == 1 ? "Ambient light detected" : "Ambient light NOT detected");
                        }
                        break;
                    case CHAR_LIGHT_SENSOR_NOTIFY:
                        if (!mIsShown) {
                            mIsShown = true;
                            llData.setVisibility(View.VISIBLE);
                            Drawable top = getResources().getDrawable(R.drawable.ic_download_checked);
                            tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                        }
                        if (value.length > 6) {
                            int year = value[0] & 0xff;
                            int month = value[1] & 0xff;
                            int day = value[2] & 0xff;
                            int hour = value[3] & 0xff;
                            int minute = value[4] & 0xff;
                            int second = value[5] & 0xff;
                            int status = value[6] & 0xff;
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, 2000 + year);
                            calendar.set(Calendar.MONTH, month - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, second);
                            String time = Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                            String statusStr = status == 1 ? "Ambient light detected" : "Ambient light NOT detected";
                            tvLightSensorStatus.setText(statusStr);
                            LightSensorStoreData data = new LightSensorStoreData();
                            data.time = time;
                            data.status = statusStr;
                            lightSensorStoreData.add(data);
                            mAdapter.replaceData(lightSensorStoreData);
                            lightSensorStoreString.append(String.format("%s %s", time, statusStr));
                            lightSensorStoreString.append("\n");
                        }
                        if (mHandler.hasMessages(0))
                            mHandler.removeMessages(0);
                        mHandler.postDelayed(() -> {
                            XLog.i("Timeout");
                            MokoSupport.getInstance().disableLightSensorNotify();
                            isSync = false;
                            ivSync.clearAnimation();
                            tvSync.setText("Sync");
                        }, 10 * 1000);
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
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
        MokoSupport.getInstance().disableLightSensorCurrentNotify();
        MokoSupport.getInstance().disableLightSensorNotify();
        MokoSupport.getInstance().lightSensorStoreData = lightSensorStoreData;
        MokoSupport.getInstance().lightSensorStoreString = lightSensorStoreString;
        finish();

    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onUpdate(View view) {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        Calendar calendar = Calendar.getInstance();
        ArrayList<OrderTask> tasks = new ArrayList<>();
        tasks.add(OrderTaskAssembler.setDeviceTime(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)));
        tasks.add(OrderTaskAssembler.getDeviceTime());
        MokoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
    }

    public void onBack(View view) {
        back();
    }

    public void onSync(View view) {
        if (isWindowLocked())
            return;
        if (!isSync) {
            isSync = true;
            MokoSupport.getInstance().enableLightSensorNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            ivSync.startAnimation(animation);
            tvSync.setText("Stop");
        } else {
            if (mHandler.hasMessages(0))
                mHandler.removeMessages(0);
            MokoSupport.getInstance().disableLightSensorNotify();
            isSync = false;
            ivSync.clearAnimation();
            tvSync.setText("Sync");
        }
    }

    public void onEmpty(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning!");
        dialog.setMessage("Are you sure to erase all the saved light sensor status data?");
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setLightSensorEmpty());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onExport(View view) {
        if (isWindowLocked())
            return;
        if (mIsShown) {
            showSyncingProgressDialog();
            writeLightSensorFile("");
            tvExport.postDelayed(() -> {
                dismissSyncProgressDialog();
                String log = lightSensorStoreString.toString();
                if (!TextUtils.isEmpty(log)) {
                    writeLightSensorFile(log);
                    File file = getLightSensorFile();
                    // 发送邮件
                    String address = "Development@mokotechnology.com";
                    String title = "Light Sensor Log";
                    String content = title;
                    Utils.sendEmail(this, address, content, title, "Choose Email Client", file);
                }
            }, 500);
        }
    }


    public static void writeLightSensorFile(String thLog) {
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

    public static File getLightSensorFile() {
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
}
