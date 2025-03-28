package com.moko.bxp.nordic.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.widget.SeekBar;

import com.moko.bxp.nordic.databinding.DialogScanFilterBinding;


public class ScanFilterDialog extends BaseDialog<DialogScanFilterBinding> {
    private int filterRssi;
    private String filterName;
    private String filterMac;

    @Override
    protected DialogScanFilterBinding getViewBind() {
        return DialogScanFilterBinding.inflate(getLayoutInflater());
    }

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        mBind.tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        mBind.sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = (progress * -1);
                mBind.tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBind.sbRssi.setProgress(Math.abs(filterRssi));
        if (!TextUtils.isEmpty(filterName)) {
            mBind.etFilterName.setText(filterName);
            mBind.etFilterName.setSelection(filterName.length());
        }
        if (!TextUtils.isEmpty(filterMac)) {
            mBind.etFilterMac.setText(filterMac);
            mBind.etFilterMac.setSelection(filterMac.length());
        }
        setDismissEnable(true);
        mBind.ivFilterNameDelete.setOnClickListener(v -> mBind.etFilterName.setText(""));
        mBind.ivFilterMacDelete.setOnClickListener(v -> mBind.etFilterMac.setText(""));
        mBind.tvDone.setOnClickListener(v -> {
            listener.onDone(mBind.etFilterName.getText().toString(), mBind.etFilterMac.getText().toString(),filterRssi);
            dismiss();
        });
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterMac(String filterMac) {
        this.filterMac = filterMac;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public interface OnScanFilterListener {
        void onDone(String filterName, String filterMac, int filterRssi);
    }
}
