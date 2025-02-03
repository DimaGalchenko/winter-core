package com.codeus.winter.config;

import com.codeus.winter.exception.BeanNotFoundException;
import jakarta.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

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
     * Return the unique bean object of this application context for specified name.
     *
     * @param name bean's name.
     * @return the unique bean object of this context, or null if none.
     */
    @Nullable
    Object getBean(String name) throws BeanNotFoundException;

    /**
     * Return the unique bean object of this application context for specified name
     * and cast it to the specified class type.
     *
     * @param name         bean name
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the unique bean object of this context, or null if none
     */
    @Nullable
    <T> T getBean(String name, Class<T> requiredType) throws BeanNotFoundException;

    /**
     * Return the unique bean object of this application context for the specified class type.
     *
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the unique bean object of this context, or null if none
     */
    @Nullable
    <T> T getBean(Class<T> requiredType) throws BeanNotFoundException;

    /**
     * Create a bean for the specified bean class.
     *
     * @param beanClass specified bean class.
     * @param <T>       the type of the bean
     * @return the bean for the specified bean class.
     */
    <T> T createBean(Class<T> beanClass) throws BeanNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException;

    /**
     * Register a bean for its name, BeanDefinition, and instance.
     *
     * @param name           bean's name.
     * @param beanDefinition bean's BeanDefinition.
     * @param beanInstance   bean's instance.
     */
    void registerBean(String name, BeanDefinition beanDefinition, Object beanInstance);

    /**
     * Add a BeanPostProcessor.
     *
     * @param postProcessor BeanPostProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor postProcessor);
}
