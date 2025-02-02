package com.codeus.winter.config;

import jakarta.annotation.Nullable;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 **/
public interface BeanDefinition {

    String SCOPE_SINGLETON = Scope.SINGLETON.toString();
    String SCOPE_PROTOTYPE = Scope.PROTOTYPE.toString();

    /**
     * @param beanClassName Specify the bean class name of this bean definition.
     */
    void setBeanClassName(@Nullable String beanClassName);

    /**
     * @return the current bean class name of this bean definition
     */
    @Nullable
    String getBeanClassName();

    /**
     * Override the target scope of this bean, specifying a new scope name.
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     * @param scope bean's scope.
     */
    void setScope(@Nullable String scope);

    /**
     * Return the name of the current target scope for this bean,
     * or {@code null} if not known yet.
     * @return bean's scope.
     */
    @Nullable
    String getScope();

    /**
     * Default method for singleton checking.
     *
     * @return true.
     */
    default boolean isSingleton() {
        return true;
    }


    /**
     * Default method for prototype checking.
     * @return false.
     */
    default boolean isPrototype() {
        return false;
    }

    /**
     * Set the names of the beans that this bean depends on being initialized.
     * The bean factory will guarantee that these beans get initialized first.
     * @param dependsOn array of dependencies.
     */
    void setDependsOn(@Nullable String... dependsOn);

    /**
     * Return the bean names that this bean depends on.
     * @return array of dependencies.
     */
    @Nullable
    String[] getDependsOn();

    /**
     * Set whether this bean is a candidate for getting autowired into some other bean.
     * <p>Note that this flag is designed to only affect type-based autowiring.
     * It does not affect explicit references by name, which will get resolved even
     * if the specified bean is not marked as an autowire candidate. As a consequence,
     * autowiring by name will nevertheless inject a bean if the name matches.
     * @param injectCandidate true if this bean is a autowire candidate.
     */
    void setInjectCandidate(boolean injectCandidate);

    /**
     * Return whether this bean is a candidate for getting autowired into some other bean.
     * @return true if this bean is a candidate.
     */
    boolean isInjectCandidate();

    /**
     * Set whether this bean is a primary autowire candidate.
     * <p>If this value is {@code true} for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     * @param primary true if bean is primary.
     */
    void setPrimary(boolean primary);

    /**
     * Return whether this bean is a primary autowire candidate.
     * @return true if bean is primary, if not - false.
     */
    boolean isPrimary();

    /**
     * Specify the factory bean to use, if any.
     * This the name of the bean to call the specified factory method on.
     * @see #setFactoryMethodName
     * @param factoryBeanName factory bean name.
     */
    void setFactoryBeanName(@Nullable String factoryBeanName);

    /**
     * Return the factory bean name, if any.
     * @return factory bean name.
     */
    @Nullable
    String getFactoryBeanName();

    /**
     * Specify a factory method, if any. This method will be invoked with
     * constructor arguments, or with no arguments if none are specified.
     * The method will be invoked on the specified factory bean, if any,
     * or otherwise as a static method on the local bean class.
     * @see #setFactoryBeanName
     * @see #setBeanClassName
     * @param factoryMethodName factory method name.
     */
    void setFactoryMethodName(@Nullable String factoryMethodName);

    /**
     * Return a factory method, if any.
     * @return factory method name.
     */
    @Nullable
    String getFactoryMethodName();

    /**
     * Set the name of the initializer method.
     * @param initMethodName name of destroy method.
     */
    void setInitMethodName(@Nullable String initMethodName);

    /**
     * Return the name of the initializer method.
     * @return String method's name.
     */
    @Nullable
    String getInitMethodName();

    /**
     * Set the name of the destroy method.
     * @param destroyMethodName name of destroy method.
     */
    void setDestroyMethodName(@Nullable String destroyMethodName);

    /**
     * Return the name of the destroy method.
     * @return String method's name.
     */
    @Nullable
    String getDestroyMethodName();
}
