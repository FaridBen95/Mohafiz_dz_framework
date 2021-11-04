/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 30/12/14 5:44 PM
 */
package com.MohafizDZ.framework_repository.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.empty_project.R;

public class FragmentUtils {
    public static final String TAG = FragmentUtils.class.getSimpleName();

    private AppCompatActivity mActivity;
    private Context mContext;
    private Bundle savedInstance = null;
    private FragmentManager fragmentManager;

    public FragmentUtils(AppCompatActivity activity, Bundle savedInstance) {
        mActivity = activity;
        mContext = activity;
        fragmentManager = mActivity.getSupportFragmentManager();
    }

    public static FragmentUtils get(AppCompatActivity activity, Bundle savedInstance) {
        return new FragmentUtils(activity, savedInstance);
    }

    public void startFragment(Fragment fragment, boolean addToBackState, Bundle extra, boolean replace) {
        if(MyAppCompatActivity.onChangeView != null){
            MyAppCompatActivity.onChangeView.openedClass(fragment.getClass());
        }
        Bundle extra_data = fragment.getArguments();
        if (extra_data == null)
            extra_data = new Bundle();
        if (extra != null)
            extra_data.putAll(extra);
        fragment.setArguments(extra_data);
        loadFragment(fragment, addToBackState, replace);
    }

    public void removeFragment(Fragment fragment) {
        if(MyAppCompatActivity.onChangeView != null){
            MyAppCompatActivity.onChangeView.openedClass(null);
        }
        _removeFragment(fragment);
    }

    private void loadFragment(Fragment fragment, Boolean addToBackState) {
        loadFragment(fragment, addToBackState, true);
    }

    private void loadFragment(Fragment fragment, Boolean addToBackState, boolean replace) {
        String tag = fragment.getClass().getCanonicalName();
        if (fragmentManager.findFragmentByTag(tag) != null && savedInstance != null) {
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (savedInstance == null) {
            Log.i(TAG, "Fragment Loaded (" + tag + ")");
            FragmentTransaction tran;
            if(replace) {
                tran = fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, tag);
            }else{
                tran = fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, tag);
            }
            if (addToBackState)
                tran.addToBackStack(tag);
            tran.commitAllowingStateLoss();
        }
    }

    private void _removeFragment(Fragment fragment) {
        String tag = fragment.getClass().getCanonicalName();
        if (fragmentManager.findFragmentByTag(tag) != null && savedInstance != null) {
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (savedInstance == null) {
            Log.i(TAG, "Fragment Loaded (" + tag + ")");
            FragmentTransaction tran;
            tran = fragmentManager.beginTransaction()
                    .remove(fragment);
            tran.commitAllowingStateLoss();
        }
    }

}
