package com.jajuka.derp;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by austinh on 10/18/14.
 */
public class DataBinding<Type> {
    private Type mValue = null;
    private final CopyOnWriteArraySet<Observer<Type>> mObservers = new CopyOnWriteArraySet<Observer<Type>>();

    public DataBinding() { }

    public DataBinding(Type value) {
        mValue = value;
    }

    public void set(Type value) {
        mValue = value;
        notifyObservers();
    }

    public Type get() {
        return mValue;
    }

    public void update() {
        notifyObservers();
    }

    public java.lang.reflect.Type getBoundType() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private void notifyObservers() {
        for (Observer<Type> observer : mObservers) {
            observer.onChange(this, mValue);
        }
    }

    /* package */ void registerObserver(Observer observer) {
        mObservers.add(observer);
    }

    /* package */ interface Observer<Type> {
        void onChange(DataBinding<Type> dataBinding, Type value);
    }
}
