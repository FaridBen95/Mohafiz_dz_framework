package com.MohafizDZ.framework_repository.Utils;

import android.database.sqlite.SQLiteDatabase;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DatabaseObserver;
import com.MohafizDZ.framework_repository.core.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SQLUtil {
    private static HashMap<String, String> sqlCreateStatement = new HashMap<>();
    private static Model model;

    public static void generateCreateStatement(Model model){
        SQLUtil.model = model;
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(model.getModelName());
        sql.append(" (");
        List<Col> columns = model.getColumns();
        sql.append(generateColumnStatement(columns));
        sql.append(")");
        sqlCreateStatement.put(model.getModelName(), sql.toString());
    }

    public static HashMap<String, String> getSqlCreateStatement() {
        return sqlCreateStatement;
    }

    private static String generateColumnStatement(List<Col> cols){
        StringBuffer colStatement = new StringBuffer();
        if(model.sortableColumns()){
            sortColumns(cols);
        }
        List<Col> allColumns = new ArrayList<>();
        List<String> uniqueColumns = new ArrayList<>();
        for(Col col : cols){
            if(!col.isDBColumn()){
                continue;
            }
            if(col.getColumnType().equals(Col.ColumnType.attachement) && !col.isLocal()){
                Col imageSyncedDown = new Col(Col.ColumnType.bool);
                imageSyncedDown.setName(col.getName()+ "_saved_in_local");
                imageSyncedDown.setLocalColumn();
                imageSyncedDown.setDefaultValue(0);
                allColumns.add(imageSyncedDown);
                Col imageSyncedUp = new Col(Col.ColumnType.bool);
                imageSyncedUp.setName(col.getName()+ "_saved_in_server");
                imageSyncedUp.setLocalColumn();
                imageSyncedUp.setDefaultValue(0);
                allColumns.add(imageSyncedUp);
            }
            if(col.isUnique()){
                uniqueColumns.add(col.getName());
            }
            allColumns.add(col);
        }
        for(Col col : allColumns) {
            if(!col.getColumnType().equals(Col.ColumnType.one2many) &&
                    !col.getColumnType().equals(Col.ColumnType.array)) {
                String type = col.getType() + " ";
                String name = col.getName() + " ";
                colStatement.append(name);
                colStatement.append(type);
                if (col.getSize() != 0) {
                    colStatement.append(" (");
                    colStatement.append(col.getSize());
                    colStatement.append(") ");
                }
                if (col.isAutoIncrement()) {
                    colStatement.append(" PRIMARY KEY ");
                    colStatement.append(" AUTOINCREMENT ");
                }
                Object default_value = col.getDefaultValue();
                if (default_value != null && !default_value.toString().equals("")) {
                    colStatement.append(" DEFAULT ");
                    if (default_value instanceof String) {
                        colStatement.append("'").append(default_value).append("'");
                    } else {
                        colStatement.append(default_value);
                    }
                }
                colStatement.append(", ");
            }
            if(col.getColumnType().equals(Col.ColumnType.array)){
                createArrayRelationalTable(model, col);
            }
        }
        colStatement.deleteCharAt(colStatement.lastIndexOf(","));
        for(String uniqueCol : uniqueColumns){
            colStatement.append(", UNIQUE (").append(uniqueCol).append(")");
        }
        return colStatement.toString();
    }

    public static void createArrayRelationalTable(Model model, Col col) {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        String tableName = model.getArrayRelTableName(col);
        sql.append(tableName);
        sql.append(" (");
        sql.append("base_col_id VARCHAR");
        sql.append(", rel_col VARCHAR");
        if(!col.canContainDuplication()) {
            sql.append(", UNIQUE (base_col_id, rel_col)");
        }else{
            sql.append(", UNIQUE (base_col_id) ");
        }
        sql.append(")");
        sqlCreateStatement.put(tableName, sql.toString());
    }

    public static boolean startTransaction(SQLiteDatabase db, DatabaseObserver databaseObserver){
        boolean transactionSuccessful = false;
        db.beginTransaction();
        try {
            transactionSuccessful = databaseObserver.onStartedTransaction(db);
            if(transactionSuccessful){
                db.setTransactionSuccessful();
            }
        }catch (Exception ignored){
            ignored.printStackTrace();
        }finally {
            db.endTransaction();
        }
        return transactionSuccessful;

    }

    private static void sortColumns(final List<Col> cols) {
        Collections.sort(cols, new Comparator<Col>() {
            @Override
            public int compare(Col o1, Col o2) {
                if(o1.getSequence() > o2.getSequence())
                    return 1;
                else if (o1.getSequence() < o2.getSequence())
                    return -1;
                else return 0;
            }
        });
    }



}
