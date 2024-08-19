package com.MohafizDZ.project.sign_up_dir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.App;
import com.MohafizDZ.co.ceryle.radiorealbutton.RadioRealButtonGroup;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.controls.KeyValueSpinner;
import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.StartClassHelper;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountDetailsActivity extends MyAppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    public static final String TAG = AccountDetailsActivity.class.getSimpleName();
    private static final int TAGS_SELECTION_PAGE_KEY = 517;

    private MaterialButton nextButton;
    private View descriptionTextView, welcomeTextView, toFocusView;
    private TextInputEditText companyNameTextInput, nameTextInput, phoneTextInput, vehicleTextInput;
    private MUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_details);
        findViewById();
        init();
        setControls();
        initView();
    }

    private void initView() {
        DataRow companyRow = CompanyModel.getCurrentCompany(this);
        if(companyRow != null){
            companyNameTextInput.setText(companyRow.getString("name"));
            companyNameTextInput.setTag(companyRow.getString(Col.SERVER_ID));
        }else{
            Toast.makeText(this, getString(R.string.scan_company_required_msg), Toast.LENGTH_LONG);
            finish();
            return;
        }
        DataRow userRow = app().getCurrentUser();
        MUser user = MUser.getCurrentMUser(this);
        String uid = user.getUid();
//        String[] states = getResources().getStringArray(R.array.states);
        if(uid.equals("")){
            Toast.makeText(this, getString(R.string.error_restart_app_txt), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if(userRow == null) {
            String name = user.getName() == null ? "" : user.getName();
            String lastName = user.getLastName() == null ? "" : user.getLastName();
            if (!name.equals("") || !lastName.equals("")) {
                nameTextInput.setText(user.getName().concat(" ").concat(user.getLastName()));
            }
        }else{
            nameTextInput.setText(userRow.getString("name"));
            if(!userRow.getString("phone_number").equals("false")) {
                phoneTextInput.setText(userRow.getString("phone_number"));
            }
            if(!userRow.getString("vehicle_name").equals("false")) {
                vehicleTextInput.setText(userRow.getString("vehicle_name"));
            }
        }
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
//        regionIdsByNames.clear();
//        regionIdsByNames.put(getString(R.string.none_label), null);
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    private void init(){
        initData();
    }

    private void initData() {
        user = MUser.getCurrentMUser(this);
    }

    private void findViewById() {
        nextButton = findViewById(R.id.nextButton);
        companyNameTextInput = findViewById(R.id.companyNameTextInput);
        nameTextInput = findViewById(R.id.nameTextInput);
        phoneTextInput = findViewById(R.id.phoneTextInput);
        vehicleTextInput = findViewById(R.id.vehicleTextInput);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
    }

    private void setControls() {
        nextButton.setOnClickListener(this);
        nameTextInput.setOnFocusChangeListener(this);
        vehicleTextInput.setOnFocusChangeListener(this);
        phoneTextInput.setOnFocusChangeListener(this);
        KeyboardVisibilityEvent.setEventListener(
                AccountDetailsActivity.this,
                (KeyboardVisibilityEventListener) isOpen -> {
                    if(isOpen){
                        welcomeTextView.setVisibility(View.GONE);
                        descriptionTextView.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> toFocusView.requestFocus(), 300);
                    }else{
                        welcomeTextView.setVisibility(View.VISIBLE);
                        descriptionTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b){
            toFocusView = view;
        }
    }


    @Override
    public void setTitleBar(ActionBar actionBar) {
        setTitle(null);
        actionBar.setTitle(null);
    }

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    public void onClick(View v) {
        MyUtil.preventDoubleClick(v);
        int id = v.getId();
        if (id == R.id.nextButton) {
            fillUserDetails();
        }
    }

    private void fillUserDetails() {
        Log.d("account", "clicked on " + MyUtil.getCurrentDate());
        DataRow currentUserRow = app().getCurrentUser();
        String companyId = companyNameTextInput.getTag().toString();
        String fullName = MyUtil.getAllowedText(Objects.requireNonNull(nameTextInput.getText()).toString());
        String phoneNum = phoneTextInput.getText().toString();
        String vehicleName = vehicleTextInput.getText().toString();
        fullName = fullName.trim();
        if(TextUtils.isEmpty(fullName)){
            nameTextInput.setError(getString(R.string.name_required));
            return;
        }else{
            nameTextInput.setError(null);
        }
        if(TextUtils.isEmpty(vehicleName)){
            vehicleTextInput.setError(getString(R.string.vehicle_required));
            return;
        }else{
            vehicleTextInput.setError(null);
        }
        Values values = new Values();
        values.put("company_id", companyId);
        values.put("name", fullName);
        values.put("vehicle_name", vehicleName);
        values.put("phone_number", phoneNum);
        values.put("phone_code", "213");
//        if(state.equals("Other")){
//            values.put("country", "Other");
//        }
        values.put("_is_active", 1);
        values.put("firebase_user_id", user.getUid());
        values.put("is_phone_authenticated", 0);
        //todo uncomment these when moving to phone authentication
//        values.put("is_phone_authenticated", app().firebaseAuth.getCurrentUser().getPhoneNumber() != null? 1 : 0);
//        values.put("phone_number", user.getPhoneNumber());
        values.put("email", user.getEmail());
        UserModel userModel = new UserModel(this);
        if(currentUserRow == null) {
            userModel.insert(values);
        }else{
            userModel.update(currentUserRow.getInteger(Col.ROWID), values);
        }
        App.restartApp(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == TAGS_SELECTION_PAGE_KEY && resultCode == Activity.RESULT_OK){
//            openHomePage();
//            setResult(RESULT_OK);
//            finish();
//        }
//    }

/*
    private void prepareCountryFlag(String currentKey) {
        DataRow currentRow = countriesMap.get(currentKey);
        String flag = currentRow.getString("emoji");
// Create a Paint object to draw the flag
        Paint paint = new Paint();
        paint.setTextSize(200);
        paint.setTextAlign(Paint.Align.CENTER);

// Create a Bitmap to hold the flag
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

// Draw the flag onto the Bitmap
        canvas.drawText(flag, canvas.getWidth() / 2f, canvas.getHeight() / 2f, paint);

// Set the Bitmap as the image of an ImageView
        countryImageView.setImageBitmap(bitmap);
    }
*/

}
