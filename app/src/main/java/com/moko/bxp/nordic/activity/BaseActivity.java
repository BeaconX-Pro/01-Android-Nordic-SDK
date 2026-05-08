package com.moko.bxp.nordic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.elvishew.xlog.XLog;

import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;


public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Intent intent = new Intent(this, GuideActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        // 设置全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            // 透明导航栏
            getWindow().setNavigationBarColor(Color.TRANSPARENT);

            // Android P及以上支持刘海屏
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);

            // 设置WindowInsets监听
            getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                private int lastOrientation = -1;

                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    DisplayCutout cutout = insets.getDisplayCutout();
                    if (cutout != null) {
                        // 获取当前方向
                        int currentOrientation = getResources().getConfiguration().orientation;

                        // 只有当方向改变时才重新设置padding
                        if (currentOrientation != lastOrientation) {
                            lastOrientation = currentOrientation;

                            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                                // 横屏：只考虑左右安全区域
                                v.setPadding(cutout.getSafeInsetLeft(), 0,
                                        cutout.getSafeInsetRight(), 0);
                            } else {
                                int bottomInset = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                                }
                                // 竖屏：使用全部安全区域
                                v.setPadding(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(),
                                        cutout.getSafeInsetRight(), cutout.getSafeInsetBottom() + bottomInset);
                            }

                            // 请求重新布局
                            v.requestLayout();
                        }
                    } else {
                        int bottomInset = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                        }
                        // 没有刘海屏时重置padding
                        v.setPadding(0, 0, 0, bottomInset);
                        lastOrientation = -1;
                    }

                    return insets;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        XLog.i("onConfigurationChanged...");
        finish();
    }


    // 记录上次页面控件点击时间,屏蔽无效点击事件
    protected long mLastOnClickTime = 0;

    public boolean isWindowLocked() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > 500) {
            mLastOnClickTime = current;
            return false;
        } else {
            return true;
        }
    }

    public boolean isWriteStoragePermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationPermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
