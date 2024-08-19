package com.MohafizDZ.project.scan_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.MohafizDZ.framework_repository.Utils.CodeScannerActivity;
import com.MohafizDZ.own_distributor.R;

public class ScanActivity extends CodeScannerActivity implements CodeScannerActivity.ScanListener, IScanPresenter.View {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final String PRESENTER_TYPE_KEY = "presenter_type_key";

    private IScanPresenter.Presenter presenter;
    private IScanPresenter.PresenterType presenterType;

    @Override
    public ScanListener setScanListener() {
        return this;
    }

    @Override
    public void onScanCompleted(String result) {
        presenter.onScan(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();
        initPresenter();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        if(data == null){
            showToast(getString(R.string.error_occurred));
            finish();
            return;
        }
        presenterType = IScanPresenter.PresenterType.valueOf(data.getString(PRESENTER_TYPE_KEY));
    }

    private void initPresenter(){
        presenter = ConcreteScanPresenter.getInstance(presenterType, this, this);
    }

    private void initView() {
        if(presenter != null){
            presenter.onViewCreated();
        }
    }

    @Override
    public void goBack() {
        finish();
    }
    @Override
    public void setResult(int resultCode, Bundle data){
        if(data != null){
            Intent intent = new Intent();
            intent.putExtras(data);
            setResult(resultCode, intent);
        }else {
            setResult(resultCode);
        }
    }

    public static Intent getAdminIntent(Context context){
        return getIntent(context, IScanPresenter.PresenterType.admin);
    }

    public static Intent getProductScanIntent(Context context){
        return getIntent(context, IScanPresenter.PresenterType.productScan);
    }
    private static Intent getIntent(Context context, IScanPresenter.PresenterType presenterType){
        Intent intent = new Intent(context, ScanActivity.class);
        Bundle data = new Bundle();
        data.putString(PRESENTER_TYPE_KEY, presenterType.name());
        intent.putExtras(data);
        return intent;
    }
}
