package com.MohafizDZ.framework_repository.Utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class CurrentLocationUtil implements LocationListener {
    private static final String TAG = CurrentLocationUtil.class.getSimpleName();
    private final Context context;
    private FragmentActivity fragmentActivity;
    private Fragment fragment;
    private Location mylocation;
    private MyLocationListener myLocationListener;
    private boolean informed;
    private static int id = -1;
    private static Location lastLocation;
    private boolean showAlertDialog = true;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocationListener locationListener;

    private boolean stopPrintLocation;
    private FusedLocationProviderClient fusedLocationClient;

    public void setStopPrintLocation(boolean stopPrintLocation) {
        this.stopPrintLocation = stopPrintLocation;
    }

    public CurrentLocationUtil(Context context, FragmentActivity fragmentActivity, MyLocationListener locationListener) {
        this(context, fragmentActivity, locationListener, true);
    }

    public CurrentLocationUtil(Context context, FragmentActivity fragmentActivity, MyLocationListener locationListener, boolean allowExecute) {
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        myLocationListener = locationListener;
        id++;
        initControls();
        if (allowExecute) {
            execute();
        }
    }

    public CurrentLocationUtil setLocationListener(MyLocationListener locationListener){
        this.myLocationListener = locationListener;
        return this;
    }

    public CurrentLocationUtil(Context context, Fragment fragment, MyLocationListener locationListener) {
        this(context, fragment, locationListener, true);
    }

    public CurrentLocationUtil(Context context, Fragment fragment, MyLocationListener locationListener, boolean allowExecute) {
        this.context = context;
        this.fragment = fragment;
        myLocationListener = locationListener;
        id++;
        initControls();
        if (allowExecute) {
            execute();
        }
    }

    private void initControls() {
        locationListener = this;
        if (fragmentActivity != null) {
            requestPermissionLauncher = fragmentActivity.registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    result -> {
                        int permissionLocation = ContextCompat.checkSelfPermission(fragmentActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                            getMyLocation();
                        }
                    }
            );
        } else {
            requestPermissionLauncher = fragment.registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    result -> {
                        int permissionLocation = ContextCompat.checkSelfPermission(fragment.getActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                            getMyLocation();
                        }
                    }
            );
        }
    }

    public void execute() {
        setUpGClient();
        getMyLocation();
    }

    private synchronized void setUpGClient() {
        myLocationListener.loading();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!stopPrintLocation) {
            mylocation = location;
            lastLocation = mylocation;
            Double latitude = mylocation.getLatitude();
            Double longitude = mylocation.getLongitude();
            Intent data = new Intent();
            data.putExtra("latitude", latitude);
            data.putExtra("longitude", longitude);
            stopPrintLocation = myLocationListener.printLocation(latitude, longitude);
            if(stopPrintLocation){
                stopLocationUpdates();
            }
        }
        if(location.getLatitude() == 0 || location.getLongitude() == 0) {
            myLocationListener.checkLocation(informed);
        }
    }

    public Location getLastLocation() {
        return lastLocation != null ? lastLocation : new Location("");
    }

    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        if (isLocationEnabled(context)) {
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build();
            fusedLocationClient.removeLocationUpdates(locationListener);
            FragmentActivity f = fragmentActivity != null? fragmentActivity : fragment.getActivity();
            fusedLocationClient.requestLocationUpdates(locationRequest, locationListener, null);
            stopPrintLocation = false;
        } else {
            if (showAlertDialog && !informed) {
                new AlertDialog.Builder(context).setTitle("GPS est désactivé").setMessage("Veuillez activer le GPS").show();
            }
            if (myLocationListener.checkLocation(informed)) {
                informed = true;
            }
        }
    }

    //    private void getLastLocation() {
//    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;


    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            getMyLocation();
        }

    }

    public void stopLocationUpdates(){
        stopPrintLocation = false;
        if(fusedLocationClient != null){
            fusedLocationClient.removeLocationUpdates(this);
        }
    }



    public interface MyLocationListener{
        void loading();
        boolean printLocation(Double latitude, Double longitude);
        boolean checkLocation(boolean failedInformed);
    }

    public void setShowAlertDialog(boolean showAlertDialog) {
        this.showAlertDialog = showAlertDialog;
    }

}
