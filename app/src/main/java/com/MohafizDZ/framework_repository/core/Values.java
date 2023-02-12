package com.MohafizDZ.framework_repository.core;

import android.content.ContentValues;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.Utils.MyUtil;

import java.io.File;
import java.util.HashMap;

public class Values extends HashMap<String, Object> {

    public ContentValues toContentValues(){
        ContentValues contentValues = new ContentValues();
        for(String key : keySet()){
            Object val = get(key);
            if (val instanceof byte[]) {
                contentValues.put(key, (byte[]) val);
            } else if (val != null) {
                contentValues.put(key, val.toString());
            }
        }
        return contentValues;
    }

    public Values getValuesFrom(ContentValues contentValues){
        Values values = new Values();
        for(String key : contentValues.keySet()){
            values.put(key, contentValues.get(key));
        }
        return values;
    }

    public Values addAll(Values values){
        this.putAll(values);
        return this;
    }

    public DataRow toDataRow() {
        DataRow row = new DataRow();
        for(String key : keySet()){
            row.put(key, get(key));
        }
        return row;
    }

    public void putM2o(String key, DataRow relationRow){
        this.put(key, relationRow.getString(Col.SERVER_ID));
    }

    public void putIcon(String key, File icon){
        this.put(key, Base64.encodeToString(MyUtil.bytesFromFile(icon), Base64.DEFAULT));
    }

    public void putImage(String key, File image){
        this.put(key, image.getName());
    }
}
