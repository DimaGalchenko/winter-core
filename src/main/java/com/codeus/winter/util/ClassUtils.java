package com.codeus.winter.util;

import com.codeus.winter.exception.BeanFactoryException;

public class ClassUtils {

    /**
     * Resolves class type for given class name.
     *
     * @param className a name of a class to resolve.
     * @return class type for given class name.
     * @throws BeanFactoryException if class type cannot be found by the given class name.
     */
    public static Class<?> resolveClass(String className) {
        Class<?> beanClass;
        try {
            beanClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot resolve class for name='%s'".formatted(className), e);
        }
        return beanClass;
    }
}
