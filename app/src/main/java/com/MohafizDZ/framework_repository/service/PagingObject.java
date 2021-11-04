package com.MohafizDZ.framework_repository.service;

public class PagingObject {
    private int limit;
    private int offset;

    public PagingObject(int limit, int offset){
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public PagingObject setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public PagingObject setOffset(int offset) {
        this.offset = offset;
        return this;
    }
}
