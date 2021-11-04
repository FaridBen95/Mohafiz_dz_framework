package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.MohafizDZ.App;
import java.util.HashMap;
import java.util.List;

public class DataRow extends HashMap<String, Object> implements Parcelable{

    private String modelName;

    public DataRow(){

    }

    protected DataRow(Parcel in) {
    }

    public static final Creator<DataRow> CREATOR = new Creator<DataRow>() {
        @Override
        public DataRow createFromParcel(Parcel in) {
            return new DataRow(in);
        }

        @Override
        public DataRow[] newArray(int size) {
            return new DataRow[size];
        }
    };

    public DataRow addAll(DataRow row){
        this.putAll(row);
        return this;
    }

    public DataRow getRelRow(Context context, Col relColumn, String colName){
        if(this.containsKey(colName)) {
            Model relModel = Model.createInstance(context, relColumn.getRelationalModel());
            return relModel.browse(this.getString(colName));
        }
        return null;
    }

    public List<DataRow> getO2MRows(Context context, Col relColumn, String relatedField){
        String selection = " " + relatedField + " = ? ";
        String[] selectionArgs = {this.getString(Col.SERVER_ID) + ""};
        Model relModel = Model.createInstance(context, relColumn.getRelationalModel());
        return relModel.getRows(selection, selectionArgs);
    }

    public List<String> getRelArray(Model model, String field){
        return model.getRelArray(this, field);
    }

    public List<String> getRelArray(Model model, Col col){
        return model.getRelArray(this, col);
    }

    public Values toValues(){
        Values values = new Values();
        for(String key : keySet()){
            values.put(key, get(key));
        }
        return values;
    }

    public Integer getInteger(String field){
        try {
            return Integer.valueOf(this.get(field).toString());
        }catch (Exception ignored){}
        return 0;
    }

    public Float getFloat(String field){
        try {
            return Float.valueOf(this.get(field).toString());
        }catch (Exception ignored){}
        return 0.0f;
    }

    public Boolean getBoolean(String field){
        if(this.containsKey(field)){
            try {
                if(this.get(field) instanceof Integer) {
                    return this.getInteger(field) == 1;
                }else if (this.get(field) instanceof Boolean){
                    return (Boolean) this.get(field);
                }
                Integer integer = Integer.valueOf(String.valueOf(this.get(field)));
                return integer == 1;
            }catch (Exception e){
                return Boolean.valueOf(String.valueOf(this.get(field)));
            }
        }
        return null;
    }

    public String getString(String field){
        return this.get(field).toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public DataRow getRelRow(String colName) {
        colName = "#".concat(colName);
        return (DataRow) get(colName);
    }

    public List<String> getRelStringList(String colName){
        colName = containsRelKey(colName)? "#".concat(colName) : colName;
//        colName = "#".concat(colName);
        try {
            return (List<String>) get(colName);
        }catch (Exception ignored){}
        return null;
    }

    public List<DataRow> getRelRowList(String colName){
        colName = "#".concat(colName);
        return (List<DataRow>) get(colName);
    }

    public DataRow putRel(String colName, Object relRow){
        colName = "#".concat(colName);
        put(colName, relRow);
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public boolean containsRelKey(String colName) {
        colName = "#".concat(colName);
        return containsKey(colName);
    }
}
