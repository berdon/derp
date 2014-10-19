package com.jajuka.derp.binder;

import android.text.TextUtils;
import android.view.View;

import com.jajuka.derp.Bind;
import com.jajuka.derp.util.Reflect;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BinderContext implements View.OnClickListener {
    private static final String WRITE_CLICK_DELIMITER = "\\|";
    private static final String WRITE_FIELD_DELIMITER = "\\:";

    private final Bind mBind;
    private final WeakReference<View> mBoundViewReference;

    private String mWriteMethodName;
    private Method mWriteMethod;
    private String mReadPath;
    private Method mOnClick;

    protected BinderContext(Bind bind, View boundView) {
        mBind = bind;
        mBoundViewReference = new WeakReference<View>(boundView);
    }

    public Bind getBind() {
        return mBind;
    }

    public View getBoundView() {
        return mBoundViewReference.get();
    }

    protected String getWriteMethodName() {
        return mWriteMethodName;
    }

    protected Method getWriteMethod(Class<?>... params) {
        final View boundView = getBoundView();

        if (boundView == null) {
            return null;
        }

        if (mWriteMethod == null) {
            if (mWriteMethodName != null) {
                try {
                    mWriteMethod = Reflect.findMethod(boundView, mWriteMethodName, params);
                    mWriteMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return mWriteMethod;
    }

    protected String getReadPath() {
        return mReadPath;
    }

    public Method getOnClick() {
        return mOnClick;
    }

    public boolean isReadBound() {
        return !TextUtils.isEmpty(mReadPath) && !TextUtils.isEmpty(mWriteMethodName);
    }

    public abstract void apply(Object source);

    protected void parseSpecifier(String specifier) {
        final String[] tokens = specifier.split(WRITE_CLICK_DELIMITER);

        if (tokens.length > 0 && !TextUtils.isEmpty(tokens[0])) {
            // Write Method / Read Path
            final String[] bindTokens = tokens[0].split(WRITE_FIELD_DELIMITER);

            if (bindTokens.length < 2) {
                throw new IllegalStateException("You must provide a write method name and an object path to bind to");
            }

            // Locate the write method
            mWriteMethodName = bindTokens[0];

            // Read path
            mReadPath = bindTokens[1];
        }

        final View boundView = getBoundView();
        if (boundView != null && tokens.length > 1 && !TextUtils.isEmpty(tokens[1])) {
            try {
                // Click handler
                mOnClick = boundView.getContext().getClass().getDeclaredMethod(tokens[1], View.class);

                // Set the click handler
                boundView.setOnClickListener(this);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        try {
            mOnClick.invoke(view.getContext(), view);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}