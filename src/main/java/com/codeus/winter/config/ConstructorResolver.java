package com.codeus.winter.config;

import com.codeus.winter.exception.BeanFactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ConstructorResolver {

    private final AbstractAutowireCapableBeanFactory beanFactory;

    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Instantiates bean using given autowiring constructor.
     *
     * @param constructor a constructor to autowire.
     * @return a bean instance.
     * @throws BeanFactoryException if given constructor doesn't exist,
     * argument array size doesn't match, or it is not accessible.
`     * Also, may contain exceptions thrown by the constructor.
     */
    public Object autowireConstructor(Constructor<?> constructor) {
        Object[] resolvedDependencies = makeArgumentArray(constructor);

        try {
            return constructor.newInstance(resolvedDependencies);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanFactoryException("Unable to create bean instance due to: " + e.getMessage(), e);
        }
    }

    /**
     * Makes arguments array for given constructor using {@link AbstractAutowireCapableBeanFactory} as an argument resolver.
     *
     * @param constructor a constructor to make arguments array for.
     * @return an array of bean instances that forms arguments array for given constructor.
     */
    Object[] makeArgumentArray(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Type[] parameterTypes = constructor.getGenericParameterTypes();
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
