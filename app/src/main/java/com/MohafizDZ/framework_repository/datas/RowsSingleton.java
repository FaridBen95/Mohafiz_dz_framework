package com.MohafizDZ.framework_repository.datas;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.Utils.IRowsSingletonObserver;
import com.MohafizDZ.framework_repository.Utils.IRowsSingletonSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Triple;

public class RowsSingleton {

    private static Rows rows;

    private RowsSingleton(){}

    public static Rows get(){
        if(rows == null) rows = new Rows();
        return rows;
    }

    public static void reset(){
        rows = null;
    }

    public static class Rows implements IRowsSingletonSubject {
        private Map<Pair<String, String>, DataRow> sRows;
        private Map<Triple<String, String, String>, IRowsSingletonObserver> observerMap;

        private Rows(){
            sRows = new HashMap<>();
            observerMap = new HashMap<>();
        }

        public void put(String modelName, String id, DataRow row){
            Pair<String, String> pair = new Pair<>(modelName, id);
            sRows.put(pair, row);
        }

        public DataRow get(String modelName, String id){
            Pair<String, String> pair = new Pair<>(modelName, id);
            return sRows.get(pair);
        }

        public void remove(String modelName, String id){
            Pair<String, String> pair = new Pair<>(modelName, id);
            sRows.remove(pair);
        }

        public List<DataRow> get(String modelName){
            List<DataRow> rows = new ArrayList<>();
            for(Pair<String, String> key : sRows.keySet()){
                if(key.first.equals(modelName)){
                    rows.add(sRows.get(key));
                }
            }
            return rows;
        }

        public void clear(){
            sRows.clear();
        }

        @Override
        public void register(IRowsSingletonObserver observer, String tag, String modelName, String id) {
            observerMap.put(new Triple<>(tag, modelName, id), observer);
        }

        @Override
        public void unregister(IRowsSingletonObserver observer) {
            observerMap.remove(observer);
        }

        @Override
        public void notifyObservers(String tag) {
            Map<Pair<String, String>, DataRow> rows = sRows;
            for(Pair<String, String> key : rows.keySet()){
                Triple<String, String, String> observerKey = new Triple<>(tag, key.first, key.second);
                if(observerMap.containsKey(observerKey)){
                    IRowsSingletonObserver observer = observerMap.get(observerKey);
                    if(observer != null) {
                        observer.onValuesUpdated(rows.get(key));
                    }
                }
            }
        }
    }
}
