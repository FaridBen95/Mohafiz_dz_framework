package com.MohafizDZ.project.company_scan_dir;

import android.util.Log;
import android.widget.Toast;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.CodeScannerActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyUserModel;

import org.json.JSONObject;

import ru.bullyboo.encoder.Encoder;
import ru.bullyboo.encoder.methods.AES;

public class CompanyScanActivity extends CodeScannerActivity implements CodeScannerActivity.ScanListener {
    public static final String TAG = CompanyScanActivity.class.getSimpleName();
    @Override
    public ScanListener setScanListener() {
        return this;
    }

    @Override
    public void onScanCompleted(String result) {
        String clearTxt = App.TEST_MODE? result : Encoder.BuilderAES()
                .message(result)
                .method(AES.Method.AES_CBC_PKCS5PADDING)
                .key("JWYXelshE9saPgnpcRVuJWYXelshE9sa")
                .keySize(AES.Key.SIZE_256)
                .decrypt();
        try{
            Log.d(TAG, clearTxt);
            JSONObject jsonObject = new JSONObject(clearTxt);
            String type = jsonObject.has("type")? jsonObject.getString("type") : "company";
            if(type.equals("company")){
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String phoneNumber = jsonObject.has("support_phone_num")?
                        jsonObject.getString("support_phone_num") : "";
                //todo i should recheck if this is good for security
                String role = jsonObject.has("role")? jsonObject.getString("role") : CompanyUserModel.DISTRIBUTOR_ROLE;
                String code = jsonObject.has("code")? jsonObject.getString("code") : "";
                saveCompany(id, name, phoneNumber, role, code);
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, getString(R.string.scan_code_wrong_msg), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveCompany(String id, String name, String phoneNumber, String role, String code){
        CompanyModel.saveCompany(this, id, name, phoneNumber, role, code);
    }
}

