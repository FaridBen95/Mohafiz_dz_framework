package com.MohafizDZ.project.settings_dir;

import static com.MohafizDZ.project.models.CompanyModel.QR_CODE_TYPE_ADMIN_PERMISSION;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyUserModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsPresenterImpl implements ISettingsPresenter.Presenter{
    private static final String TAG = SettingsPresenterImpl.class.getSimpleName();

    private final ISettingsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private DataRow companyRow, companyUserRow, distributorRow;
    private String role;

    public SettingsPresenterImpl(ISettingsPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);

    }

    @Override
    public void onViewCreated() {
        initData();
        view.setCompanyName(companyRow.getString("name"));
        view.setUserName(companyUserRow.getString("name"));
        view.setJoinDate(companyUserRow.getString("join_date"));
        if(role.equals(CompanyUserModel.DISTRIBUTOR_ROLE)) {
            view.setRole(getString(R.string.distributor_label));
            view.toggleQrCodeContainer(false);
        }else{
            view.toggleQrCodeContainer(true);
            view.setRole(getString(R.string.admin_label));
        }
        onRefresh();
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        companyRow = models.companyModel.getCurrentCompany();
        companyUserRow = models.companyUserModel.getCurrentUser(currentUserRow);
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        role = CompanyUserModel.getRole(companyUserRow);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void generateDistInviteQrCode(String code) {
        String qrCodeContent = generateJsonString(CompanyUserModel.DISTRIBUTOR_ROLE, code);
        view.showQrCode(qrCodeContent);
    }

    private String generateJsonString(String role, String distributorCode){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "company");
            jsonObject.put("id", companyRow.getString(Col.SERVER_ID));
            jsonObject.put("name", companyRow.getString("name"));
            jsonObject.put("support_phone_num", companyRow.getString("support_phone_num"));
            jsonObject.put("role", CompanyUserModel.getEncryptedRole(role));
            if(distributorCode != null){
                jsonObject.put("code", distributorCode);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    @Override
    public void generateAdminInviteQrCode(String code) {
        String qrCodeContent = generateJsonString(CompanyUserModel.ADMIN_ROLE, code);
        view.showQrCode(qrCodeContent);
    }

    @Override
    public void generateAdminQrCode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", QR_CODE_TYPE_ADMIN_PERMISSION);
            jsonObject.put("id", companyRow.getString(Col.SERVER_ID));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String qrCodeContent = jsonObject.toString();
        view.showQrCode(qrCodeContent);
    }

    private static class Models{
        private final UserModel userModel;
        private final DistributorModel distributorModel;
        private final CompanyModel companyModel;
        private final CompanyUserModel companyUserModel;

        private Models(Context context){
            this.userModel = new UserModel(context);
            this.distributorModel = new DistributorModel(context);
            this.companyModel = new CompanyModel(context);
            this.companyUserModel = new CompanyUserModel(context);
        }
    }
}
