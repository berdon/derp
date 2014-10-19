package com.jajuka.derp;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by austinh on 10/18/14.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {
    int value();
    boolean repeat() default false;
    int layoutId() default View.NO_ID;
    String target() default "";
}
