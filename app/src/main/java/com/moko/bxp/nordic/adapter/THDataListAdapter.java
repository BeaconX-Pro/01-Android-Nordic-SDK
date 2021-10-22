package com.moko.bxp.nordic.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.nordic.R;
import com.moko.support.nordic.entity.THStoreData;

public class THDataListAdapter extends BaseQuickAdapter<THStoreData, BaseViewHolder> {
    public THDataListAdapter() {
        super(R.layout.item_export_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, THStoreData item) {
        helper.setText(R.id.tv_time, item.time);
        helper.setText(R.id.tv_temp, item.temp);
        helper.setText(R.id.tv_humidity, item.humidity);
    }
}
