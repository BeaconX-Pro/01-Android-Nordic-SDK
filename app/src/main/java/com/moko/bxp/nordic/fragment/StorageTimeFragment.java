package com.moko.bxp.nordic.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.bxp.nordic.activity.THDataActivity;
import com.moko.bxp.nordic.dialog.BottomDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StorageTimeFragment extends Fragment {

    private static final String TAG = "StorageTimeFragment";
    @BindView(R2.id.tv_storage_time_only)
    TextView tvStorageTimeOnly;
    @BindView(R2.id.tv_time_tips)
    TextView tvTimeTips;
    private ArrayList<String> mDatas;

    private THDataActivity activity;


    public StorageTimeFragment() {
    }

    public static StorageTimeFragment newInstance() {
        StorageTimeFragment fragment = new StorageTimeFragment();
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
        View view = inflater.inflate(R.layout.fragment_storage_time, container, false);
        ButterKnife.bind(this, view);
        activity = (THDataActivity) getActivity();
        tvTimeTips.setText(getString(R.string.time_only_tips, mSelected + 1));
        mDatas = new ArrayList<>();
        for (int i = 1; i <= 255; i++) {
            mDatas.add(i + "");
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

    private int mSelected = 0;

    public void setTimeData(int data) {
        mSelected = data - 1;
        tvStorageTimeOnly.setText(String.valueOf(data));
        tvTimeTips.setText(getString(R.string.time_only_tips, data));
    }

    public void selectStorageTime() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mDatas, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            tvStorageTimeOnly.setText(mDatas.get(value));
            tvTimeTips.setText(getString(R.string.time_only_tips, mSelected + 1));
            activity.setSelectedTime(mSelected + 1);
        });
        dialog.show(activity.getSupportFragmentManager());
    }
}
