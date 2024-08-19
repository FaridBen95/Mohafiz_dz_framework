package com.MohafizDZ.project.product_form_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.MohafizDZ.framework_repository.Utils.CodeScannerActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;

import org.json.JSONObject;

public class ProductScanActivity extends CodeScannerActivity implements CodeScannerActivity.ScanListener {
    private static final String TAG = ProductScanActivity.class.getSimpleName();
    public static final String PRODUCT_CODE_KEY = "product_code_key";

    public static Intent getIntent(Context context) {
        return new Intent(context, ProductScanActivity.class);
    }

    @Override
    public ScanListener setScanListener() {
        return this;
    }

    @Override
    public void onScanCompleted(String result) {
        String clearTxt = result;
        try{
            validateProductCode(clearTxt);
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, getString(R.string.scan_code_wrong_msg), Toast.LENGTH_SHORT).show());
        }
    }

    private void validateProductCode(String productCode){
        Bundle data = new Bundle();
        data.putString(PRODUCT_CODE_KEY, productCode);
        Intent intent = new Intent();
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
        finish();
    }
}

