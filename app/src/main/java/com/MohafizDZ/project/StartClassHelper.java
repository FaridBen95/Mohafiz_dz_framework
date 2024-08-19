package com.MohafizDZ.project;

import android.app.Activity;

import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.project.home_dir.HomeActivity;
import com.MohafizDZ.project.sign_up_dir.AccountDetailsActivity;

public class StartClassHelper {

    public static void openSignUpActivity(Activity activity) {
        IntentUtils.startActivity(activity, AccountDetailsActivity.class, null);
        activity.finish();
    }

    public static void openProjectMainActivity(Activity activity) {
        IntentUtils.startActivity(activity, HomeActivity.class, null);
        activity.finish();
    }
}
