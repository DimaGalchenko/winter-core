package com.codeus.winter.config;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.annotation.Component;
import com.codeus.winter.annotation.PostConstruct;
import com.codeus.winter.annotation.PreDestroy;
import com.codeus.winter.annotation.Primary;
import com.codeus.winter.annotation.Scope;
import com.codeus.winter.config.impl.BeanDefinitionImpl;
import com.codeus.winter.config.impl.PackageScannerImpl;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Responsible for scanning a package for classes annotated with {@link Component},
 * and registering their {@link BeanDefinition}s in the provided {@link BeanDefinitionRegistry}.
 */
public class PackageBeanRegistration {

    private final PackageScanner packageScanner;
    private final BeanDefinitionRegistry registry;

    /**
     * Constructor to initialize the package scanner and the registry.
     *
     * @param registry       the {@link BeanDefinitionRegistry} to register bean definitions
     */
    public PackageBeanRegistration(BeanDefinitionRegistry registry) {
        this.packageScanner = new PackageScannerImpl();
        this.registry = registry;
    }

    /**
     * Scans the specified package for classes annotated with {@link Component} and
     * registers their {@link BeanDefinition}s in the registry.
     *
     * @param basePackages packages to scan for annotated classes
     */
    public void registerBeans(String... basePackages) {
        notEmpty(basePackages);
        Set<Class<?>> componentClasses = Arrays.stream(basePackages)
            .map(basePackage -> packageScanner.findClassesWithAnnotations(basePackage,
                Set.of(Component.class)))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        if (!componentClasses.isEmpty()) {
            for (Class<?> clazz : componentClasses) {
                String beanName = getBeanName(clazz);

                BeanDefinitionImpl beanDefinition = new BeanDefinitionImpl();
                processCommonDefinitionAnnotations(clazz, beanDefinition);

                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, beanDefinition);
                } else {
                    throw new NotUniqueBeanDefinitionException(
                        String.format(
                            "A bean with the name '%s' is already defined in the registry.",
                            beanName)
                    );
                }
            }
        }
    }

    private void notEmpty(Object[] array) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException("At least one base package must be specified");
        }
    }

    private void processCommonDefinitionAnnotations(Class<?> clazz, BeanDefinition beanDefinition) {
        beanDefinition.setBeanClassName(clazz.getName());
        beanDefinition.setInjectCandidate(true);

        if (clazz.isAnnotationPresent(Primary.class)) {
            beanDefinition.setPrimary(true);
        }

        if (clazz.isAnnotationPresent(Scope.class)) {
            String value = clazz.getAnnotation(Scope.class).value().toLowerCase();
            String scope = value.equals("prototype") ? BeanDefinition.SCOPE_PROTOTYPE
                : BeanDefinition.SCOPE_SINGLETON;
            beanDefinition.setScope(scope);
        }

        List<String> dependencies = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (dependencies.contains(field.getType().getName())) {
                    continue;
                }
                dependencies.add(field.getType().getName());
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Autowired.class)) {
                for (Parameter parameter : method.getParameters()) {
                    if (dependencies.contains(parameter.getType().getName())) {
                        continue;
                    }
                    dependencies.add(parameter.getType().getName());
                }
            }

            if (method.isAnnotationPresent(PostConstruct.class)) {
                beanDefinition.setInitMethodName(method.getName());
            }

            if (method.isAnnotationPresent(PreDestroy.class)) {
                beanDefinition.setDestroyMethodName(method.getName());
            }
        }

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                for (Parameter parameter : constructor.getParameters()) {
                    if (dependencies.contains(parameter.getType().getName())) {
                        continue;
                    }
                    dependencies.add(parameter.getType().getName());
                }
            }
        }
        beanDefinition.setDependsOn(dependencies.toArray(String[]::new));
    }

    /**
     * Generates a bean name based on the class name.
     * <p>
     * This logic can be replaced with more robust naming strategies if needed.
     *
     * @param clazz the class for which to generate the bean name
     * @return the generated bean name
     */
    private String getBeanName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
