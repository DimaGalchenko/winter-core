package com.codeus.winter.config;

import com.codeus.winter.config.impl.PackageScannerImpl;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A bean definition scanner that detects bean candidates on the classpath,
 * registering corresponding bean definitions with a given registry ({@code BeanFactory}
 * or {@code ApplicationContext}).
 **/
public class ClassPathBeanDefinitionScanner {
    private final BeanDefinitionRegistry registry;
    private final PackageScanner packageScanner;
    private final PackageBeanRegistration packageBeanRegistration;

    /**
     * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
     *
     * @param registry the {@code BeanFactory} to load bean definitions into, in the form
     *                 of a {@code BeanDefinitionRegistry}
     */
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.packageScanner = new PackageScannerImpl();
        this.packageBeanRegistration = new PackageBeanRegistration(packageScanner, registry);
    }

    /**
     * Perform a scan within the specified base packages.
     *
     * @param basePackages the packages to check for annotated classes
     * @return number of beans registered
     */
    public int scanPackages(String... basePackages) {
        doScan(basePackages);
        return registry.getBeanDefinitionCount();
    }

    /**
     * Perform a scan within the specified base packages, returning bean definitions.
     * @param basePackages the packages to check for annotated classes
     * @return set of beans registered if any for tooling registration purposes (never {@code null})
     */
    private void doScan(String... basePackages) {
        notEmpty(basePackages, "At least one base package must be specified");
        for (String basePackage : basePackages) {
            packageBeanRegistration.registerBeans(basePackage);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }
}
