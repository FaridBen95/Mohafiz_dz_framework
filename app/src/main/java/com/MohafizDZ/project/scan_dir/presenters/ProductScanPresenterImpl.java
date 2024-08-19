package com.MohafizDZ.project.scan_dir.presenters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.MohafizDZ.project.scan_dir.ConcreteScanPresenter;
import com.MohafizDZ.project.scan_dir.IScanPresenter;

import org.json.JSONException;

public class ProductScanPresenterImpl extends ConcreteScanPresenter {
    private static final String TAG = ProductScanPresenterImpl.class.getSimpleName();
    public ProductScanPresenterImpl(IScanPresenter.View view, Context context) {
        super(view, context);
    }

    @Override
    public void onViewCreated() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onQrCodeScanned(String data) {
        Bundle bundle = new Bundle();
        bundle.putString("code", data);
        view.setResult(Activity.RESULT_OK, bundle);
        view.goBack();
    }
}
