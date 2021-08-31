package com.moko.bxp.nordic.dialog;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.bxp.nordic.R;
import com.moko.bxp.nordic.R2;
import com.moko.support.nordic.entity.UrlSchemeEnum;

import butterknife.BindView;
import butterknife.OnClick;

public class UrlSchemeDialog extends BaseDialog<String> {

    @BindView(R2.id.rg_url_scheme)
    RadioGroup rgUrlScheme;
    @BindView(R2.id.rb_http_www)
    RadioButton rbHttpWww;
    @BindView(R2.id.rb_https_www)
    RadioButton rbHttpsWww;
    @BindView(R2.id.rb_http)
    RadioButton rbHttp;
    @BindView(R2.id.rb_https)
    RadioButton rbHttps;

    public UrlSchemeDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_url_scheme;
    }

    @Override
    protected void renderConvertView(View convertView, String urlScheme) {
        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlDesc(urlScheme);
        switch (urlSchemeEnum.getUrlType()) {
            case 0:
                rbHttpWww.setChecked(true);
                break;
            case 1:
                rbHttpsWww.setChecked(true);
                break;
            case 2:
                rbHttp.setChecked(true);
                break;
            case 3:
                rbHttps.setChecked(true);
                break;
        }
    }

    @OnClick(R2.id.tv_cancel)
    public void onCancel(View view) {
        dismiss();

    }

    @OnClick(R2.id.tv_ensure)
    public void onConfirm(View view) {
        dismiss();
        urlSchemeClickListener.onEnsureClicked((String) findViewById(rgUrlScheme.getCheckedRadioButtonId()).getTag());

    }

    private UrlSchemeClickListener urlSchemeClickListener;

    public void setUrlSchemeClickListener(UrlSchemeClickListener urlSchemeClickListener) {
        this.urlSchemeClickListener = urlSchemeClickListener;
    }

    public interface UrlSchemeClickListener {

        void onEnsureClicked(String urlType);
    }
}
