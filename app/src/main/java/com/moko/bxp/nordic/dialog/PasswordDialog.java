package com.moko.bxp.nordic.dialog;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.databinding.DialogPasswordBinding;
import com.moko.bxp.nordic.utils.ToastUtils;

public class PasswordDialog extends MokoBaseDialog<DialogPasswordBinding> {
    public static final String TAG = PasswordDialog.class.getSimpleName();


    private final String FILTER_ASCII = "[ -~]*";

    private String password;


    @Override
    protected DialogPasswordBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreateView() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        mBind.etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        if (!TextUtils.isEmpty(password)) {
            mBind.etPassword.setText(password);
            mBind.etPassword.setSelection(password.length());
        }
        mBind.tvPasswordCancel.setOnClickListener(v -> {
            dismiss();
            if (passwordClickListener != null) {
                passwordClickListener.onDismiss();
            }
        });
        mBind.tvPasswordEnsure.setOnClickListener(v -> {
            dismiss();
            if (TextUtils.isEmpty(mBind.etPassword.getText().toString())) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.password_null));
                return;
            }
            if (passwordClickListener != null)
                passwordClickListener.onEnsureClicked(mBind.etPassword.getText().toString());
        });
        mBind.etPassword.postDelayed(new Runnable() {
            @Override
            public void run() {
                //设置可获得焦点
                mBind.etPassword.setFocusable(true);
                mBind.etPassword.setFocusableInTouchMode(true);
                //请求获得焦点
                mBind.etPassword.requestFocus();
                //调用系统输入法
                InputMethodManager inputManager = (InputMethodManager) mBind.etPassword
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mBind.etPassword, 0);
            }
        }, 200);
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

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
