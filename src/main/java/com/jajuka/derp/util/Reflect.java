package com.jajuka.derp.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by austinh on 10/18/14.
 */
public class Reflect {
    public static Object translate(Object target, String path) {
        try {
            String specifications[] = path.split("\\.");

            for (String specifier : specifications) {
                // Pull of indices
                Integer index = null;
                if (specifier.matches("^.*\\[[0-9]*\\]$")) {
                    String[] tokens = specifier.split("(\\[|\\])");
                    index = Integer.parseInt(tokens[1]);
                    specifier = tokens[0];
                }

                if (specifier.matches("^.*\\(\\)$")) {
                    specifier = specifier.substring(0, specifier.length() - 2);
                    try {
                        final Method method = target.getClass().getDeclaredMethod(specifier);
                        target = method.invoke(target);
                    } catch (NoSuchMethodException e) {
                        // TODO
                    } catch (InvocationTargetException e) {
                        // TODO
                    }
                } else {
                    try {
                        final Field field = target.getClass().getDeclaredField(specifier);
                        field.setAccessible(true);
                        target = field.get(target);
                    } catch (NoSuchFieldException e) {
                        // TODO
                    }
                }

                // Handle indexing
                if (index != null) {
                    if (target instanceof String) {
                        target = ((String) target).charAt(index);
                    } else if (target instanceof Object[]) {
                        target = ((Object[]) target)[index];
                    } else if (target instanceof List<?>) {
                        target = ((List<?>) target).get(index);
                    } else {
                        throw new IllegalAccessException("Unable to index into " + target);
                    }
                }
            }

            return target;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Method findMethod(Object target, String name, Class<?>... parameters) throws NoSuchMethodException {
        try {
            // First try exact match
            return target.getClass().getMethod(name, parameters);
        } catch (NoSuchMethodException e) { }

        // Now search for equivalent types
        Method[] methods = target.getClass().getMethods();
        for (Method m : methods) {
            Class<?>[] parameterTypes = m.getParameterTypes();

            // Fast length check
            if (!m.getName().equalsIgnoreCase(name) || parameters.length != parameterTypes.length) {
                continue;
            }

            // Check the types
            boolean found = true;
            for (int i = 0; i < parameters.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(parameters[i])) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return m;
            }
        }

        throw new NoSuchMethodException("No such method: " + name);
    }

    public static Object getValue(Field field, Object target) {
        final boolean isAccessible = field.isAccessible();
        if (!isAccessible) {
            field.setAccessible(true);
        }

        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static Object getValue(Method method, Object target, Object... params) {
        final boolean isAccessible = method.isAccessible();
        if (!isAccessible) {
            method.setAccessible(true);
        }

        try {
            if (params == null) {
                return method.invoke(target);
            } else {
                return method.invoke(target, params);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            method.setAccessible(isAccessible);
        }

        return null;
    }
}
