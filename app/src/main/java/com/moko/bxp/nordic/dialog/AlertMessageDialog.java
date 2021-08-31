package com.moko.bxp.nordic.dialog;

import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;

import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertMessageDialog extends MokoBaseDialog {
    public static final String TAG = AlertMessageDialog.class.getSimpleName();
    @BindView(R2.id.tv_alert_title)
    TextView tvAlertTitle;
    @BindView(R2.id.ll_alert_title)
    LinearLayout llAlertTitle;
    @BindView(R2.id.tv_alert_message)
    TextView tvAlertMessage;
    @BindView(R2.id.tv_alert_cancel)
    TextView tvAlertCancel;
    @BindView(R2.id.tv_alert_confirm)
    TextView tvAlertConfirm;
    @BindView(R2.id.view_divider)
    View viewDivider;

    private String cancel;

    private String confirm;

    private String title;

    private String message;

    private int cancelId = -1;

    private int confirmId = -1;

    private int titleId = -1;

    private int messageId = -1;

    private boolean cancelGone;


    @Override
    public int getLayoutRes() {
        return R.layout.dialog_alert;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        if (titleId > 0) {
            title = getString(titleId);
        }
        if (messageId > 0) {
            message = getString(messageId);
        }
        if (confirmId > 0) {
            confirm = getString(confirmId);
        }
        if (cancelId > 0) {
            cancel = getString(cancelId);
        }
        TextPaint tp = tvAlertTitle.getPaint();
        tp.setFakeBoldText(true);
        if (TextUtils.isEmpty(title)) {
            llAlertTitle.setVisibility(View.GONE);
        } else {
            tvAlertTitle.setText(title);
        }
        tvAlertMessage.setText(message);
        if (!TextUtils.isEmpty(cancel)) {
            tvAlertCancel.setText(cancel);
        }
        if (!TextUtils.isEmpty(confirm)) {
            tvAlertConfirm.setText(confirm);
        }
        if (cancelGone) {
            tvAlertCancel.setVisibility(View.GONE);
            viewDivider.setVisibility(View.GONE);
        }
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

    @OnClick(R2.id.tv_alert_cancel)
    public void onCancel(View view) {
        dismiss();
        if (onAlertCancelListener != null)
            onAlertCancelListener.onClick();
    }

    @OnClick(R2.id.tv_alert_confirm)
    public void onConfirm(View view) {
        dismiss();
        if (onAlertConfirmListener != null)
            onAlertConfirmListener.onClick();
    }
}
