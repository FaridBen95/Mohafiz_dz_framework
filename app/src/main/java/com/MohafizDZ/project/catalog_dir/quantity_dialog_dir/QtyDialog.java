package com.MohafizDZ.project.catalog_dir.quantity_dialog_dir;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.product_form_dir.ProductFormActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class QtyDialog implements IQtyDialogPresenter.View, IQtyDialogPresenter.Dialog, TextWatcher {
    private static final String TAG = QtyDialog.class.getSimpleName();
    private final IQtyDialogPresenter.DialogListener dialogListener;
    private final ProductRow productRow;
    private float qty;
    private Float availability, unitPrice;
    private MyAppCompatActivity activity;
    private IQtyDialogPresenter.Presenter presenter;
    private IQtyDialogPresenter.PresenterType presenterType;
    private MaterialAlertDialogBuilder builder;
    private ImageView imageView;
    private View availabilityContainer, totalPriceContainer, unitPriceContainer;
    private TextView titleTextView, availabilityTextView, unitPriceTextView, totalPriceTextView, viewDetailsButton;
    private TextInputEditText qtyTextInput;

    public QtyDialog(IQtyDialogPresenter.DialogListener dialogListener, ProductRow productRow, float qty) {
        this(dialogListener, productRow, qty, IQtyDialogPresenter.PresenterType.selectQty);
    }

    public QtyDialog(IQtyDialogPresenter.DialogListener dialogListener, ProductRow productRow, float qty, IQtyDialogPresenter.PresenterType presenterType) {
        this.dialogListener = dialogListener;
        this.productRow = productRow;
        this.qty = qty;
        this.presenterType = presenterType;
    }


    @Override
    public void setAvailability(float availability) {
        this.availability = availability;
    }

    @Override
    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public void showDialog(MyAppCompatActivity activity) {
        this.activity = activity;
        initPresenter();
        builder = new MaterialAlertDialogBuilder(activity);
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        View convView = activity.getLayoutInflater().inflate(R.layout.product_dialog, container, false);
        container.addView(convView);
        builder.setView(container);
        builder.setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) ->
                dialogListener.onPositiveClicked(productRow, getEnteredQty()));
        builder.setNeutralButton(getString(R.string.delete_label), (dialogInterface, i) ->
                dialogListener.onNeutralClicked(productRow, 0.0f));
        builder.setNegativeButton(getString(R.string.cancel_label), (dialogInterface, i) ->
                dialogListener.onNegativeClicked(productRow, getEnteredQty()));
        findViewById(convView);
        setControls();
        presenter.setAvailability(availability);
        presenter.setUnitPrice(unitPrice);
        presenter.onViewCreated();
    }

    protected void initPresenter() {
        switch (presenterType){
            case saleQty:
                presenter = new SaleQtyDialogPresenterImpl(this, activity, productRow, qty, false);
            case backOrderQty:
                presenter = new SaleQtyDialogPresenterImpl(this, activity, productRow, qty, true);
            case selectQty:
                presenter = new SelectQtyDialogPresenterImpl(this, activity, productRow, qty);
        };
    }

    private void setControls() {
        viewDetailsButton.setOnClickListener(view -> openProductDetails());
        qtyTextInput.addTextChangedListener(this);
    }

    private void openProductDetails(){
        Intent intent = ProductFormActivity.getIntent(activity, productRow.getString(Col.SERVER_ID), false);
        IntentUtils.startActivity(activity, intent);
    }

    private float getEnteredQty() {
        try {
            return Float.valueOf(qtyTextInput.getText().toString());
        }catch (Exception ignored){
            return 0.0f;
        }
    }

    private void findViewById(View convView) {
        qtyTextInput = convView.findViewById(R.id.qtyTextInput);
        availabilityTextView = convView.findViewById(R.id.availabilityTextView);
        unitPriceTextView = convView.findViewById(R.id.unitPriceTextView);
        totalPriceTextView = convView.findViewById(R.id.totalPriceTextView);
        viewDetailsButton = convView.findViewById(R.id.viewDetailsButton);
        titleTextView = convView.findViewById(R.id.titleTextView);
        availabilityContainer = convView.findViewById(R.id.availabilityContainer);
        unitPriceContainer = convView.findViewById(R.id.unitPriceContainer);
        totalPriceContainer = convView.findViewById(R.id.totalPriceContainer);
        imageView = convView.findViewById(R.id.imageView);
    }

    private String getString(int resId){
        return activity.getString(resId);
    }

    @Override
    public void showToast(String msg) {
        activity.showToast(msg);
    }

    @Override
    public void showSimpleDialog(String title, String msg) {
        activity.showSimpleDialog(title, msg);
    }

    @Override
    public boolean inNetwork() {
        return activity.inNetwork();
    }

    @Override
    public void toggleLoading(boolean isRefreshing) {
        activity.toggleLoading(isRefreshing);
    }

    @Override
    public void setToolbarTitle(String title) {
        builder.setTitle(title);
    }

    @Override
    public void show() {
        builder.create().show();
    }

    @Override
    public void setProductName(String name) {
        titleTextView.setText(name);
    }

    @Override
    public void toggleAvailability(boolean visible) {
        availabilityContainer.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void setQuantity(String qtyText) {
        qtyTextInput.setText(qtyText);
    }

    @Override
    public void togglePrice(boolean visible) {
        unitPriceContainer.setVisibility(getViewVisibility(visible));
        totalPriceContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleTotalPrice(boolean visible) {
        totalPriceContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setTotalPrice(String text) {
        totalPriceTextView.setText(text);
    }

    @Override
    public void setUnitPrice(String text) {
        unitPriceTextView.setText(text);
    }

    @Override
    public void setAvailability(String text) {
        availabilityTextView.setText(text);
    }

    @Override
    public void setProductImage(String base64) {
        Bitmap bitmap = BitmapUtils.getBitmapImage(activity, base64);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(availability != null){
            float qty;
            try {
                qty = Float.valueOf(qtyTextInput.getText().toString());
            }catch (Exception ignored){
                qty = 0.0f;
            }
            if(qty > availability){
                qtyTextInput.setText(availability + "");
                showToast(getString(R.string.quantity_exceed_msg));
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}