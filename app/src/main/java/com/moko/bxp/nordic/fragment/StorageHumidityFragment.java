package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.activity.THDataActivity;
import com.moko.bxp.nordic.databinding.FragmentStorageHumidityBinding;
import com.moko.lib.bxpui.dialog.BottomDialog;

import java.util.ArrayList;

public class StorageHumidityFragment extends Fragment {

    private static final String TAG = "StorageHumidityFragment";
   private FragmentStorageHumidityBinding mBind;
    private ArrayList<String> mDatas;

    private THDataActivity activity;


    public StorageHumidityFragment() {
    }

    public static StorageHumidityFragment newInstance() {
        StorageHumidityFragment fragment = new StorageHumidityFragment();
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
        mBind = FragmentStorageHumidityBinding.inflate(inflater, container, false);
        activity = (THDataActivity) getActivity();
        mDatas = new ArrayList<>();
        for (int i = 0; i <= 190; i++) {
            mDatas.add(MokoUtils.getDecimalFormat("0.0").format(i * 0.5));
        }
        return mBind.getRoot();
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

    private int mSelected;

    public void setHumidityData(int data) {
        mSelected = data;
        String humStr = mDatas.get(mSelected);
        mBind.tvStorageHumidityOnly.setText(humStr);
        if (mSelected == 0) {
            mBind.tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
        } else {
            mBind.tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, humStr));
        }
    }

    public void selectStorageHumi() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mDatas, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            if (mSelected == 0) {
                mBind.tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
            } else {
                mBind.tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, mDatas.get(value)));
            }
            mBind.tvStorageHumidityOnly.setText(mDatas.get(mSelected));
            activity.setSelectedHumidity(value);
        });
        dialog.show(activity.getSupportFragmentManager());
    }
}
