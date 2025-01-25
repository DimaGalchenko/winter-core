package com.codeus.winter.config;

/**
 * An extension of {@link BeanFactory} that provides extra logic for bean resolving and autowiring.
 */
public abstract class AutowireCapableBeanFactory implements BeanFactory {

    /**
     * Resolves Bean instance using given {@link DependencyDescriptor}.
     * Can collect multiple candidates beans into a List, Set or Map.
     * Formed Map contains bean name as a key, bean instance as a value.
     *
     * @param descriptor a dependency descriptor to resolve a bean.
     * @return bean instance that conform the given {@link DependencyDescriptor}.
     */
    protected abstract Object resolveDependency(DependencyDescriptor descriptor);
}
