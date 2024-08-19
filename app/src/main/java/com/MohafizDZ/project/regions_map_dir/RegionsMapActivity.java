package com.MohafizDZ.project.regions_map_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.regions_map_dir.region_creation_presenter_dir.IRegionCreationPresenter;
import com.MohafizDZ.project.regions_map_dir.region_creation_presenter_dir.RegionCreationPresenterImpl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

public class RegionsMapActivity extends MyAppCompatActivity implements IRegionsMapPresenter.View,
        CurrentLocationUtil.MyLocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, IRegionCreationPresenter.View, Slider.OnChangeListener {
    private static final String TAG = RegionsMapActivity.class.getSimpleName();
    private IRegionsMapPresenter.Presenter regionsMapPresenter;
    private IRegionCreationPresenter.Presenter regionCreationPresenter;
    private GoogleMap map;
    private Slider slider;
    private CurrentLocationUtil currentLocationUtil;
    private Circle currentCircle = null;
    private Menu menu;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regions_map_layout);
        init();
        findViewById();
        setControls();
        initView();
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        regionsMapPresenter = new RegionsMapPresenterImpl(this, this, currentUserRow);
        regionCreationPresenter = new RegionCreationPresenterImpl(this, this, currentUserRow);
        currentLocationUtil = new CurrentLocationUtil(this, this, this, false);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(googleMap -> {
                    map = googleMap;
                    regionsMapPresenter.onRefresh();
                    map.setOnMapClickListener(RegionsMapActivity.this);
                    map.setOnMarkerDragListener(RegionsMapActivity.this);
                });
    }

    private void findViewById(){
        slider = findViewById(R.id.slider);
    }

    private void setControls(){
        slider.addOnChangeListener(this);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regionCreationPresenter.onBackPressed();
            }
        });
    }

    private void initView(){
        regionsMapPresenter.onViewCreated();
    }

    @Override
    public void getCurrentLocation() {
        currentLocationUtil.execute();
    }

    public static Intent getIntent(Context context){
        return new Intent(context, RegionsMapActivity.class);
    }

    @Override
    public void loading() {
    }

    @Override
    public boolean printLocation(Double latitude, Double longitude) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14));
        map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
        return true;
    }

    @Override
    public void showRegion(double latitude, double longitude, float radius, boolean isNewRegion) {
        Circle circle = map.addCircle(new CircleOptions().center(new LatLng(latitude, longitude)).radius(radius));
        if(isNewRegion){
            this.currentCircle = circle;
        }
    }

    @Override
    public boolean checkLocation(boolean failedInformed) {
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        regionCreationPresenter.onMapClicked(latLng.latitude, latLng.longitude);
    }

    @Override
    public void addMarker(String name, double latitude, double longitude, boolean draggable) {
        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(draggable).title(name));
    }

    @Override
    public void showRegionNameDialog(IRegionsMapPresenter.RegionDialogListener regionDialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.name_region_title));
        TextInputEditText nameTextInput = new TextInputEditText(this);
        nameTextInput.setHint(getString(R.string.name_label));
        builder.setView(nameTextInput);
        builder.setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> regionDialogListener.onPositiveClicked(nameTextInput.getText().toString()));
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        regionCreationPresenter.onMarkerDragEng(marker.getPosition().latitude, marker.getPosition().longitude);
        currentCircle.setCenter(marker.getPosition());
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        regionCreationPresenter.onMarkerDragStart();
    }

    @Override
    public void toggleSlider(boolean visible) {
        slider.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleCurrentCircle(boolean visible) {
        currentCircle.setVisible(visible);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        regionCreationPresenter.onSliderValueChange(value);
    }

    @Override
    public void setCurrentCircleRadius(float radius) {
        currentCircle.setRadius(radius);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void showIgnoreChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ignore_changes_title));
        builder.setMessage(getString(R.string.ignore_region_changes_msg));
        builder.setPositiveButton(getString(R.string.ignore_label), (dialogInterface, i) -> finish());
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    @Override
    public void toggleSave(boolean visible) {
        menu.findItem(R.id.menuSave).setVisible(visible);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        this.menu = menu;
        regionCreationPresenter.onCreateOptionsMenu();
        menu.findItem(R.id.menuEdit).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            regionCreationPresenter.onBackPressed();
        }else if(id == R.id.menuSave){
            requestSave();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestSave(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(getString(R.string.save_label));
        dialogBuilder.setMessage(getString(R.string.save_confirm_msg));
        dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
        dialogBuilder.setPositiveButton(getString(R.string.save_label), (dialogInterface, i) -> regionCreationPresenter.save());
        dialogBuilder.create().show();;
    }

    @Override
    protected void onStop() {
        try {
            currentLocationUtil.stopLocationUpdates();
            currentLocationUtil.setStopPrintLocation(false);
        }catch (Exception ignored){}
        super.onStop();
    }
}
