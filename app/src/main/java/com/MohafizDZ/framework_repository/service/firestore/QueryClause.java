package com.MohafizDZ.framework_repository.service.firestore;

public class QueryClause {
    private String fieldName;
    private Operator operator;
    private Object arg;

    public QueryClause(String fieldName, Operator operator, Object arg) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.arg = arg;
    }

    public QueryClause(String fieldName, Operator operator) {
        this.fieldName = fieldName;
        this.operator = operator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getArg() {
        return arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public enum Operator{equalTo, not_equalTo, lessThan, lessOrEqualThan, greaterThan,
        greaterOrEqualThan, whereIn, arrayContains, arrayContainsAny, likeStartWith,
        startAt, endAt, orderAsc, orderDesc
    }
}
