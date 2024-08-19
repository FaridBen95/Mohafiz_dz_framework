package com.MohafizDZ.project.customer_category_form_dir;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;

public class CustomerCategoryFormActivity extends MyAppCompatActivity implements ICategoryFormPresenter.View{
    private static final String TAG = CustomerCategoryFormActivity.class.getSimpleName();
    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.customer_category);
    }
}
