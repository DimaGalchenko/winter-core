package com.codeus.winter.config.impl;

import com.codeus.winter.config.BeanDefinition;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the {@link BeanDefinition} interface that provides
 * a structure for defining the properties of a bean
 * <p>
 * This class is intended to describe metadata for a bean, including its class name,
 * scope, dependencies, and other properties required for its lifecycle.
 * </p>
 */
public class BeanDefinitionImpl implements BeanDefinition {

    private String beanClassName;
    private String scope = SCOPE_SINGLETON; // Default to singleton

    private boolean injectCandidate = true;
    private boolean primary = false;
    private String factoryBeanName;
    private String factoryMethodName;
    private String initMethodName;
    private String destroyMethodName;

    private final List<String> dependsOn = new ArrayList<>();
    /**
     * Set the class name of the bean that this definition describes.
     *
     * @param beanClassName the fully qualified class name of the bean, or {@code null} if not set.
     */
    @Override
    public void setBeanClassName(@Nullable String beanClassName) {
        this.beanClassName = beanClassName;
    }
    /**
     * Get the class name of the bean that this definition describes.
     *
     * @return the fully qualified class name of the bean, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getBeanClassName() {
        return this.beanClassName;
    }
    /**
     * Set the scope of the bean (e.g., singleton or prototype).
     *
     * @param scope the scope of the bean, or {@code null} if not set.
     */
    @Override
    public void setScope(@Nullable String scope) {
        this.scope = (scope != null) ? scope : SCOPE_SINGLETON;
    }
    /**
     * Get the scope of the bean.
     *
     * @return the scope of the bean as a string, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getScope() {
        return this.scope;
    }
    /**
     * Check whether this bean is a singleton.
     *
     * @return {@code true} if the bean is a singleton; {@code false} otherwise.
     */
    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope);
    }

    /**
     * Check whether this bean is a prototype.
     *
     * @return {@code true} if the bean is a prototype; {@code false} otherwise.
     */
    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    /**
     * Set the names of the beans that this bean depends on for initialization.
     *
     * @param dependsOn an array of bean names that this bean depends on, or {@code null}.
     */
    @Override
    public void setDependsOn(@Nullable String... dependsOn) {
        this.dependsOn.clear();
        if (dependsOn != null) {
            this.dependsOn.addAll(Arrays.asList(dependsOn));
        }
    }
    /**
     * Get the names of the beans that this bean depends on for initialization.
     *
     * @return an array of bean names, or an empty array if no dependencies are set.
     */
    @Nullable
    @Override
    public String[] getDependsOn() {
        return dependsOn.toArray(new String[0]);
    }
    /**
     * Specify whether this bean can be autowired into other beans.
     *
     * @param injectCandidate {@code true} if the bean is an autowire candidate; {@code false} otherwise.
     */
    @Override
    public void setInjectCandidate(boolean injectCandidate) {
        this.injectCandidate = injectCandidate;
    }
    /**
     * Check whether this bean is a candidate for autowiring.
     *
     * @return {@code true} if the bean can be autowired; {@code false} otherwise.
     */
    @Override
    public boolean isInjectCandidate() {
        return this.injectCandidate;
    }
    /**
     * Specify whether this bean is a primary candidate for autowiring when multiple candidates are available.
     *
     * @param primary {@code true} if the bean is primary; {@code false} otherwise.
     */
    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    /**
     * Check whether this bean is a primary candidate for autowiring.
     *
     * @return {@code true} if the bean is primary; {@code false} otherwise.
     */
    @Override
    public boolean isPrimary() {
        return this.primary;
    }
    /**
     * Set the name of the factory bean, if any, that creates this bean.
     *
     * @param factoryBeanName the name of the factory bean, or {@code null}.
     */
    @Override
    public void setFactoryBeanName(@Nullable String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
    /**
     * Get the name of the factory bean, if any, that creates this bean.
     *
     * @return the name of the factory bean, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }
    /**
     * Set the name of the factory method, if any, that creates this bean.
     *
     * @param factoryMethodName the name of the factory method, or {@code null}.
     */
    @Override
    public void setFactoryMethodName(@Nullable String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }
    /**
     * Get the name of the factory method, if any, that creates this bean.
     *
     * @return the name of the factory method, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }
    /**
     * Set the name of the method to be called after the bean has been initialized.
     *
     * @param initMethodName the name of the initialization method, or {@code null}.
     */
    @Override
    public void setInitMethodName(@Nullable String initMethodName) {
        this.initMethodName = initMethodName;
    }
    /**
     * Get the name of the method to be called after the bean has been initialized.
     *
     * @return the name of the initialization method, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getInitMethodName() {
        return this.initMethodName;
    }
    /**
     * Set the name of the method to be called before the bean is destroyed.
     *
     * @param destroyMethodName the name of the destroy method, or {@code null}.
     */
    @Override
    public void setDestroyMethodName(@Nullable String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
    /**
     * Get the name of the method to be called before the bean is destroyed.
     *
     * @return the name of the destroy method, or an empty string if not set.
     */
    @Nullable
    @Override
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }
}
