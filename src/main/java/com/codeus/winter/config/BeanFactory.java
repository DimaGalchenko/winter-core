package com.codeus.winter.config;

import com.codeus.winter.exception.BeansException;

import javax.annotation.Nullable;

public interface BeanFactory<T> {

    /**
     * Return the unique bean object of this application context for specified name.
     * @return the unique bean object of this context, or null if none
     */
    @Nullable
    Object getBean(String name) throws BeansException;

    /**
     * Return the unique bean object of this application context for specified name and cast it specified class type.
     * @param name bean name
     * @param requiredType required class type
     * @return the unique bean object of this context, or null if none
     */
    @Nullable
    T getBean(String name, Class<T> requiredType) throws BeansException;

    /**
     * Return the unique bean object of this application context for specified class type.
     * @param requiredType required class type
     * @return the unique bean object of this context, or null if none
     */
    @Nullable
    T getBean(Class<T> requiredType) throws BeansException;

    /**
     * Create bean for specified bean class
     * @param beanClass specified bean class
     * @return bean for specified bean class
     */
    T createBean(Class<T> beanClass) throws BeansException;

    /**
     * Create bean for specified name
     * @param name bean's name
     * @return bean object for specified bean's name
     */
    Object createBean(String name) throws BeansException;

    void registerBean(String name, BeanDefinition beanDefinition, Object beanInstance);

    void addBeanPostProcessor(BeanPostProcessor postProcessor);
}
