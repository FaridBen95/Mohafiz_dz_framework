package com.MohafizDZ.framework_repository.core;

import java.util.HashMap;

public class SQLitesListSingleton {

    public HashMap<String, MySqlite> sqlites = new HashMap<String, MySqlite>();

    private static SQLitesListSingleton sqLitesListSingleton;

    private SQLitesListSingleton(){
    }

    public static SQLitesListSingleton getSQLiteList(){
        if(sqLitesListSingleton == null){
            sqLitesListSingleton = new SQLitesListSingleton();
        }
        return sqLitesListSingleton;
    }
}
