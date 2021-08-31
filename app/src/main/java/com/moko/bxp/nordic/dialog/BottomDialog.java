package com.moko.bxp.nordic.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.view.WheelView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BottomDialog extends MokoBaseDialog {


    @BindView(R2.id.wv_bottom)
    WheelView wvBottom;
    private ArrayList<String> mDatas;
    private int mIndex;


    @Override
    public int getLayoutRes() {
        return R.layout.dialog_bottom;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        wvBottom.setData(mDatas);
        wvBottom.setDefault(mIndex);
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @OnClick(R2.id.tv_cancel)
    public void onCancel(View view) {
        dismiss();
    }

    @OnClick(R2.id.tv_confirm)
    public void onConfirm(View view) {
        if (TextUtils.isEmpty(wvBottom.getSelectedText())) {
            return;
        }
        dismiss();
        final int selected = wvBottom.getSelected();
        if (listener != null) {
            listener.onValueSelected(selected);
        }
    }

    public void setDatas(ArrayList<String> datas, int index) {
        this.mDatas = datas;
        this.mIndex = index;
    }

    private OnBottomListener listener;

    public void setListener(OnBottomListener listener) {
        this.listener = listener;
    }

    public interface OnBottomListener {
        void onValueSelected(int value);
    }
}
