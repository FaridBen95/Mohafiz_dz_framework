package com.MohafizDZ.framework_repository.Utils;

public interface IRowsSingletonSubject {
    void register(IRowsSingletonObserver observer, String tag, String modelName, String id);
    void unregister(IRowsSingletonObserver observer);
    void notifyObservers(String tag);
}
