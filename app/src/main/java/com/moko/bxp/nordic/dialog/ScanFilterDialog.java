package com.moko.bxp.nordic.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;

import butterknife.BindView;
import butterknife.OnClick;


public class ScanFilterDialog extends BaseDialog {
    @BindView(R2.id.et_filter_name)
    EditText etFilterName;
    @BindView(R2.id.et_filter_mac)
    EditText etFilterMac;
    @BindView(R2.id.tv_rssi)
    TextView tvRssi;
    @BindView(R2.id.sb_rssi)
    SeekBar sbRssi;

    private int filterRssi;
    private String filterName;
    private String filterMac;

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_scan_filter;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {
        tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = (progress * -1);
                tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbRssi.setProgress(Math.abs(filterRssi));
        if (!TextUtils.isEmpty(filterName)) {
            etFilterName.setText(filterName);
            etFilterName.setSelection(filterName.length());
        }
        if (!TextUtils.isEmpty(filterMac)) {
            etFilterMac.setText(filterMac);
            etFilterMac.setSelection(filterMac.length());
        }
        setDismissEnable(true);
    }

    @OnClick(R2.id.iv_filter_name_delete)
    public void onFilterNameDete(View view) {
        etFilterName.setText("");
    }

    @OnClick(R2.id.iv_filter_mac_delete)
    public void onFilterMacDelete(View view) {
        etFilterMac.setText("");
    }

    @OnClick(R2.id.tv_done)
    public void onViewClicked(View view) {
        listener.onDone(etFilterName.getText().toString(), etFilterMac.getText().toString(), filterRssi);
        dismiss();
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
