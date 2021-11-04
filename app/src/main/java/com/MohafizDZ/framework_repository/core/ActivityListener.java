package com.MohafizDZ.framework_repository.core;

public interface ActivityListener {
    String setInfo();
    Class<?> trackActivity();
    interface OnChangeView {
        void openedClass(Class opened);
    }
}
