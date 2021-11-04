package com.MohafizDZ.project;

import android.app.Activity;

public class StartClassHelper {

    public static void openSignUpActivity(Activity activity) {
//        IntentUtils.startActivity(context, SignUpClass.class, null);
        activity.finish();
    }

    public static void openProjectMainActivity(Activity activity) {
//        IntentUtils.startActivity(activity, HomeActivity.class, null);
        activity.finish();
    }
}
