package com.MohafizDZ.project.scan_dir.presenters;

import static com.MohafizDZ.project.models.CompanyModel.QR_CODE_TYPE_ADMIN_PERMISSION;
import static com.MohafizDZ.project.models.CompanyModel.QR_CODE_TYPE_UNDEFINED;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.scan_dir.ConcreteScanPresenter;
import com.MohafizDZ.project.scan_dir.IScanPresenter;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminScanPresenterImpl extends ConcreteScanPresenter {
    private static final String TAG = AdminScanPresenterImpl.class.getSimpleName();
    private String currentCompanyId;

    @Override
    protected void onQrCodeScanned(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        String type = jsonObject.has("type")? jsonObject.getString("type") : QR_CODE_TYPE_UNDEFINED;
        if(type.equals(QR_CODE_TYPE_ADMIN_PERMISSION)){
            String id = jsonObject.getString("id");
            if(currentCompanyId.equals(id)){
                view.setResult(Activity.RESULT_OK, null);
                view.goBack();
            }else{
                view.showToast(getString(R.string.scan_qr_code_failed_msg));
            }
        }
    }

    public AdminScanPresenterImpl(IScanPresenter.View view, Context context) {
        super(view, context);
    }

    @Override
    public void onViewCreated() {
        view.showSimpleDialog(getString(R.string.warning_label), getString(R.string.admin_scan_msg));
        initData();
        onRefresh();
    }

    private void initData(){
        currentCompanyId = CompanyModel.getCurrentCompanyId(context);
    }

    @Override
    public void onRefresh() {

    }
}
