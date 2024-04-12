/**
 * A key value spinner
 * see example at the buttom of this file
 * @ benyoub mohamed / 20-20/2014
 */

package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.AppCompatSpinner;

import com.MohafizDZ.empty_project.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class KeyValueSpinner extends AppCompatSpinner {

    public static final String DEFAULT_KEY = "DEFAULT_KEY";

    private ArrayList<String> mNamesList = new ArrayList<String>();
    private ArrayList<String> mKeysList = new ArrayList<String>();
    private String defaultValue;
    private Boolean ready = false;
    private Context mContext;
    public Boolean isFirstTrigger = true;
    private Boolean hasDefaultValue = true;

    public KeyValueSpinner(Context context, int mode) {
        super(context, mode);
        mContext = context;
    }

    public KeyValueSpinner(Context context, int mode, OnItemSelectedListener onItemSelectedListener) {
        super(context, mode);
        mContext = context;
        setOnItemSelectedListener(onItemSelectedListener);
    }

    public KeyValueSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public KeyValueSpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(context, attrs, defStyle, mode);
        mContext = context;
    }

    public KeyValueSpinner(Context context) {
        super(context);
        mContext = context;
    }

    public KeyValueSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setDefaultValue(String value) {
        defaultValue = value;
    }

    public void setKeyValueMap(LinkedHashMap<String, String> keyValMap, Boolean hasDefaultValue) {

        mNamesList.clear();
        mKeysList.clear();

        this.hasDefaultValue = hasDefaultValue;

        if(hasDefaultValue) {
            mNamesList.add(defaultValue);
            mKeysList.add(DEFAULT_KEY);
        }

        ready = true;
        List<Map.Entry<String, String>> list = new ArrayList(keyValMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                // Sort by value in ascending order
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Iterator<Map.Entry<String, String>> it = list.iterator();
//        Iterator<Map.Entry<String, String>> it = keyValMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            mNamesList.add(pairs.getValue());
            mKeysList.add(pairs.getKey());
            it.remove(); // avoids a ConcurrentModificationException
        }

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, mNamesList);
        categoriesAdapter.setDropDownViewResource(R.layout.spinner_custom_layout);
        setAdapter(categoriesAdapter);
    }


    public void setKeyValueMap(LinkedHashMap<String, String> keyValMap) {

        mNamesList.clear();
        mKeysList.clear();

        mNamesList.add(defaultValue);
        mKeysList.add(DEFAULT_KEY);
        ready = true;
        Iterator<Map.Entry<String, String>> it = keyValMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            mNamesList.add(pairs.getValue());
            mKeysList.add(pairs.getKey());
            it.remove(); // avoids a ConcurrentModificationException
        }

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, mNamesList);
        categoriesAdapter.setDropDownViewResource(R.layout.spinner_custom_layout);
        setAdapter(categoriesAdapter);
    }

    public void setCurrentKey(String key) {
        Integer position = mKeysList.indexOf(key);
        if (position >= 0)
            setSelection(position);
    }

    public String getCurrentValue() {
        if (ready) {
            Integer position = getSelectedItemPosition();
            return mNamesList.get(position);
        } else {
            return DEFAULT_KEY;
        }
    }


    public String getCurrentKey() {
        if (ready) {
            Integer position = getSelectedItemPosition();
            return mKeysList.get(position);
        } else {

            return hasDefaultValue ?DEFAULT_KEY: mKeysList.get(0);
        }
    }
    public boolean containsKey(String key) {
        return mKeysList.contains(key);
    }

    /**
     * usage example
     *
     * in layout xml use com.dypix.util.KeyValueSpinner class instead of Spinner
     *
     * <com.dypix.util.KeyValueSpinner android:id="@+id/category_spinner"
     * android:layout_width="wrap_content"
     * android:layout_height="match_parent"/>
     *
     * //in java code set your keyValueSpinner public KeyValueSpinner
     * categoriesFilter;
     *
     * //set layout using the casting class KeyValueSpinner categoriesFilter =
     * (KeyValueSpinner) mView.findViewById(R.id.category_spinner);
     *
     * //add default value categoriesFilter.addDefaultValue( "All categories" );
     *
     * //load HashMap key-Value list to spinner categoriesFilter.setKeyValueMap(
     * categoriesMap );
     *
     * //use the value of the spinner, check if not null or default
     * if(categoriesFilter != null && categoriesFilter.getCurrentKey() !=
     * KeyValueSpinner.DEFAULT_KEY) { categoriesFilter.getCurrentKey() }
     */
}