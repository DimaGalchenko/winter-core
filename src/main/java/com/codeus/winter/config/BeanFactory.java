package com.codeus.winter.config;

import com.codeus.winter.exception.BeanNotFoundException;

public interface BeanFactory {

    /**
     * Registers a single bean definition into the bean factory.
     *
     * @param name           the name of the bean
     * @param beanDefinition the definition of the bean
     * @throws IllegalArgumentException if a bean with the same name already exists
     */
    void registerBeanDefinition(String name, BeanDefinition beanDefinition);

    /**
     * Returns the singleton bean object of this application context for specified name.
     *
     * @param name bean's name.
     * @return the singleton bean object of this context.
     * @throws BeanNotFoundException if bean not found for specified name.
     */
    Object getBean(String name) throws BeanNotFoundException;

    /**
     * Returns the singleton bean object of this application context for specified name
     * and cast it to the specified class type.
     *
     * @param name         bean name
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the singleton bean object of this context.
      @throws BeanNotFoundException if bean not found for specified name and type.
     */
    <T> T getBean(String name, Class<T> requiredType) throws BeanNotFoundException;

    /**
     * Returns the singleton bean object of this application context for the specified class type.
     *
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the singleton bean object of this context.
      @throws BeanNotFoundException if bean not found for specified type.
     */
    <T> T getBean(Class<T> requiredType) throws BeanNotFoundException;

    /**
     * Creates a prototype-scoped bean for the specified bean class.
     *
     * @param beanClass specified bean class.
     * @param <T>       the type of the bean
     * @return the bean for the specified bean class.
     */
    <T> T createBean(Class<T> beanClass);

    /**
     * Registers a bean for its name, BeanDefinition, and instance.
     *
     * @param name           bean's name.
     * @param beanDefinition bean's BeanDefinition.
     * @param beanInstance   bean's instance.
     */
    void registerBean(String name, BeanDefinition beanDefinition, Object beanInstance);

    /**
     * Adds a BeanPostProcessor.
     *
     * @param postProcessor BeanPostProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor postProcessor);
}
