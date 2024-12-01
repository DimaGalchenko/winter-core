package com.codeus.winter.context;

import com.codeus.winter.config.BeanDefinition;
import com.codeus.winter.config.BeanFactory;
import com.codeus.winter.config.BeanPostProcessor;
import com.codeus.winter.config.ClassPathBeanDefinitionScanner;
import com.codeus.winter.config.DefaultBeanFactory;
import com.codeus.winter.exception.BeanNotFoundException;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Standalone application context, accepting component classes as input.
 *
 * This includes @Configuration-annotated classes, plain @Component types,
 * and JSR-330 compliant classes using jakarta.inject annotations.
 * Allows registering classes one by one using {@code register(Class...)}
 * as well as classpath scanning using {@code scan(String...)}.
 */
public class AnnotationApplicationContext implements ApplicationContext, BeanFactory {
    private final String id = ObjectUtils.identityToString(this);
    private String displayName = ObjectUtils.identityToString(this);
    private final ClassPathBeanDefinitionScanner scanner;
    private final DefaultBeanFactory beanFactory;

    /**
     * Constructs a new {@code AnnotationApplicationContext} for the specified base packages.
     *
     * @param basePackages the base packages to scan for component classes
     */
    public AnnotationApplicationContext(String... basePackages) {
        beanFactory = new DefaultBeanFactory();
        this.scanner = new ClassPathBeanDefinitionScanner(beanFactory);
        scanner.scanPackages(basePackages);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getApplicationName() {
        return "";
    }

    /**
     * Set a friendly name for this context.
     * Typically done during initialization of concrete context implementations.
     * <p>Default is the object id of the context instance.</p>
     *
     * @param displayName the friendly name to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public final String getDisplayName() {
        return displayName;
    }

    @Override
    public final long getStartupDate() {
        return 0;
    }

    @Nullable
    @Override
    public final Object getBean(String name) throws BeanNotFoundException {
        return null;
    }

    @Nullable
    @Override
    public final <T> T getBean(String name, Class<T> requiredType) throws BeanNotFoundException {
        return null;
    }

    @Nullable
    @Override
    public final <T> T getBean(Class<T> requiredType) throws BeanNotFoundException {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public final <T> T createBean(Class<T> beanClass)
            throws BeanNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        return beanFactory.createBean(beanClass);
    }

    @Override
    public final Object createBean(String name) throws BeanNotFoundException {
        return beanFactory.createBean(name);
    }

    @Override
    public final void registerBean(String name, BeanDefinition beanDefinition, Object beanInstance) {
        beanFactory.registerBean(name, beanDefinition, beanInstance);
    }

    @Override
    public final void addBeanPostProcessor(BeanPostProcessor postProcessor) {
        beanFactory.addBeanPostProcessor(postProcessor);
    }
}
