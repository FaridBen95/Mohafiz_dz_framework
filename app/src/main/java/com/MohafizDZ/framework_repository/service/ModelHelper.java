package com.MohafizDZ.framework_repository.service;

import com.MohafizDZ.framework_repository.core.Model;

import java.util.List;

public class ModelHelper {
    private Model model;
    private SyncingDomain customDomain = null;
    private Boolean canSyncDown = null;
    private Boolean canSyncDownRelations = null;
    private Boolean canSyncUp = null;
    private Boolean canSyncUpRelations = null;
    private Boolean allowDeleteInLocal = null;
    private Boolean allowDeleteOnServer = null;
    private Boolean allowRemoveOutOfDomain = null;
    private Integer limit = null;
    private String likeValue = null;
    private List<String> likeFields = null;
    private OrderBy orderBy = null;
    private PagingObject pagingObject;

    public ModelHelper(Model model) {
        this.model = model;
    }

    public ModelHelper(Model model, SyncingDomain customDomain) {
        this.model = model;
        this.customDomain = customDomain;
    }

    public ModelHelper(Model model, SyncingDomain customDomain, Boolean canSyncDown,
                       Boolean canSyncDownRelations, Boolean canSyncUp, Boolean canSyncUpRelations,
                       Boolean allowDeleteInLocal, Boolean allowDeleteOnServer, Boolean allowRemoveOutOfDomain, Integer limit,
                       String likeValue, List<String> likeFields) {
        this.model = model;
        this.customDomain = customDomain;
        this.canSyncDown = canSyncDown;
        this.canSyncDownRelations = canSyncDownRelations;
        this.canSyncUp = canSyncUp;
        this.canSyncUpRelations = canSyncUpRelations;
        this.allowDeleteInLocal = allowDeleteInLocal;
        this.allowDeleteOnServer = allowDeleteOnServer;
        this.allowRemoveOutOfDomain = allowRemoveOutOfDomain;
        this.limit = limit;
        this.likeValue = likeValue;
        this.likeFields = likeFields;
    }

    public ModelHelper(Model model, SyncingDomain customDomain, Boolean canSyncDown,
                       Boolean canSyncDownRelations, Boolean canSyncUp, Boolean canSyncUpRelations,
                       Boolean allowDeleteInLocal, Boolean allowDeleteOnServer, Boolean allowRemoveOutOfDomain, Integer limit,
                       OrderBy orderBy) {
        this.model = model;
        this.customDomain = customDomain;
        this.canSyncDown = canSyncDown;
        this.canSyncDownRelations = canSyncDownRelations;
        this.canSyncUp = canSyncUp;
        this.canSyncUpRelations = canSyncUpRelations;
        this.allowDeleteInLocal = allowDeleteInLocal;
        this.allowDeleteOnServer = allowDeleteOnServer;
        this.allowRemoveOutOfDomain = allowRemoveOutOfDomain;
        this.limit = limit;
        this.orderBy = orderBy;
    }

    public SyncingDomain getCustomDomain() {
        return customDomain;
    }

    public Boolean isCanSyncDown() {
        return canSyncDown;
    }

    public Boolean isCanSyncDownRelations() {
        return canSyncDownRelations;
    }

    public Boolean isCanSyncUp() {
        return canSyncUp;
    }

    public Boolean isCanSyncUpRelations() {
        return canSyncUpRelations;
    }

    public Boolean isAllowDeleteInLocal() {
        return allowDeleteInLocal;
    }

    public ModelHelper setCustomDomain(SyncingDomain customDomain) {
        this.customDomain = customDomain;
        return this;
    }

    public ModelHelper setCanSyncDown(Boolean canSyncDown) {
        this.canSyncDown = canSyncDown;
        return this;
    }

    public ModelHelper setCanSyncDownRelations(Boolean canSyncDownRelations) {
        this.canSyncDownRelations = canSyncDownRelations;
        return this;
    }

    public ModelHelper setCanSyncUp(Boolean canSyncUp) {
        this.canSyncUp = canSyncUp;
        return this;
    }

    public ModelHelper setCanSyncUpRelations(Boolean canSyncUpRelations) {
        this.canSyncUpRelations = canSyncUpRelations;
        return this;
    }

    public ModelHelper setAllowDeleteInLocal(Boolean allowDeleteInLocal) {
        this.allowDeleteInLocal = allowDeleteInLocal;
        return this;
    }

    public ModelHelper setAllowDeleteOnServer(Boolean allowDeleteOnServer) {
        this.allowDeleteOnServer = allowDeleteOnServer;
        return this;
    }

    public ModelHelper setAllowRemoveOutOfDomain(Boolean allowRemoveOutOfDomain) {
        this.allowRemoveOutOfDomain = allowRemoveOutOfDomain;
        return this;
    }

    public Boolean isAllowDeleteOnServer() {
        return allowDeleteOnServer;
    }

    public Boolean isAllowRemoveOutOfDomain() {
        return allowRemoveOutOfDomain;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getLikeValue() {
        return likeValue;
    }

    public ModelHelper setLikeValue(String likeValue) {
        this.likeValue = likeValue;
        return this;
    }

    public List<String> getLikeFields() {
        return likeFields;
    }

    public ModelHelper setLikeFields(List<String> likeFields) {
        this.likeFields = likeFields;
        return this;
    }

    public Model getModel() {
        return model;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public PagingObject getPagingObject() {
        return pagingObject;
    }

    public ModelHelper setPagingObject(PagingObject pagingObject) {
        this.pagingObject = pagingObject;
        return this;
    }
}
