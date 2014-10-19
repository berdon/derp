package com.jajuka.derp.binder;

import android.view.View;

import com.jajuka.derp.Bind;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* pkg */ class ViewBinderContext extends BinderContext {
    /* pkg */ ViewBinderContext(Bind bind, View view) {
        super(bind, view);

        // Initialize the write/click handlers
        if (view.getTag() != null && view.getTag() instanceof String) {
            parseSpecifier((String) view.getTag());
        }
    }

    public void apply(Object value) {
        // Grab the write method
        final Method writeMethod = getWriteMethod(value.getClass());

        try {
            // Apply the data to the view
            writeMethod.invoke(getBoundView(), value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}