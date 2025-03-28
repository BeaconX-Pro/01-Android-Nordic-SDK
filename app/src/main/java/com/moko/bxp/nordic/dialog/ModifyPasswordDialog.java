package com.moko.bxp.nordic.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.databinding.DialogChangePasswordBinding;

public class ModifyPasswordDialog extends MokoBaseDialog<DialogChangePasswordBinding> {
    public static final String TAG = ModifyPasswordDialog.class.getSimpleName();

    private final String FILTER_ASCII = "[ -~]*";

    private boolean passwordEnable;
    private boolean confirmPasswordEnable;


    @Override
    protected DialogChangePasswordBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogChangePasswordBinding.inflate(inflater, container, false);
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
        mBind.etNewPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        mBind.etNewPasswordRe.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        mBind.etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEnable = count > 0;
                mBind.tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBind.etNewPasswordRe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordEnable = count > 0;
                mBind.tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBind.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvPasswordEnsure.setOnClickListener(v -> {
            dismiss();
            String newPassword = mBind.etNewPassword.getText().toString();
            String newPasswordRe = mBind.etNewPasswordRe.getText().toString();
            if (!newPasswordRe.equals(newPassword)) {
                if (modifyPasswordClickListener != null)
                    modifyPasswordClickListener.onPasswordNotMatch();
                return;
            }
            if (modifyPasswordClickListener != null)
                modifyPasswordClickListener.onEnsureClicked(mBind.etNewPassword.getText().toString());
        });
        mBind.etNewPassword.postDelayed(() -> {
            //设置可获得焦点
            mBind.etNewPassword.setFocusable(true);
            mBind.etNewPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            mBind.etNewPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) mBind.etNewPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(mBind.etNewPassword, 0);
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

    private ModifyPasswordClickListener modifyPasswordClickListener;

    public void setOnModifyPasswordClicked(ModifyPasswordClickListener modifyPasswordClickListener) {
        this.modifyPasswordClickListener = modifyPasswordClickListener;
    }

    public interface ModifyPasswordClickListener {

        void onEnsureClicked(String password);

        void onPasswordNotMatch();
    }
}
