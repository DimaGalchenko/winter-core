package com.codeus.winter.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class AnnotationUtils {
    private static final String VALUE = "value";

    private AnnotationUtils() {
    }

    /**
     * Retrieve the 'value' of the {@code value} attribute of a
     * single-element Annotation, given an annotation instance.
     * @param annotation the annotation instance from which to retrieve the value
     * @return the attribute value, or {@code null} if not found
     */
    public static Object getValue(Annotation annotation) {
        return getValue(annotation, VALUE);
    }

    /**
     * Retrieve the 'value' of a named attribute, given an annotation instance.
     * @param annotation the annotation instance from which to retrieve the value
     * @param attributeName the name of the attribute value to retrieve
     * @return the attribute value, or {@code null} if not found
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        if (annotation == null || attributeName == null || attributeName.trim().isEmpty()) {
            return null;
        }

        try {
            for (Method method : annotation.annotationType().getDeclaredMethods()) {
                if (method.getName().equals(attributeName) && method.getParameterCount() == 0) {
                    return method.invoke(annotation);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
