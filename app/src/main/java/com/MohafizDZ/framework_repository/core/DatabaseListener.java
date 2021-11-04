package com.MohafizDZ.framework_repository.core;

interface DatabaseListener {
    boolean unAssigneFromModel();
    boolean sortableColumns();
    interface TransactionsListener{
        void onPreInsert(Values values);
        void onPreUpdate(Values values, String selectionConditions);
        void onPreDelete(String selectionConditions);
        void onPostInsert(Values values);
        void onPostUpdate(Values values, String selectionConditions);
        void onPostDelete(String selectionConditions);
    }
}
