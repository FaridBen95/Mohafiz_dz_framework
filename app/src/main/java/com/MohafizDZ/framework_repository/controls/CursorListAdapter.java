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
 * Created on 8/1/15 12:44 PM
 */
package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CursorListAdapter extends CursorAdapter implements
        AdapterView.OnItemClickListener, SectionIndexer {

    private Integer mLayout = null;
    private LayoutInflater mInflater = null;
    private OnViewCreateListener mOnViewCreateListener = null;
    private HashMap<Integer, OnRowViewClickListener> mViewClickListeners = new HashMap<>();
    private HashMap<String, View> mViewCache = new HashMap<String, View>();
    public Boolean mCacheViews = false;
    private OnViewBindListener mOnViewBindListener = null;
    private BeforeBindUpdateData mBeforeBindUpdateData = null;
    private Context mContext = null;
    private IOnItemClickListener mIOnItemClickListener = null;
    private AbsListView mListView = null;
    private Boolean hasIndexers = false;
    private String mIndexerColumn = null;
    private HashMap<String, Integer> azIndexers = new HashMap<>();
    private String[] sections = new String[0];
    protected boolean mUseBackgroundSelector = true;
    //this should be used when a value change (for example inside EditText)
    private FieldValueChangeListener fieldValueChangeListener;

    public void setFieldValueChangeListener(FieldValueChangeListener fieldValueChangeListener) {
        this.fieldValueChangeListener = fieldValueChangeListener;
    }

    public CursorListAdapter(Context context, Cursor c, int layout, boolean useBackgroundSelector) {
        super(context, c, false);
        mLayout = layout;
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mUseBackgroundSelector = useBackgroundSelector;
    }

    public CursorListAdapter(Context context, Cursor c, int layout) {
        super(context, c, false);
        mLayout = layout;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public CursorListAdapter allowCacheView(Boolean cache) {
        mCacheViews = cache;
        return this;
    }

    public int mSelectedItem = 0;
    public int mSelectedItemId = 0;

    public int getSelectedItem() {
        return mSelectedItem;
    }

    public int getSelectedItemId() {
        return mSelectedItemId;
    }

    public void setSelectedItemId(int selectedItemId) {
        this.mSelectedItemId = selectedItemId;
        this.mViewCache.remove(this.mSelectedItem);
        this.mViewCache.clear();
    }

    public void setSelectedItem(int selectedItem) {
        this.mSelectedItem = selectedItem;
        this.mViewCache.remove(this.mSelectedItem);
        this.mViewCache.clear();
    }


    public View getCachedView(Cursor cr) {
        int pos = cr.getPosition();
        if (mViewCache.size() > pos) {
            return mViewCache.get("view_" + pos);
        }
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final DataRow row = new DataRow();
        for (String col : cursor.getColumnNames()) {
            row.put(col, getValue(cursor, col));
        }
        if (mBeforeBindUpdateData != null) {
            row.addAll(mBeforeBindUpdateData.updateDataRow(cursor));
        }
        if (mOnViewBindListener != null) {
            mOnViewBindListener.onViewBind(view, cursor, row);
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        Cursor cursor = (Cursor) getItem(position);
        view = getCachedView(cursor);
        if (mCacheViews && view != null) {
            return view;
        }
        view = newView(mContext, cursor, (ViewGroup) view);
        final View mView = view;
        for (final int id : mViewClickListeners.keySet()) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mView.findViewById(id) != null) {
                        mView.findViewById(id).setOnClickListener(
                                new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        OnRowViewClickListener listener = mViewClickListeners
                                                .get(id);
                                        Cursor c = getCursor();
                                        c.moveToPosition(position);
                                        listener.onRowViewClick(position, c, v,
                                                mView);
                                    }
                                });
                    }
                }
            }, 100);
        }
        return super.getView(position, view, viewGroup);
    }

    private Object getValue(Cursor c, String column) {
        Object value = false;
        int index = c.getColumnIndex(column);
        switch (c.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                value = false;
                break;
            case Cursor.FIELD_TYPE_BLOB:
            case Cursor.FIELD_TYPE_STRING:
                try {
                    value = c.getString(index);
                } catch (Exception e){
                    value = false;
                }
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                value = c.getFloat(index);
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                value = c.getInt(index);
                break;
        }
        return value;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view;
        if (mCacheViews && getCachedView(cursor) != null) {
            view = getCachedView(cursor);
            if (!view.isDirty()) {
                return view;
            }
        }
        if (mOnViewCreateListener != null) {
            view = mOnViewCreateListener.onViewCreated(context, viewGroup,
                    cursor, cursor.getPosition());
            if (view == null) {
                view = mInflater.inflate(mLayout, viewGroup, false);
            }
        } else
            view = mInflater.inflate(mLayout, viewGroup, false);
        if (mCacheViews) {
            mViewCache.put("view_" + cursor.getPosition(), view);
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (hasIndexers && mIndexerColumn != null) {
            Cursor cr = getCursor();
            if (cr.getCount() > 0) {
                int pos = cr.getCount() - 1;
                if (cr.moveToLast()) {
                    List<String> keys = new ArrayList<>();
                    do {
                        int index = cr.getColumnIndex(mIndexerColumn);
                        if (index > -1) {
                            String colValue = cr.getString(index);
                            azIndexers.put(colValue.substring(0, 1), pos);
                            keys.add(colValue.substring(0, 1));
                        }
                        pos--;
                    } while (cr.moveToPrevious());
                    Collections.sort(keys);
                    sections = keys.toArray(new String[keys.size()]);
                }
            }
        }
    }

    public View inflate(int resource, ViewGroup viewGroup) {
        return mInflater.inflate(resource, viewGroup, false);
    }

    public int getResource() {
        return mLayout;
    }

    public void setResource(Integer layout) {
        mLayout = layout;
    }

    public void setOnRowViewClickListener(int view_id,
                                          OnRowViewClickListener listener) {
        mViewClickListeners.put(view_id, listener);
    }

    public void setOnViewCreateListener(OnViewCreateListener viewCreateListener) {
        mOnViewCreateListener = viewCreateListener;
    }

    public void setOnViewBindListener(OnViewBindListener bindListener) {
        mOnViewBindListener = bindListener;
    }

    public void setBeforeBindUpdateData(BeforeBindUpdateData updater) {
        mBeforeBindUpdateData = updater;
    }

    public interface OnRowViewClickListener {
        public void onRowViewClick(int position, Cursor cursor, View view,
                                   View parent);
    }

    public interface OnViewBindListener {
        public void onViewBind(View view, Cursor cursor, DataRow row);
    }

    public interface BeforeBindUpdateData {
        public DataRow updateDataRow(Cursor cr);
    }

    public interface OnViewCreateListener {
        public View onViewCreated(Context context, ViewGroup view, Cursor cr,
                                  int position);
    }

    public void handleItemClickListener(AbsListView absListView, IOnItemClickListener listener) {
        mIOnItemClickListener = listener;
        mListView = absListView;
        if (mListView != null && mIOnItemClickListener != null) {
            mListView.setOnItemClickListener(this);
        }
    }

    private Boolean mDoubleClick = false;
    private Integer mDoubleClickItemIndex = -1;

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        if (mDoubleClick && mDoubleClickItemIndex == position) {
            mDoubleClick = false;
//            mIOnItemClickListener.onItemDoubleClick(view, position);
        } else {
            mDoubleClick = true;
            mDoubleClickItemIndex = position;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDoubleClick) {
                    mDoubleClick = false;
                    mDoubleClickItemIndex = -1;
                    mIOnItemClickListener.onItemClick(view, position);
                }
            }
        }, 500);
    }

    public void setHasSectionIndexers(boolean hasSectionIndexers, String onColumn) {
        hasIndexers = hasSectionIndexers;
        mIndexerColumn = onColumn;
    }

    // Section Indexers
    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return azIndexers.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return azIndexers.get(sections[position]);
    }

    /**
     * change background of selected list item
     *
     * @param view
     * @param position
     * @author h.Benoudjit
     */
    public void setItemBackground(View view, int position, int itemId, Cursor cursor, int selected_list_item_bg) {

        if (mUseBackgroundSelector) {
            if (itemId == mSelectedItemId) {
                mSelectedItem = position;
                view.setBackgroundResource(selected_list_item_bg);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setElevation(4);
                }
            }
        }
    }

    /**
     * change background of selected list item
     *
     * @param view
     * @param position
     * @author h.Benoudjit
     */
    public void setItemBackground(View view, int position, int itemId, int selected_list_item_bg) {
         if (mUseBackgroundSelector) {
                if (itemId == mSelectedItemId) {
                    mSelectedItem = position;
                    view.setBackgroundResource(selected_list_item_bg);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.setElevation(4);
                    }
                }
            }

    }


    public interface IOnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public interface FieldValueChangeListener{
        void onFieldChanged(String key, Object value);
    }
}
