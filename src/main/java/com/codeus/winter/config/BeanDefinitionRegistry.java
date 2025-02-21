package com.codeus.winter.config;


import java.util.Map;

/**
 * Interface for registries that hold bean definitions, for example RootBeanDefinition
 * and ChildBeanDefinition instances. Typically implemented by BeanFactories that
 * internally work with the BeanDefinition hierarchy.
 **/
public interface BeanDefinitionRegistry {
    /**
     * Register a new bean definition with this registry.
     *
     * @param beanName       the name of the bean instance to register
     * @param beanDefinition definition of the bean instance to register
     *                       for the specified bean name, and we are not allowed to override it
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * Remove the BeanDefinition for the given name.
     *
     * @param beanName the name of the bean instance to register
     */
    void removeBeanDefinition(String beanName);

    /**
     * Return the BeanDefinition for the given bean name.
     *
     * @param beanName name of the bean to find a definition for
     * @return the BeanDefinition for the given name (never {@code null})
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * Check if this registry contains a bean definition with the given name.
     *
     * @param beanName the name of the bean to look for
     * @return if this registry contains a bean definition with the given name
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * Return the names of all beans defined in this registry.
     *
     * @return the names of all beans defined in this registry,
     * or an empty array if none defined
     */
    String[] getBeanDefinitionNames();

    /**
     * Return the number of beans defined in the registry.
     *
     * @return the number of beans defined in the registry
     */
    int getBeanDefinitionCount();


    /**
     * Return map of registered bean definitions by bean name.
     *
     * @return map of registered bean definitions by bean name
     */
    Map<String, BeanDefinition> getRegisteredBeanDefinitions();
}
