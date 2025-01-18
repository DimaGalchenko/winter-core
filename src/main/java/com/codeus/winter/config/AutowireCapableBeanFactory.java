package com.codeus.winter.config;

/**
 * An extension of {@link BeanFactory} that provides extra logic for bean resolving and autowiring.
 */
public abstract class AutowireCapableBeanFactory implements BeanFactory {

    /**
     * Resolves Bean instance using given {@link DependencyDescriptor}.
     *
     * @param descriptor a dependency descriptor for resolving a bean.
     * @return bean instance that conform the given {@link DependencyDescriptor}.
     */
    protected abstract Object resolveDependency(DependencyDescriptor descriptor);
}
