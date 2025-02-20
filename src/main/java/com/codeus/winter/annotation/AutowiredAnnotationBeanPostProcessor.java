package com.codeus.winter.annotation;

import com.codeus.winter.config.AbstractAutowireCapableBeanFactory;
import com.codeus.winter.config.BeanPostProcessor;
import com.codeus.winter.config.DependencyDescriptor;
import com.codeus.winter.exception.BeanNotFoundException;
import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * BeanPostProcessor implementation that autowires annotated fields, setter methods, and constructor.
 */
@SuppressWarnings("java:S3011")
public class AutowiredAnnotationBeanPostProcessor implements BeanPostProcessor {

    private AbstractAutowireCapableBeanFactory beanFactory;

    /**
     * Set BeanFactory as dependency.
     *
     * @param beanFactory bean factory
     */
    public void setBeanFactory(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Resolve dependency injection for constructors/methods/fields with @Autowired annotation.
     *
     * @param bean     bean object
     * @param beanName bean name
     * @return bean object
     */
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeanNotFoundException {
        try {
            injectMethod(bean);
            injectField(bean);
        } catch (Exception e) {
            throw new BeanNotFoundException("Bean post processing failed: " + beanName, e);
        }
        return bean;
    }

    private void injectMethod(Object bean) throws InvocationTargetException, IllegalAccessException {
        Class<?> beanType = bean.getClass();
        for (Method method : beanType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Autowired.class)) {
                for (Parameter parameter : method.getParameters()) {
                    Object dependency = beanFactory.resolveDependency(DependencyDescriptor.from(parameter));
                    method.setAccessible(true);
                    method.invoke(bean, dependency);
                }
            }
        }
    }

    private void injectField(Object bean) throws IllegalAccessException {
        Class<?> beanType = bean.getClass();
        for (Field field : beanType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = beanFactory.resolveDependency(DependencyDescriptor.from(field));
                field.setAccessible(true);
                field.set(bean, dependency);
            }
        }
    }
}
