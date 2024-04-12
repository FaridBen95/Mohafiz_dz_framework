package com.MohafizDZ.framework_repository.service.functions;

class _WhereCondition {
    public static final String TAG = _WhereCondition.class.getSimpleName();

    private final String field;
    private final WhereCondition.Operator operator;
    private final Object value;

    public _WhereCondition(String field, WhereCondition.Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator.getSymbol();
    }

    public Object getValue() {
        return value;
    }
}
