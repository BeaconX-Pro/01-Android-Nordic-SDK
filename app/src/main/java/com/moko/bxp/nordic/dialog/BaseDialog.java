package com.moko.bxp.nordic.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;


import com.moko.bxp.nordic.R;

import androidx.viewbinding.ViewBinding;

/**
 * @Date 2017/12/11 0011
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.dialog.BaseDialog
 */
public abstract class BaseDialog<VM extends ViewBinding> extends Dialog {
    private boolean dismissEnable;
    private Animation animation;
    protected VM mBind;

    public BaseDialog(Context context) {
        super(context, R.style.BaseDialogTheme);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = getViewBind();
        setContentView(mBind.getRoot());
        onCreate();
        if (animation != null) {
            mBind.getRoot().setAnimation(animation);
        }
        if (dismissEnable) {
            mBind.getRoot().setOnClickListener(v -> dismiss());
        }
    }

    protected abstract VM getViewBind();

    protected void onCreate() {
    }

    @Override
    public void show() {
        super.show();
        //set the dialog fullscreen
        final Window window = getWindow();
        assert window != null;
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置窗口高度为包裹内容
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }

    protected void setAnimation(Animation animation) {
        this.animation = animation;
    }

    protected void setDismissEnable(boolean dismissEnable) {
        this.dismissEnable = dismissEnable;
    }
}
