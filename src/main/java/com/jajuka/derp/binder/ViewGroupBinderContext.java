package com.jajuka.derp.binder;

import android.view.View;

import com.jajuka.derp.Bind;

/* pkg */ class ViewGroupBinderContext extends BinderContext {
    /* pkg */ ViewGroupBinderContext(Bind bind, View boundView) {
        super(bind, boundView);
    }

    @Override
    public Bind getBind() {
        return null;
    }

    @Override
    public View getBoundView() {
        return null;
    }

    @Override
    public void apply(Object source) {

    }
}