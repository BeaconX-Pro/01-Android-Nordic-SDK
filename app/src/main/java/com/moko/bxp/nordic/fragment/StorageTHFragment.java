package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.activity.THDataActivity;
import com.moko.bxp.nordic.dialog.BottomDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StorageTHFragment extends Fragment {

    private static final String TAG = "StorageTHFragment";
    @BindView(R2.id.tv_storage_temp)
    TextView tvStorageTemp;
    @BindView(R2.id.tv_storage_humidity)
    TextView tvStorageHumidity;
    @BindView(R2.id.tv_t_h_tips)
    TextView tvTHTips;
    private ArrayList<String> mHumidityDatas;
    private ArrayList<String> mTempDatas;

    private THDataActivity activity;


    public StorageTHFragment() {
    }

    public static StorageTHFragment newInstance() {
        StorageTHFragment fragment = new StorageTHFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_storage_t_h, container, false);
        ButterKnife.bind(this, view);
        activity = (THDataActivity) getActivity();
        mHumidityDatas = new ArrayList<>();
        for (int i = 1; i <= 190; i++) {
            mHumidityDatas.add(MokoUtils.getDecimalFormat("0.0").format(i * 0.5));
        }
        mTempDatas = new ArrayList<>();
        for (int i = 1; i <= 120; i++) {
            mTempDatas.add(MokoUtils.getDecimalFormat("0.0").format(i * 0.5));
        }
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private int mTempSelected = 1;

    public void setTempData(int data) {
        mTempSelected = data - 1;
        if (mTempSelected < 0)
            mTempSelected = 0;
        String tempStr = mTempDatas.get(mTempSelected);
        String humiStr = mHumidityDatas.get(mHumiditySelected);
        tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, humiStr));
//        else if (mTempSelected == 0 && mHumiditySelected > 0) {
//            tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
//        } else if (mTempSelected > 0 && mHumiditySelected == 0) {
//            String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
//            tvTHTips.setText(getString(R.string.t_h_tips_2, tempStr));
//        } else if (mTempSelected == 0 && mHumiditySelected == 0) {
//            tvTHTips.setText(R.string.t_h_tips_3);
//        }
        tvStorageTemp.setText(tempStr);
    }

    private int mHumiditySelected = 1;

    public void setHumidityData(int data) {
        mHumiditySelected = data - 1;
        if (mHumiditySelected < 0)
            mHumiditySelected = 0;
        String tempStr = mTempDatas.get(mTempSelected);
        String humiStr = mHumidityDatas.get(mHumiditySelected);
        tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, humiStr));
//        else if (mTempSelected == 0 && mHumiditySelected > 0) {
//            tvTHTips.setText(getString(R.string.t_h_tips_1, humiStr));
//        } else if (mTempSelected > 0 && mHumiditySelected == 0) {
//            tvTHTips.setText(getString(R.string.t_h_tips_2, tempStr));
//        } else if (mTempSelected == 0 && mHumiditySelected == 0) {
//            tvTHTips.setText(R.string.t_h_tips_3);
//        }
        tvStorageHumidity.setText(humiStr);
    }

    public void selectStorageTemp() {
        BottomDialog tempDialog = new BottomDialog();
        tempDialog.setDatas(mTempDatas, mTempSelected);
        tempDialog.setListener(value -> {
            mTempSelected = value;
            String tempStr = mTempDatas.get(mTempSelected);
            String humiStr = mHumidityDatas.get(mHumiditySelected);
            tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, humiStr));
            tvStorageTemp.setText(tempStr);
            activity.setSelectedTemp(value + 1);
        });
        tempDialog.show(activity.getSupportFragmentManager());
    }

    public void selectStorageHumi() {
        BottomDialog humidityDialog = new BottomDialog();
        humidityDialog.setDatas(mHumidityDatas, mHumiditySelected);
        humidityDialog.setListener(value -> {
            mHumiditySelected = value;
            String tempStr = mTempDatas.get(mTempSelected);
            String humiStr = mHumidityDatas.get(mHumiditySelected);
            tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, humiStr));
            tvStorageHumidity.setText(humiStr);
            activity.setSelectedHumidity(value + 1);
        });
        humidityDialog.show(activity.getSupportFragmentManager());
    }
}
