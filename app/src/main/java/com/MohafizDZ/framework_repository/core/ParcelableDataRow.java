package com.MohafizDZ.framework_repository.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Set;

public class ParcelableDataRow implements Parcelable {
    private String modelName;
    private HashMap<String, Object> hashMap;

    // Constructor
    public ParcelableDataRow(){
        this.hashMap = new HashMap<>();
        this.modelName = "";
    }

    public ParcelableDataRow(ParcelableDataRow parcelableDataRow){
        this.hashMap = parcelableDataRow.getHashMap();
        this.modelName = parcelableDataRow.getModelName();
    }

    private ParcelableDataRow(HashMap<String, Object> hashMap, String modelName){
        this.hashMap = hashMap;
        this.modelName = modelName;
    }

    // Getter method for the HashMap
    public HashMap<String, Object> getHashMap() {
        return hashMap;
    }

    // Parcelable implementation

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(hashMap);
        dest.writeString(modelName);
    }

    public static final Creator<ParcelableDataRow> CREATOR = new Creator<ParcelableDataRow>() {
        @Override
        public ParcelableDataRow createFromParcel(Parcel in) {
            HashMap<String, Object> hashMap = in.readHashMap(Object.class.getClassLoader());
            String modelName = in.readString();
            return new ParcelableDataRow(hashMap, modelName);
        }

        @Override
        public ParcelableDataRow[] newArray(int size) {
            return new ParcelableDataRow[size];
        }
    };


    public void putAll(DataRow row) {
        hashMap.putAll(row.getHashMap());
    }

    public boolean containsKey(String colName) {
        return hashMap.containsKey(colName);
    }

    public Set<String> keySet(){
        return hashMap.keySet();
    }

    public Object get(String key) {
        return hashMap.get(key);
    }

    public void put(String colName, Object relRow) {
        hashMap.put(colName, relRow);
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}