package com.MohafizDZ.framework_repository.service.functions;

import java.util.ArrayList;
import java.util.List;

public class WhereCondition {
    private List<_WhereCondition> whereConditionList;

    public WhereCondition(){
        whereConditionList = new ArrayList<>();
    }

    public WhereCondition add(String field, Operator operator, Object value){
        whereConditionList.add(new _WhereCondition(field, operator, value));
        return this;
    }

    public enum Operator {
        EQUALS("=="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL_TO("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL_TO(">="),
        //max of 30 elements in the where in or contains any
        WHERE_IN("in"),
        //max of 10 elements in the where not in
        WHERE_NOT_IN("not-in"),
        ARRAY_CONTAINS("array-contains"),
        ARRAY_CONTAINS_ANY("array-contains-any");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
