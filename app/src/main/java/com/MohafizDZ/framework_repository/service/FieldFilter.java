package com.MohafizDZ.framework_repository.service;

import java.util.List;

public class FieldFilter {
    private String filedPath;
    private String operation;
    private Object value;

    public FieldFilter(String filedPath, String operation, Object value){
        this.filedPath = filedPath;
        this.operation = operation;
        this.value = value;
    }

    public static String getOperation(SyncingDomain.Operation operation){
        switch (operation){
            case equalTo:
                return "EQUAL";
            case whereIn:
                return "IN";
            case arrayContainsAny:
                return "ARRAY_CONTAINS_ANY";
            case lessThan:
                return "LESS_THAN";
            case greaterThan:
                return "GREATER_THAN";
            case lessOrEqualThan:
                return "LESS_THAN_OR_EQUAL";
            case greaterOrEqualThan:
                return "GREATER_THAN_OR_EQUAL";
            case arrayContains:
                return "ARRAY_CONTAINS";
        }
        return null;
    }

    private static String getValueType(Object value){
        if(value instanceof List) {
            return "arrayValue";
        }
        if(value instanceof Boolean){
            return "booleanValue";
        }
        if(value instanceof Float || value instanceof Double){
            return "doubleValue";
        }
        if(value instanceof Integer || value instanceof Long){
            return "integerValue";
        }
        if(value instanceof String){
            return "stringValue";
        }
        return "stringValue";
    }

    public String generateFieldFilter(){
        if(value instanceof List){
            List<Object> objects = (List<Object>) value;
            String startQuery = "\"fieldFilter\": {\n" +
                    "              \"field\": {\n" +
                    "                \"fieldPath\": \""+ filedPath +"\"\n" +
                    "              },\n" +
                    "              \"op\": \"" + getOperation() + "\",\n" +
                    "              \"value\": {\n" +
                    "                \"arrayValue\": {\n" +
                    "                  \"values\": [\n";
            String endQuery = "                  ]}\n" +
                    "              }\n" +
                    "            }";
            StringBuilder query = new StringBuilder(startQuery);
            for(Object o : objects){
                query.append("              {");
                String line = "\""+ getValueType(o)+"\": \""+o +"\"\n" ;
                query.append(line);
                query.append("              }");
                query.append(",");
            }
            query.deleteCharAt(query.lastIndexOf(","));
            query.append(endQuery);
            return query.toString();
        }else{
            return "\"fieldFilter\":{" +
                    "\"field\":{" +
                    "\"fieldPath\": \""+ filedPath + "\"" +
                    "}," +
                    "\"op\": \""+ operation + "\"," +
                    "\"value\": {\n" +
                    "\""+ getValueType(value)+"\": \""+value +"\"" +
                    "}" +
                    "}";
        }
    }/*{
        if(value instanceof List){
            List<Object> objects = (List<Object>) value;
            String startQuery = "\"fieldFilter\": {\n" +
                    "              \"field\": {\n" +
                    "                \"fieldPath\": \""+ filedPath +"\"\n" +
                    "              },\n" +
                    "              \"op\": \"IN\",\n" +
                    "              \"value\": {\n" +
                    "                \"arrayValue\": {\n" +
                    "                  \"values\": [\n" +
                    "                    {";
            String endQuery = "}\n" +
                    "                  ]}\n" +
                    "              }\n" +
                    "            }";
            StringBuilder query = new StringBuilder(startQuery);
            for(Object o : objects){
                String line = "\""+ getValueType(o)+"\": \""+o +"\"\n" ;
                query.append(line);
                query.append(",");
            }
            query.deleteCharAt(query.lastIndexOf(","));
            query.append(endQuery);
            return query.toString();
        }else{
            return "\"fieldFilter\": {\n" +
                    "              \"field\": {\n" +
                    "                \"fieldPath\": \""+ filedPath + "\"\n" +
                    "              },\n" +
                    "              \"op\": \""+ operation + "\",\n" +
                    "              \"value\": {\n" +
                    "                \""+ getValueType(value)+"\": \""+value +"\"\n" +
                    "              }\n" +
                    "            }";
        }
    }*/

    public String getFiledPath() {
        return filedPath;
    }

    public FieldFilter setFiledPath(String filedPath) {
        this.filedPath = filedPath;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public FieldFilter setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public FieldFilter setValue(Object value) {
        this.value = value;
        return this;
    }
}
