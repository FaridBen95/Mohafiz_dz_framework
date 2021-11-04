package com.MohafizDZ.framework_repository.service;

import java.util.ArrayList;
import java.util.List;

public class FilterObject {
    private List<FieldFilter> fieldFilters = new ArrayList<>();

    public List<FieldFilter> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(List<FieldFilter> fieldFilters) {
        this.fieldFilters.clear();
        for(FieldFilter fieldFilter : fieldFilters){
            this.fieldFilters.add(new FieldFilter(fieldFilter.getFiledPath(),
                    fieldFilter.getOperation(), fieldFilter.getValue()));
        }
    }

    public boolean add(FieldFilter fieldFilter) {
        return fieldFilters.add(fieldFilter);
    }

    public void add(int index, FieldFilter element) {
        fieldFilters.add(index, element);
    }

    public String generateCompositeFilter(){
        if(fieldFilters.size() == 0){
            return null;
        }else if(fieldFilters.size() == 1){
            return fieldFilters.get(0).generateFieldFilter();
        }else{
            String startQuery = "\"compositeFilter\": {" +
                    "\"op\": \"AND\"," +
                    "\"filters\": [" +
                    "{";
            String endQuery = "" +
                    "]" +
                    "}";
            StringBuilder query = new StringBuilder(startQuery);
            for(int i = 0 ; i < fieldFilters.size(); i++){
                FieldFilter fieldFilter = fieldFilters.get(i);
                query.append(fieldFilter.generateFieldFilter());
                query.append("}");
                if(i != fieldFilters.size() - 1){
                    query.append(",{");
                }
            }
            query.append(endQuery);
            return query.toString();
        }
    }/*{
        if(fieldFilters.size() == 0){
            return null;
        }else if(fieldFilters.size() == 1){
            return fieldFilters.get(0).generateFieldFilter();
        }else{
            String startQuery = "\"compositeFilter\": {\n" +
                    "        \"op\": \"AND\",\n" +
                    "        \"filters\": [\n" +
                    "          {";
            String endQuery = "\n" +
                    "        ]\n" +
                    "      }";
            StringBuilder query = new StringBuilder(startQuery);
            for(int i = 0 ; i < fieldFilters.size(); i++){
                FieldFilter fieldFilter = fieldFilters.get(i);
                query.append(fieldFilter.generateFieldFilter());
                query.append("}");
                if(i != fieldFilters.size() - 1){
                    query.append(",{");
                }
            }
            query.append(endQuery);
            return query.toString();
        }
    }*/

    public Boolean isComposite(){
        if(fieldFilters.size() == 0){
            return null;
        }else if(fieldFilters.size() == 1){
            return false;
        }else{
            return true;
        }
    }
}
