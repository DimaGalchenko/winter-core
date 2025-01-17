package com.codeus.winter.config;

public abstract class AutowireCapableBeanFactory implements BeanFactory {

    protected abstract Object resolveDependency(DependencyDescriptor descriptor);
}
