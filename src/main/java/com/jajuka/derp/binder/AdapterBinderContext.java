package com.jajuka.derp.binder;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

import com.jajuka.derp.Bind;
import com.jajuka.derp.binder.adapter.BindStrategy;
import com.jajuka.derp.binder.adapter.LayoutFactory;
import com.jajuka.derp.util.Reflect;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/* pkg */ class AdapterBinderContext extends BinderContext implements View.OnClickListener {
    private WeakReference<LayoutInflater> mLayoutInflater;
    private LayoutFactory mLayoutFactory;
    private android.widget.BaseAdapter mAdapter;

    private SimpleOnViewCreatedCallback mOnViewCreatedCallback = new SimpleOnViewCreatedCallback();

    private WeakHashMap<View, Method> mOnItemViewClickMethods = new WeakHashMap<View, Method>();

    /* pkg */ AdapterBinderContext(Bind bind, View boundView) {
        super(bind, boundView);

        if (bind.layoutId() == View.NO_ID) {
            throw new IllegalStateException("layoutId must be set when using using a repeat binding");
        }
    }

    private LayoutInflater getLayoutInflater(Context context) {
        LayoutInflater inflater = null;
        if (mLayoutInflater != null) {
            inflater = mLayoutInflater.get();
        }

        if (inflater == null) {
            inflater = LayoutInflater.from(context);

            mLayoutInflater = new WeakReference<LayoutInflater>(inflater);
            mLayoutFactory = new LayoutFactory();
            inflater.setFactory(mLayoutFactory);
            mLayoutFactory.setOnViewCreatedCallback(mOnViewCreatedCallback);
        }

        return inflater;
    }

    @Override
    public boolean isReadBound() {
        return true;
    }

    @Override
    public void apply(Object source) {
        final View boundView = getBoundView();

        if (boundView == null) {
            return;
        }

        if (boundView instanceof AdapterView<?>) {
            if (mAdapter == null) {
                // Create special adapters depending on the data source
                // TODO : abstract this into strategies and allow consumers to add their own
                if (source instanceof Object[]) {
                    // Create a new simple adapter
                    mAdapter = new ArrayAdapter(boundView.getContext(), getBind().layoutId(), (Object[]) source);
                } else if (source instanceof List<?>) {
                    // Create a new simple adapter
                    mAdapter = new ListAdapter(boundView.getContext(), getBind().layoutId(), (List<Object>) source);
                } else if (source instanceof Cursor) {
                    mAdapter = new CursorAdapter(boundView.getContext(), getBind().layoutId(), (Cursor) source);
                } else {
                    throw new IllegalStateException("Bound object must be either an array, List or Cursor");
                }

                // Set the adapter
                ((AdapterView) boundView).setAdapter(mAdapter);
            } else {
                // Swap out the cursor if the cursor has changed
                if (source instanceof Cursor && source != ((CursorAdapter) mAdapter).getCursor()) {
                    ((CursorAdapter) mAdapter).changeCursor((Cursor) source);
                }

                mAdapter.notifyDataSetChanged();
            }
        } else if (boundView instanceof ViewGroup) {
            // Clear the children from the view
            ((ViewGroup) boundView).removeAllViews();

            final Object[] targets = (Object[]) source;

            for (final Object target : targets) {
                mOnViewCreatedCallback.target = target;

                // Inflate the view
                final LayoutInflater inflater = getLayoutInflater(boundView.getContext());
                if (inflater != null) {
                    inflater.inflate(getBind().layoutId(), (ViewGroup) boundView);
                }
            }
        }
    }

    private class CursorAdapter extends android.widget.CursorAdapter {
        private final WeakHashMap<View, BindStrategy> mBindStrategies = new WeakHashMap<View, BindStrategy>();
        private final WeakHashMap<View, WeakReference<View>> mChildViewToRootItemViewMap = new WeakHashMap<View, WeakReference<View>>();
        private final int mLayoutId;

        private CursorAdapter(Context context, int layoutId, Cursor c) {
            super(context, c);
            mLayoutId = layoutId;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            mOnViewCreatedCallback.target = cursor;
            mOnViewCreatedCallback.methodMap = new WeakHashMap<View, Method>();
            mOnViewCreatedCallback.pathMap = new WeakHashMap<View, String>();
            mOnViewCreatedCallback.clickViews = new WeakHashMap<View, Void>();

            final LayoutInflater inflater = getLayoutInflater(context);
            if (inflater == null) {
                return null;
            }

            final View view = inflater.inflate(mLayoutId, null);
            final BindStrategy bindStrategy = new BindStrategy(
                    view,
                    -1,
                    mOnViewCreatedCallback.methodMap,
                    mOnViewCreatedCallback.pathMap);
            view.setTag(bindStrategy);

            // Map the view to the BindStrategy
            mBindStrategies.put(view, bindStrategy);

            // Map any click handler to the root item layout
            for (View childView : mOnViewCreatedCallback.clickViews.keySet()) {
                mChildViewToRootItemViewMap.put(childView, new WeakReference<View>(view));
            }

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final BindStrategy bindStrategy = (BindStrategy) view.getTag();
            bindStrategy.bind(cursor.getPosition(), cursor);
        }
    }

    private class ListAdapter extends BaseAdapter {
        private List<Object> mObjects;

        private ListAdapter(Context context, int resource, List<Object> objects) {
            super(context, resource);
            mObjects = objects;
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }

        @Override
        public Object getItem(int i) {
            return mObjects.get(i);
        }
    }

    private class ArrayAdapter extends BaseAdapter {
        private Object[] mObjects;

        private ArrayAdapter(Context context, int resource, Object[] objects) {
            super(context, resource);
            mObjects = objects;
        }

        @Override
        public int getCount() {
            return mObjects.length;
        }

        @Override
        public Object getItem(int i) {
            return mObjects[i];
        }
    }

    private abstract class BaseAdapter extends android.widget.BaseAdapter {
        private final WeakReference<Context> mContext;
        private final WeakHashMap<View, BindStrategy> mBindStrategies = new WeakHashMap<View, BindStrategy>();
        private final WeakHashMap<View, WeakReference<View>> mChildViewToRootItemViewMap = new WeakHashMap<View, WeakReference<View>>();
        private final int mLayoutId;

        private BaseAdapter(Context context, int resource) {
            mContext = new WeakReference<Context>(context);
            mLayoutId = resource;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Object target = getItem(position);

            final Context context = mContext.get();
            if (context == null) {
                return null;
            }

            if (convertView == null) {
                mOnViewCreatedCallback.target = target;
                mOnViewCreatedCallback.methodMap = new WeakHashMap<View, Method>();
                mOnViewCreatedCallback.pathMap = new WeakHashMap<View, String>();
                mOnViewCreatedCallback.clickViews = new WeakHashMap<View, Void>();

                final LayoutInflater inflater = getLayoutInflater(context);
                if (inflater == null) {
                    return null;
                }

                final View view = inflater.inflate(mLayoutId, null);
                final BindStrategy bindStrategy = new BindStrategy(
                        view,
                        position,
                        mOnViewCreatedCallback.methodMap,
                        mOnViewCreatedCallback.pathMap);
                view.setTag(bindStrategy);

                // Map the view to the BindStrategy
                mBindStrategies.put(view, bindStrategy);

                // Map any click handler to the root item layout
                for (View childView : mOnViewCreatedCallback.clickViews.keySet()) {
                    mChildViewToRootItemViewMap.put(childView, new WeakReference<View>(view));
                }

                return view;
            }

            final BindStrategy bindStrategy = (BindStrategy) convertView.getTag();
            bindStrategy.bind(position, target);

            return convertView;
        }
    }

    @Override
    public void onClick(View view) {
        // Grab the bound view
        final View boundView = getBoundView();
        if (boundView == null) {
            return;
        }

        // Grab the method from the view mapping
        final Method onClickMethod = mOnItemViewClickMethods.get(view);

        // Grab the root item layout
        final WeakReference<View> rootItemViewReference = (mAdapter instanceof CursorAdapter ?
                ((CursorAdapter) mAdapter).mChildViewToRootItemViewMap.get(view) : ((BaseAdapter) mAdapter).mChildViewToRootItemViewMap.get(view));
        if (rootItemViewReference == null) {
            return;
        }

        final View rootItemView = rootItemViewReference.get();
        if (rootItemView == null) {
            return;
        }

        // Grab the bind strategy for the root item view
        final BindStrategy bindStrategy = (mAdapter instanceof CursorAdapter ?
                ((CursorAdapter) mAdapter).mBindStrategies.get(rootItemView) : ((BaseAdapter) mAdapter).mBindStrategies.get(rootItemView));
        if (bindStrategy == null) {
            return;
        }

        if (onClickMethod != null) {
            try {
                onClickMethod.invoke(view.getContext(), boundView, view, bindStrategy.getPosition());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private class SimpleOnViewCreatedCallback implements LayoutFactory.OnViewCreatedCallback {
        public Object target;
        public Map<View, Method> methodMap;
        public Map<View, String> pathMap;
        public WeakHashMap<View, Void> clickViews;

        @Override
        public void onViewCreated(View view, String name, Context context, AttributeSet attrs) {
            if (view.getTag() != null && view.getTag() instanceof String) {
                // Split on | for read|click
                String[] tokens = ((String) view.getTag()).split("\\|");

                if (tokens.length > 0 && !TextUtils.isEmpty(tokens[0])) {
                    // Split for the method and the target
                    String[] readTokens = ((String) view.getTag()).split("\\:");
                    final String methodName = readTokens.length > 0 ? readTokens[0] : "setText";
                    final String path = readTokens.length > 1 ? readTokens[1] : "";

                    // Store the path
                    pathMap.put(view, path);

                    try {
                        // Grab the value to assign
                        Object value = Reflect.translate(target, path);

                        // Grab the method to call
                        final Method method = Reflect.findMethod(view, methodName, value.getClass());
                        method.setAccessible(true);

                        // Store the method
                        methodMap.put(view, method);

                        // Execute the method
                        method.invoke(view, value);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                if (tokens.length > 1 && !TextUtils.isEmpty(tokens[1])) {
                    try {
                        // Track the click view
                        clickViews.put(view, null);

                        // Grab the actual callback method
                        final Method method = context
                                .getClass()
                                .getDeclaredMethod(tokens[1], View.class, View.class, int.class);

                        // Store the method
                        mOnItemViewClickMethods.put(view, method);

                        // Set the callback method to ours
                        view.setOnClickListener(AdapterBinderContext.this);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}