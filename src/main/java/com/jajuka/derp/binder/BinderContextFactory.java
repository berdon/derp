package com.jajuka.derp.binder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jajuka.derp.Bind;

/**
 * Created by austinh on 10/18/14.
 */
public class BinderContextFactory {
    public static BinderContext create(Bind bind, View view) {
        if (view instanceof AdapterView<?>) {
            return new AdapterBinderContext(bind, view);
        } else if (view instanceof ViewGroup && bind.repeat()) {
            return new ViewGroupBinderContext(bind, view);
        } else {
            return new ViewBinderContext(bind, view);
        }
    }
}
