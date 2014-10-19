package com.jajuka.derp.binder.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by austinh on 10/18/14.
 */
public class LayoutFactory implements LayoutInflater.Factory {
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit."
    };

    private OnViewCreatedCallback mCallback;
    public void setOnViewCreatedCallback(OnViewCreatedCallback callback) {
        mCallback = callback;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = null;

        if (view == null) {
            view = createViewOrFailQuietly(name, context, attrs);
        }

        if (view != null) {
            if (mCallback != null) {
                mCallback.onViewCreated(view, name, context, attrs);
            }
        }

        return view;
    }

    protected View createViewOrFailQuietly(String name, Context context, AttributeSet attrs) {
        if (name.contains(".")) {
            return createViewOrFailQuietly(name, null, context, attrs);
        }

        for (final String prefix : sClassPrefixList) {
            final View view = createViewOrFailQuietly(name, prefix, context, attrs);

            if (view != null) {
                return view;
            }
        }

        return null;
    }

    protected View createViewOrFailQuietly(String name, String prefix, Context context, AttributeSet attrs) {
        try {
            return LayoutInflater.from(context).createView(name, prefix, attrs);
        } catch (Exception ignore) {
            return null;
        }
    }

    public interface OnViewCreatedCallback {
        void onViewCreated(View view, String name, Context context, AttributeSet attrs);
    }
}