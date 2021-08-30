package com.moko.bxp.h6.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.bxp.h6.R;
import com.moko.bxp.h6.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ModifyPasswordDialog extends BaseDialog {

    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";

    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    @BindView(R.id.et_new_password_re)
    EditText etNewPasswordRe;
    @BindView(R.id.tv_password_ensure)
    TextView tvPasswordEnsure;
    private boolean passwordEnable;
    private boolean confirmPasswordEnable;


    public ModifyPasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_change_password;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etNewPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        etNewPasswordRe.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEnable = count > 0;
                tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etNewPasswordRe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordEnable = count > 0;
                tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.tv_cancel, R.id.tv_password_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_password_ensure:
                dismiss();
                String newPassword = etNewPassword.getText().toString();
                String newPasswordRe = etNewPasswordRe.getText().toString();
                if (!newPasswordRe.equals(newPassword)) {
                    ToastUtils.showToast(getContext(), "Password do not match! Please try again.");
                    return;
                }
                if (modifyPasswordClickListener != null)
                    modifyPasswordClickListener.onEnsureClicked(etNewPassword.getText().toString());
                break;
        }
    }

    private ModifyPasswordClickListener modifyPasswordClickListener;

    public void setOnModifyPasswordClicked(ModifyPasswordClickListener modifyPasswordClickListener) {
        this.modifyPasswordClickListener = modifyPasswordClickListener;
    }

    public interface ModifyPasswordClickListener {

        void onEnsureClicked(String password);
    }

    public void showKeyboard() {
        if (etNewPassword != null) {
            //设置可获得焦点
            etNewPassword.setFocusable(true);
            etNewPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            etNewPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etNewPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etNewPassword, 0);
        }
    }
}
