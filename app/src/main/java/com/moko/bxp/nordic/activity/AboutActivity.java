package com.moko.bxp.nordic.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.moko.bxp.nordic.BuildConfig;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.databinding.ActivityAboutNordicBinding;
import com.moko.bxp.nordic.utils.Utils;
import com.moko.lib.bxpui.utils.ToastUtils;

import java.io.File;
import java.util.Calendar;

public class AboutActivity extends BaseActivity {
    private ActivityAboutNordicBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAboutNordicBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        if (!BuildConfig.IS_LIBRARY) {
            mBind.appVersion.setText(String.format("APP Version:V%s", Utils.getVersionInfo(this)));
            mBind.tvFeedbackLog.setVisibility(View.VISIBLE);
        }
    }


    public void onBack(View view) {
        finish();
    }

    public void onCompanyWebsite(View view) {
        if (isWindowLocked())
            return;
        Uri uri = Uri.parse("https://" + getString(R.string.company_website));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onFeedback(View view) {
        if (isWindowLocked())
            return;
        File trackerLog = new File(NordicMainActivity.PATH_LOGCAT + File.separator + "BXP-NORDIC.txt");
        File trackerLogBak = new File(NordicMainActivity.PATH_LOGCAT + File.separator + "BXP-NORDIC.txt.bak");
        File trackerCrashLog = new File(NordicMainActivity.PATH_LOGCAT + File.separator + "crash_log.txt");
        if (!trackerLog.exists() || !trackerLog.canRead()) {
            ToastUtils.showToast(this, "File is not exists!");
            return;
        }
        String address = "Development@mokotechnology.com";
        StringBuilder mailContent = new StringBuilder("BXP-NORDIC_");
        Calendar calendar = Calendar.getInstance();
        String date = Utils.calendar2strDate(calendar, "yyyyMMdd");
        mailContent.append(date);
        String title = mailContent.toString();
        if ((!trackerLogBak.exists() || !trackerLogBak.canRead())
                && (!trackerCrashLog.exists() || !trackerCrashLog.canRead())) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog);
        } else if (!trackerCrashLog.exists() || !trackerCrashLog.canRead()) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak);
        } else if (!trackerLogBak.exists() || !trackerLogBak.canRead()) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerCrashLog);
        } else {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak, trackerCrashLog);
        }
    }
}
