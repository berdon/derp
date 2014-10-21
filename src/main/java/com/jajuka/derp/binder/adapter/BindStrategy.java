package com.jajuka.derp.binder.adapter;

import android.view.View;

import com.jajuka.derp.util.Reflect;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by austinh on 10/18/14.
 */
public class BindStrategy {
    private Map<View, Method> mMethodMap = new WeakHashMap<View, Method>();
    private Map<View, String> mPathMap = new WeakHashMap<View, String>();
    private WeakReference<View> mRootView;
    private int mPosition;

    public View getRootView() {
        return mRootView.get();
    }

    public int getPosition() {
        return mPosition;
    }

    public BindStrategy(View rootView, int position, Map<View, Method> methodMap, Map<View, String> pathMap) {
        mRootView = new WeakReference<View>(rootView);
        mPosition = position;
        mMethodMap = methodMap;
        mPathMap = pathMap;
    }

    public void bind(int position, Object object) {
        mPosition = position;

        for (View view : mMethodMap.keySet()) {
            final Method method = mMethodMap.get(view);
            final String path = mPathMap.get(view);
            try {
                method.setAccessible(true);
                method.invoke(view, Reflect.translate(object, path));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
