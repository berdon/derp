package com.jajuka.derp;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.jajuka.derp.binder.BinderContext;
import com.jajuka.derp.binder.BinderContextFactory;
import com.jajuka.derp.util.Reflect;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.WeakHashMap;

/**
 * Created by austinh on 10/18/14.
 */
public class Derp {
    private static final WeakHashMap<Context, DerpContext> sContexts = new WeakHashMap<Context, DerpContext>();

    public static void bind(Activity activity) {
        final DerpContext derpContext = getDerpContext(activity);
    }

    private static DerpContext getDerpContext(Activity activity) {
        DerpContext derpContext = sContexts.get(activity);

        if (derpContext == null) {
            derpContext = new DerpContext(activity);
            sContexts.put(activity, derpContext);
        }

        return derpContext;
    }

    public static class DerpContext implements DataBinding.Observer<Object> {
        private final WeakHashMap<View, BinderContext> mViewToBinder = new WeakHashMap<View, BinderContext>();
        private final WeakHashMap<DataBinding<?>, BinderContext> mBindingToBinder = new WeakHashMap<DataBinding<?>, BinderContext>();

        public DerpContext(Activity activity) {
            initialize(activity);
        }

        private void initialize(Activity activity) {
            // Traverse activity
            for (final Field field : activity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Bind.class)) {
                    // Grab the bind annotation
                    final Bind bind = field.getAnnotation(Bind.class);

                    // Grab the bound view
                    final View boundView = activity.findViewById(bind.value());

                    // Make sure we found the binding view
                    if (boundView == null) {
                        throw new IllegalStateException("Unable to locate view to bind to");
                    }

                    // Create and track the binder context
                    final BinderContext binder = createAndTrackBinderContext(bind, boundView);

                    // Apply the data to the view
                    if (binder.isReadBound()) {
                        final Object value = Reflect.getValue(field, activity);
                        final boolean isBinder = value instanceof DataBinding<?>;
                        binder.apply(isBinder ? ((DataBinding<?>) value).get() : value);

                        // Register an observer if applicable
                        if (isBinder) {
                            // Grab the binding
                            final DataBinding<?> dataBinding = (DataBinding<?>) value;

                            // Register the derp context as the observer
                            dataBinding.registerObserver(this);

                            // Map the binding to the binder
                            mBindingToBinder.put(dataBinding, binder);
                        }
                    }
                }
            }

            for (final Method method : activity.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Bind.class)) {
                    // Grab the bind annotation
                    final Bind bind = method.getAnnotation(Bind.class);

                    // Grab the bound view
                    final View boundView = activity.findViewById(bind.value());

                    // Make sure we found the binding view
                    if (boundView == null) {
                        throw new IllegalStateException("Unable to locate view to bind to");
                    }

                    // Create and track the binder context
                    final BinderContext binder = createAndTrackBinderContext(bind, boundView);

                    if (binder.isReadBound()) {
                        // Apply the data to the view
                        final Object value = Reflect.getValue(method, activity);
                        binder.apply(value);
                    }
                }
            }
        }

        private BinderContext createAndTrackBinderContext(Bind bind, View view) {
            final BinderContext binder = BinderContextFactory.create(bind, view);
            mViewToBinder.put(view, binder);
            return binder;
        }

        @Override
        public void onChange(DataBinding<Object> dataBinding, Object value) {
            // Grab the Binder reference
            final BinderContext binder = mBindingToBinder.get(dataBinding);
            if (binder != null) {
                binder.apply(value);
            }
        }
    }
}
