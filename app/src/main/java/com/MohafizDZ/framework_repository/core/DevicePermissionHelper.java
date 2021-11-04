package com.MohafizDZ.framework_repository.core;

import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class DevicePermissionHelper implements MyAppCompatActivity.DevicePermissionResultListener {
    public static final String TAG = DevicePermissionHelper.class.getSimpleName();
    public static final int REQUEST_PERMISSION = 7;
    public static final int REQUEST_PERMISSIONS = 8;
    private MyAppCompatActivity mActivity;
    private PermissionGrantListener mPermissionGrantListener;

    public DevicePermissionHelper(MyAppCompatActivity activity) {
        mActivity = activity;
        mActivity.setOnDevicePermissionResultListener(this);
    }

    public DevicePermissionHelper(BaseFragment fragment) {
        this(fragment.parent());
    }

    public boolean hasPermission(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(mActivity, permission);
        switch (permissionCheck) {
            case PackageManager.PERMISSION_GRANTED:
                return true;
            case PackageManager.PERMISSION_DENIED:
                return false;
        }
        return false;
    }

    public void requestToGrantPermission(PermissionGrantListener callback, String permission) {
        mPermissionGrantListener = callback;
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            if (callback != null) callback.onPermissionRationale();
        } else {
            ActivityCompat.requestPermissions(mActivity, new String[]{permission}, REQUEST_PERMISSION);
        }
    }

    public void requestPermissions(PermissionGrantListener callback, String[] permissions) {
        mPermissionGrantListener = callback;
        ActivityCompat.requestPermissions(mActivity, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionGranted();
            } else {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionDenied();
            }
        }
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionGranted();
            } else {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionDenied();
            }
        }
    }

    public interface PermissionGrantListener {
        void onPermissionGranted();

        void onPermissionDenied();

        void onPermissionRationale();
    }

    public interface DevicePermissionImpl {
        String[] permissions();
    }
}
