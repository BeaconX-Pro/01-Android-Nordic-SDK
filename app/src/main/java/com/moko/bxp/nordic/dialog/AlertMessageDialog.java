package com.moko.bxp.nordic.dialog;

import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.databinding.DialogAlertBinding;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class AlertMessageDialog extends MokoBaseDialog<DialogAlertBinding> {
    public static final String TAG = AlertMessageDialog.class.getSimpleName();
    private String cancel;
    private String confirm;
    private String title;
    private String message;
    private int cancelId = -1;
    private int confirmId = -1;
    private int titleId = -1;
    private int messageId = -1;
    private boolean cancelGone;
    private int messageTextColorId = -1;

    @Override
    protected DialogAlertBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogAlertBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        if (titleId > 0) {
            title = getString(titleId);
        }
        if (messageId > 0) {
            message = getString(messageId);
        }
        if (messageTextColorId > 0) {
            mBind.tvAlertMessage.setTextColor(ContextCompat.getColor(getContext(), messageTextColorId));
        }
        if (confirmId > 0) {
            confirm = getString(confirmId);
        }
        if (cancelId > 0) {
            cancel = getString(cancelId);
        }
        TextPaint tp = mBind.tvAlertTitle.getPaint();
        tp.setFakeBoldText(true);
        if (TextUtils.isEmpty(title)) {
            mBind.llAlertTitle.setVisibility(View.GONE);
        } else {
            mBind.tvAlertTitle.setText(title);
        }
        mBind.tvAlertMessage.setText(message);
        if (!TextUtils.isEmpty(cancel)) {
            mBind.tvAlertCancel.setText(cancel);
        }
        if (!TextUtils.isEmpty(confirm)) {
            mBind.tvAlertConfirm.setText(confirm);
        }
        if (cancelGone) {
            mBind.tvAlertCancel.setVisibility(View.GONE);
            mBind.viewDivider.setVisibility(View.GONE);
        }
        mBind.tvAlertCancel.setOnClickListener(v -> {
            dismiss();
            if (onAlertCancelListener != null)
                onAlertCancelListener.onClick();
        });
        mBind.tvAlertConfirm.setOnClickListener(v -> {
            dismiss();
            if (onAlertConfirmListener != null)
                onAlertConfirmListener.onClick();
        });
    }

    @Override
    public int getDialogStyle() {
        return R.style.CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    private OnAlertCancelListener onAlertCancelListener;

    public void setOnAlertCancelListener(OnAlertCancelListener listener) {
        this.onAlertCancelListener = listener;
    }

    public interface OnAlertCancelListener {
        void onClick();
    }

    private OnAlertConfirmListener onAlertConfirmListener;

    public void setOnAlertConfirmListener(OnAlertConfirmListener onAlertConfirmListener) {
        this.onAlertConfirmListener = onAlertConfirmListener;
    }

    public interface OnAlertConfirmListener {
        void onClick();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(@StringRes int messageId) {
        this.messageId = messageId;
    }

    public void setTitle(@StringRes int titleId) {
        this.titleId = titleId;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public void setConfirm(@StringRes int confirmId) {
        this.confirmId = confirmId;
    }

    public void setCancel(@StringRes int cancelId) {
        this.cancelId = cancelId;
    }

    public void setCancelGone() {
        cancelGone = true;
    }

    public void setMessageTextColorId(int messageTextColorId) {
        this.messageTextColorId = messageTextColorId;
    }
}
