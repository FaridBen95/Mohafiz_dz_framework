package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyValueAutoComplete extends AutoCompleteTextView implements TextWatcher, AdapterView.OnItemClickListener {
    private static final String TAG = KeyValueAutoComplete.class.getSimpleName();
    public static final String DEFAULT_KEY = "DEFAULT_KEY";
    private final LinkedHashMap<String, String> keyValMap = new LinkedHashMap<>();
    private final Context context;
    private String defaultValue;
    private String defaultKey = DEFAULT_KEY;
    private boolean hasDefaultValue = true;
    private boolean ready;
    private CustomArrayAdapter adapter;
    private SelectionListener selectionListener;
    private boolean initialized;
    private String lastCurrentKey;
    private boolean filterBasedOnSelection = true;

    public void setFilterBasedOnSelection(boolean filterBasedOnSelection) {
        this.filterBasedOnSelection = filterBasedOnSelection;
    }

    public KeyValueAutoComplete(Context context) {
        super(context);
        this.context = context;
    }

    public KeyValueAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public KeyValueAutoComplete(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public KeyValueAutoComplete(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public KeyValueAutoComplete(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, popupTheme);
        this.context = context;
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        initialized = true;
        this.selectionListener = selectionListener;
        addTextChangedListener(this);
        setOnItemClickListener(this);
    }

    public void removeSelectionListener(){
        removeTextChangedListener(this);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setKeyValueMap(LinkedHashMap<String, String> keyValMap, Boolean hasDefaultValue) {
        final ArrayList<String> mNamesList = new ArrayList<>();
        final ArrayList<String> mKeysList = new ArrayList<>();

        this.hasDefaultValue = hasDefaultValue;

        ready = true;

        if(hasDefaultValue) {
            mNamesList.add(defaultValue);
            mKeysList.add(DEFAULT_KEY);
        }

        Iterator<Map.Entry<String, String>> it = keyValMap.entrySet().iterator();
        this.keyValMap.putAll(keyValMap);
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            mNamesList.add(pairs.getValue());
            mKeysList.add(pairs.getKey());
            it.remove(); // avoids a ConcurrentModificationException
        }
        if(!hasDefaultValue){
            defaultValue = mNamesList.get(0);
            defaultKey = mKeysList.get(0);
        }
        adapter = new CustomArrayAdapter(context, mNamesList, mKeysList, filterBasedOnSelection);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        setAdapter(adapter);
    }

    public String getCurrentValue() {
        return getCurrentValue(false);
    }

    public String getCurrentValue(boolean getExactValue) {
        if (ready) {
            return getExactValue? keyValMap.get(getCurrentKey()) : getSelectedItem();
        } else {
            return defaultValue;
        }
    }

    public String getCurrentKey() {
        if (ready) {
            return getSelectedItemKey();
        } else {
            return defaultKey;
        }
    }

    private String getSelectedItemKey(){
        String selectedItem = getSelectedItem();
        String key = adapter.getKey(selectedItem);
        return key != null? key : DEFAULT_KEY;
    }

    public String getSelectedItem(){
        String text = getText().toString().trim();
        if(hasDefaultValue && TextUtils.isEmpty(text)){
            return defaultValue;
        }else{
            return text;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(!initialized){
            return;
        }
        final String currentKey = getCurrentKey();
        final String currentValue = getCurrentValue();
        if(currentKey != null && !currentKey.equals(lastCurrentKey)){
            selectionListener.onSelect(this, currentKey, currentValue, true);
            lastCurrentKey = currentKey;
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String currentKey = getCurrentKey();
        String currentValue = getCurrentValue();
        if(currentKey != null && !currentKey.equals(lastCurrentKey)) {
            selectionListener.onSelect(this, currentKey, currentValue, false);
        }
    }

    private static class CustomArrayAdapter extends ArrayAdapter<String>{
        private final Map<String, String> valuesToKeysMap;
        private final ArrayList<String> items;
        private final boolean filterBasedOnSelection;

        public CustomArrayAdapter(@NonNull Context context, ArrayList<String> namesList, ArrayList<String> keysList, boolean filterBasedOnSelection) {
            super(context, android.R.layout.simple_dropdown_item_1line, namesList);
            this.items = namesList;
            this.filterBasedOnSelection = filterBasedOnSelection;
            valuesToKeysMap = new HashMap<>();
            for(int i = 0; i < namesList.size(); i++){
                valuesToKeysMap.put(namesList.get(i).toLowerCase().trim(), keysList.get(i));
            }
        }

        @NonNull
        @Override
        public Filter getFilter() {
            if(filterBasedOnSelection){
                return super.getFilter();
            }else {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = items;
                        filterResults.count = items.size();
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public CharSequence convertResultToString(Object resultValue) {
                        return resultValue.toString();
                    }
                };
            }
        }

        public String getKey(String value){
            return valuesToKeysMap.get(value.toLowerCase().trim());
        }
    }

    public static interface SelectionListener{
        void onSelect(View view, String key, String value, boolean perTyping);
    }

}
