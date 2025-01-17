package com.codeus.winter.config;


import com.codeus.winter.exception.BeanFactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

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
        Type[] parameterTypes = autowiringConstructor.getGenericParameterTypes();
        Object[] resolvedDependencies = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String dependencyName = parameter.getName();
            Class<?> dependencyClass = parameter.getType();
            Type dependencyType = parameterTypes[i];
            DependencyDescriptor descriptor = new DependencyDescriptor(dependencyName, dependencyType, dependencyClass);

            resolvedDependencies[i] = beanFactory.resolveDependency(descriptor);
        }
        return resolvedDependencies;
    }
}
