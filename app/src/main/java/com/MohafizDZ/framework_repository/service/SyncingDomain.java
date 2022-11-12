package com.MohafizDZ.framework_repository.service;

import com.MohafizDZ.framework_repository.Utils.MyUtil;

import java.util.ArrayList;
import java.util.List;

public class SyncingDomain {
    public static final String TAG = SyncingDomain.class.getSimpleName();
    public static final int MAX_WHERE_IN_VALUES_SIZE = 10;

    private List<FilterObject> queries = new ArrayList<>();
    private String selection = "";
    private String[] whereArgs = new String[]{};

    public SyncingDomain(List<FilterObject> queries, String selection, String[] whereArgs) {
        this.queries.clear();
        for(FilterObject filterObject : queries){
            FilterObject _filterObject = new FilterObject();
            _filterObject.setFieldFilters(filterObject.getFieldFilters());
            this.queries.add(_filterObject);
        }
        this.selection = selection;
        this.whereArgs = whereArgs;
    }

    public enum Operation {equalTo, notEqualTo, lessThan, lessOrEqualThan, greaterThan,
        greaterOrEqualThan, whereIn, arrayContains, arrayContainsAny}

    public SyncingDomain(){
    }

    public SyncingDomain addOperation(String field, Operation operation, Object value) {
        return addOperation(field, operation, value, true);
    }

    public SyncingDomain addOperation(String field, Operation operation, Object value, boolean active) {
        FilterObject filterObject = queries.size() == 0? new FilterObject() :
                queries.get(queries.size() - 1);
        FieldFilter fieldFilter = new FieldFilter(field, FieldFilter.getOperation(operation), value);
        filterObject.add(fieldFilter);
        if(active && !selection.equals("")) {
            selection += "and ";
        }
        if(active){
            if(value instanceof List){
                List<Object> objects = (List<Object>) value;
                for (int i = 0; i < objects.size(); i++) {
                    whereArgs = MyUtil.addArgs(whereArgs, String.valueOf(objects.get(i)));
                }
            }else{
                whereArgs = MyUtil.addArgs(whereArgs, String.valueOf(value));
            }
        }
        switch (operation){
            case equalTo:
                if(active){
                    selection += field + " = ? ";
                }
                break;
            case notEqualTo:
                if(active){
                    selection += field + " <> ? ";
                }
                break;
            case greaterOrEqualThan:
                if(active){
                    selection += field + " >= ? ";
                }
                break;
            case greaterThan:
                if(active){
                    selection += field + " > ? ";
                }
                break;
            case lessOrEqualThan:
                if(active){
                    selection += field + " <= ?  ";
                }
                break;
            case lessThan:
                if(active){
                    selection += field + " < ? ";
                }
                break;
            case whereIn:
                List<Object> objects = (List<Object>) value;
                if(active){
                    selection += field + " in (" + MyUtil.repeat("?,", objects.size() - 1) + " ?) ";
                }
                break;
        }
        int index = queries.size() -1 >= 0? queries.size()-1 : 0;
        if(queries.size() == 0){
            queries.add(filterObject);
        }else{
            queries.set(index, filterObject);
        }
        return this;
    }

    public SyncingDomain orDomain(SyncingDomain domain) {
        return orDomain(domain, true);
    }

    public SyncingDomain orDomain(SyncingDomain domain, boolean active){
        this.queries.add(domain.getFilterObject());
        if(active){
            this.selection += (!selection.equals("")? " or ": "")+  "(" + domain.selection + ")";
            for(String arg : domain.whereArgs){
                this.whereArgs = MyUtil.addArgs(this.whereArgs, arg);
            }
        }
        return this;
    }

    public SyncingDomain(FilterObject filterObject, String selection, String[] whereArgs){
        this();
        this.queries.add(filterObject);
        this.selection = selection;
        this.whereArgs = whereArgs;
    }

    public SyncingDomain(SyncingDomain SyncingDomain, boolean active){
        this();
        this.queries.add(SyncingDomain.getFilterObject());
        if(active) {
            this.selection = SyncingDomain.selection ;
            this.whereArgs = SyncingDomain.whereArgs;
        }
    }

    public FilterObject getFilterObject() {
        return queries.get(0);
    }

    public List<FilterObject> getFilterObjects() {
        return queries;
    }

    public class DomainErrorException extends Exception{
        public DomainErrorException(String message){
            super(message);
        }
    }

    public String getSelection() {
        return selection;
    }

    public String[] getWhereArgs() {
        return whereArgs;
    }

    public SyncingDomain copy(){
        return new SyncingDomain(queries, selection, whereArgs);
    }

    public SyncingDomain addWhereInOperation(String field, List<String> ids) {
        return addWhereInOperation(field, ids, true);
    }

    public SyncingDomain addWhereInOperation(String field, List<String> ids, boolean active){
        SyncingDomain whereInDomain = copy();
        if(ids.size() < MAX_WHERE_IN_VALUES_SIZE){
            whereInDomain.addOperation(field, Operation.whereIn, ids, active );
        }else{

            for(int i = 0; i < ids.size(); i = i+MAX_WHERE_IN_VALUES_SIZE){
                SyncingDomain _domain = copy();
                int toIndex = Math.min(ids.size(), i + MAX_WHERE_IN_VALUES_SIZE);
                List<String> subIds = ids.subList(i, toIndex);
                _domain.addOperation(field, Operation.whereIn, subIds, active);
                whereInDomain.orDomain(_domain);
            }
        }
        return whereInDomain;
    }

    public SyncingDomain addFieldContainsAny(String field, List<String> ids) {
        return addFieldContainsAny(field, ids, true);
    }

    public SyncingDomain addFieldContainsAny(String field, List<String> ids, boolean active){
        SyncingDomain whereInDomain = copy();
        if(ids.size() < MAX_WHERE_IN_VALUES_SIZE){
            whereInDomain.addOperation(field, Operation.arrayContainsAny, ids, active );
        }else{

            for(int i = 0; i < ids.size(); i = i+MAX_WHERE_IN_VALUES_SIZE){
                SyncingDomain _domain = copy();
                int toIndex = Math.min(ids.size(), i + MAX_WHERE_IN_VALUES_SIZE);
                List<String> subIds = ids.subList(i, toIndex);
                _domain.addOperation(field, Operation.arrayContainsAny, subIds, active);
                whereInDomain.orDomain(_domain);
            }
        }
        return whereInDomain;
    }
}

