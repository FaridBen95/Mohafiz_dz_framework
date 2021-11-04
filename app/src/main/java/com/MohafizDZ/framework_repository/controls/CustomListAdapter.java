package com.MohafizDZ.framework_repository.controls;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomListAdapter extends ArrayAdapter<Object>{

    private Context mContext = null;
    private List<Object> mObjects = null;
    private List<Object> mAllObjects = null;
    private RowFilter mFilter = null;
    private int mResourceId = 0;
    private RowFilterTextListener mRowFilterTextListener = null;
    private OnSearchChange mOnSearchChange = null;
    private ListViewGenerator viewGenerator;
    private Activity activity;
    //this should be used when a value change (for example inside EditText)
    private FieldValueChangeListener fieldValueChangeListener;

    public void setFieldValueChangeListener(FieldValueChangeListener fieldValueChangeListener) {
        this.fieldValueChangeListener = fieldValueChangeListener;
    }

    private CustomListAdapter(Context context, int resource, List<Object> objects) {
        super(context, 0, objects);
        mContext = context;
        mObjects = new ArrayList<>(objects);
        mAllObjects = new ArrayList<>(objects);
        mResourceId = resource;
    }

    public CustomListAdapter(Activity activity, int ressource, List<Object> objects, ListViewGenerator viewGenerator){
        this(activity, ressource, objects);
        this.activity = activity;
        this.viewGenerator = viewGenerator;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View mView, @NonNull ViewGroup parent) {
        View v = mView;
        if (v == null)
            v = activity.getLayoutInflater().inflate(getResource(), parent, false);
        if(viewGenerator != null){
            viewGenerator.generateView(v, position);
        }
        return v;
    }

    public List<Object> getObjects(){
        return mObjects;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new RowFilter();
        }
        return mFilter;
    }

    public int getResource() {
        return mResourceId;
    }

    public void replaceObjectAtPosition(int position, Object object) {
        mAllObjects.remove(position);
        mAllObjects.add(position, object);
        mObjects.remove(position);
        mObjects.add(position, object);
    }

    public void notifiyDataChange(List<Object> objects) {
        mAllObjects.clear();
        mObjects.clear();
        mAllObjects.addAll(objects);
        mObjects.addAll(objects);
        notifyDataSetChanged();
    }

    class RowFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            if (!TextUtils.isEmpty(constraint)) {
                String searchingStr = constraint.toString().toLowerCase();
                List<Object> filteredItems = new ArrayList<Object>();
                for (Object item : mAllObjects) {
                    String filterText = "";
                    if (mRowFilterTextListener != null) {
                        filterText = mRowFilterTextListener.filterCompareWith(
                                item).toLowerCase();
                    } else {
                        filterText = item.toString().toLowerCase();
                    }
                    if (filterText.contains(searchingStr)) {
                        filteredItems.add(item);
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;

            } else {
                synchronized (this) {
                    result.count = mAllObjects.size();
                    result.values = mAllObjects;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            clear();
            mObjects = (List<Object>) results.values;
            try {
                addAll(mObjects);
                notifyDataSetChanged();
                if (mOnSearchChange != null) {
                    mOnSearchChange.onSearchChange(mObjects);
                }
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public void setOnSearchChange(OnSearchChange callback) {
        mOnSearchChange = callback;
    }

    public void setRowFilterTextListener(RowFilterTextListener listener) {
        mRowFilterTextListener = listener;
    }

    public interface RowFilterTextListener {

        public String filterCompareWith(Object object);
    }

    public interface OnSearchChange {

        public void onSearchChange(List<Object> newRecords);
    }

    public interface ListViewGenerator{
        void generateView(View view, int position);
    }

    public interface FieldValueChangeListener{
        void onFieldChanged(String key, Object value);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }
}
