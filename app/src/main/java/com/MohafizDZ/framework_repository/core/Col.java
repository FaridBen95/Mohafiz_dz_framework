package com.MohafizDZ.framework_repository.core;

import java.lang.reflect.Field;

public class Col {
    public static String ROWID = "_id";
    public static String LOCAL_WRITE_DATE = "_write_date";
    public static String ENABLED = "enabled";
    public static String REMOVED = "removed";
    public static String SERVER_ID = "id";
    public static String SYNCED = "synced";


    private String currentModel;
    private boolean mUnique;

    public enum ColumnType {
        varchar, text, integer, real, bool, one2many, many2one, attachement, low_quality_image, array
    }
    private ColumnType columnType;
    private String type;
    private String name;
    private int size = 0;
    private Class relationalModel;
    private boolean autoIncrement;
    private Object defaultValue;
    private int sequence;
    private String relatedColumn;
    private boolean is_local;
    private boolean canSyncDownRelations = true;
    private boolean canSyncUpCol = true;
    private boolean canInsertOnServer = false;

    public int getSequence() {
        return sequence;
    }

    public Col setSequence(int sequence) {
        this.sequence = sequence;
        return this;
    }


    public Col(){
        this(ColumnType.text);
    }

    public Col(ColumnType columnType){
        this.columnType = columnType;
        setType(columnType);
    }

    public Col(ColumnType columnType, Class relationalModel){
        this.columnType = columnType;
        setType(columnType);
        this.relationalModel = relationalModel;
        try{
            throw new Exception("the caller is");
        }
        catch (Exception e){
            currentModel = e.getStackTrace()[0].getFileName().replace(".java","");
        }

    }

    public Col getColumn(Field field){
        try {
            return (Col) field.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Col setDefaultValue(Object defaultValue){
        this.defaultValue = defaultValue;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(String currentModel) {
        this.currentModel = currentModel;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getRelationalModel() {
        return relationalModel;
    }

    public Col setRelationalModel(Class relationalModel) {
        this.relationalModel = relationalModel;
        return this;
    }

    public void setType(ColumnType columnType) {
        String type = "TEXT";
        Object defaultValue = "";
        switch (columnType){
            case bool:
                type = "BOOLEAN";
                defaultValue = 0;
                break;
            case real:
                type = "REAL";
                defaultValue = 0;
                break;
            case varchar:
            case many2one:
                type = "VARCHAR";
                break;
            case integer:
                type = "INTEGER";
                defaultValue = 0;
                break;
            case attachement:
            case low_quality_image:
            case text:
                type = "TEXT";
                break;
        }
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Col setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public Col setSize(int size){
        this.size = size;
        return this;
    }

    public int getSize() {
        return size;
    }

    public String getRelatedColumn() {
        return relatedColumn;
    }

    public Col setRelatedColumn(String relatedColumn) {
        this.relatedColumn = relatedColumn;
        return this;
    }

    public Col setLocalColumn(){
        this.is_local = true;
        return  this;
    }

    public boolean isLocal(){
        return this.is_local;
    }

    public Col setCanSyncDownRelations(boolean canSyncDownRelations) {
        this.canSyncDownRelations = canSyncDownRelations;
        return this;
    }

    public boolean canSyncRelations() {
        return canSyncDownRelations;
    }

    public Col setCanSyncUpCol(boolean can){
        canSyncUpCol = can;
        return this;
    }

    public boolean canSyncUpCol() {
        return canSyncUpCol;
    }

    public boolean canInsertOnServer() {
        return canInsertOnServer;
    }

    public Col setCanInsertOnServer(boolean canInsertOnServer) {
        this.canInsertOnServer = canInsertOnServer;
        return this;
    }

    public boolean isUnique() {
        return mUnique;
    }

    public Col setUnique() {
        this.mUnique = true;
        return this;
    }
}
