package com.MohafizDZ.framework_repository.service;

public class OrderByLine {
    private String field;
    private Direction direction;

    public OrderByLine(String field, Direction direction){
        this.field = field;
        this.direction = direction;
    }

    public String getField() {
        return field;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction{
        DIRECTION_UNSPECIFIED, ASCENDING, DESCENDING
    }
}
