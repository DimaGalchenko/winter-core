package com.codeus.winter.config;


import com.codeus.winter.exception.BeanFactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class ConstructorResolver {

    private final AutowireCapableBeanFactory beanFactory;

    public ConstructorResolver(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * @param autowiringConstructor
     * @return
     */
    public Object autowireConstructor(Constructor<?> autowiringConstructor) {
        Object[] resolvedDependencies = makeArgumentArray(autowiringConstructor);

        try {
            return autowiringConstructor.newInstance(resolvedDependencies);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanFactoryException("Unable to create bean instance due to: " + e.getMessage(), e);
        }
    }

    Object[] makeArgumentArray(Constructor<?> autowiringConstructor) {
        Parameter[] parameters = autowiringConstructor.getParameters();
        Object[] resolvedDependencies = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String dependencyName = parameter.getName();
            Class<?> dependencyClass = parameter.getType();

            resolvedDependencies[i] = beanFactory.resolveDependency(dependencyName, dependencyClass);
        }
        return resolvedDependencies;
    }
}
