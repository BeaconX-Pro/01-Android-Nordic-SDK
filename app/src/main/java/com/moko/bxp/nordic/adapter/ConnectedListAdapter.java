package com.moko.bxp.nordic.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.nordic.R;

public class ConnectedListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public ConnectedListAdapter() {
        super(R.layout.item_connected_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvMac, item);
        helper.addOnClickListener(R.id.tvDisconnect);
    }
}
