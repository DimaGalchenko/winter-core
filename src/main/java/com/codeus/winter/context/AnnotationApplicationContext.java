package com.codeus.winter.context;

import com.codeus.winter.annotation.AutowiredAnnotationBeanPostProcessor;
import com.codeus.winter.annotation.InitDestroyAnnotationBeanPostProcessor;
import com.codeus.winter.config.BeanDefinition;
import com.codeus.winter.config.BeanDefinitionRegistry;
import com.codeus.winter.config.BeanFactory;
import com.codeus.winter.config.BeanPostProcessor;
import com.codeus.winter.config.DefaultBeanFactory;
import com.codeus.winter.config.PackageBeanRegistration;
import com.codeus.winter.config.impl.BeanDefinitionRegistryImpl;
import com.codeus.winter.exception.BeanNotFoundException;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Standalone application context, accepting component classes as input.
 * <p>
 * This includes @Configuration-annotated classes, plain @Component types,
 * and JSR-330 compliant classes using jakarta.inject annotations.
 * Allows registering classes one by one using {@code register(Class...)}
 * as well as classpath scanning using {@code scan(String...)}.
 */
public class AnnotationApplicationContext implements ApplicationContext, BeanFactory {
    private final long startupMillis = System.currentTimeMillis();
    private final String id = ObjectUtils.identityToString(this);
    private String displayName = ObjectUtils.identityToString(this);

    private final PackageBeanRegistration packageBeanRegistration;
    private final DefaultBeanFactory beanFactory;
    private final BeanDefinitionRegistry beanDefinitionRegistry;

    /**
     * Constructs a new {@code AnnotationApplicationContext} for the specified base packages.
     *
     * @param basePackages the base packages to scan for component classes
     */
    public AnnotationApplicationContext(String... basePackages) {
        System.out.println(WINTER_BANNER);
        this.beanDefinitionRegistry = new BeanDefinitionRegistryImpl();
        this.packageBeanRegistration = new PackageBeanRegistration(beanDefinitionRegistry);
        packageBeanRegistration.registerBeans(basePackages);

        this.beanFactory = new DefaultBeanFactory(beanDefinitionRegistry.getRegisteredBeanDefinitions());
        configureSystemBeanPostProcessors(this.beanFactory);
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
        return startupMillis;
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        beanFactory.registerBeanDefinition(name, beanDefinition);
    }

    @Nullable
    @Override
    public final Object getBean(String name) throws BeanNotFoundException {
        return beanFactory.getBean(name);
    }

    @Nullable
    @Override
    public final <T> T getBean(String name, Class<T> requiredType) throws BeanNotFoundException {
        return beanFactory.getBean(name, requiredType);
    }

    @Nullable
    @Override
    public final <T> T getBean(Class<T> requiredType) throws BeanNotFoundException {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public final <T> T createBean(Class<T> beanClass) throws BeanNotFoundException {
        return beanFactory.createBean(beanClass);
    }

    @Override
    public final void registerBean(String name, BeanDefinition beanDefinition, Object beanInstance) {
        beanFactory.registerBean(name, beanDefinition, beanInstance);
    }

    @Override
    public final void addBeanPostProcessor(BeanPostProcessor postProcessor) {
        beanFactory.addBeanPostProcessor(postProcessor);
    }

    private void configureSystemBeanPostProcessors(DefaultBeanFactory beanFactory) {
        this.beanFactory.addBeanPostProcessor(new InitDestroyAnnotationBeanPostProcessor());

        AutowiredAnnotationBeanPostProcessor autowiredPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredPostProcessor.setBeanFactory(beanFactory);
        this.beanFactory.addBeanPostProcessor(autowiredPostProcessor);
    }

    private static final String WINTER_BANNER = """
    >
           *                  *             *
     *           *      *             *             *

     __          __  _   _   _   _____   _____   _____  *
     \\ \\        / / | | | \\ | | |_   _| |  ___| |  __ \\
      \\ \\  /\\  / /  | | |  \\| |   | |   | |_    | |__) |
       \\ \\/  \\/ /   | | | . ` |   | |   |  _|   |  _  /
        \\  /\\  /    | | | |\\  |   | |   | |___  | | \\ \\
         \\/  \\/     |_| |_| \\_|   |_|   |_____| |_|  \\_\\  V: 1.0.0

      *             *              *                 *
             *            *                 *
    """;
}
