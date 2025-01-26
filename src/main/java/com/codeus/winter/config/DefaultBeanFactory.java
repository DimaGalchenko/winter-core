package com.codeus.winter.config;

import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.exception.BeanNotFoundException;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the {@link BeanFactory} interface.
 */
public class DefaultBeanFactory implements BeanFactory {

    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions;
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public DefaultBeanFactory() {
        this.beanDefinitions = new HashMap<>();
    }

    public DefaultBeanFactory(Map<String, BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
    }

    /**
     * Registers a single bean definition into the bean factory.
     *
     * @param name           the name of the bean
     * @param beanDefinition the definition of the bean
     * @throws IllegalArgumentException if a bean with the same name already exists
     */
    @Override
    public void registerBeanDefinition(@Nonnull final String name, @Nonnull final BeanDefinition beanDefinition) {
        if (beanDefinitions.containsKey(name)) {
            throw new BeanFactoryException(String.format("A bean with name '%s' is already defined.", name));
        }
        beanDefinitions.put(name, beanDefinition);
    }

    /**
     * Request beans from the storage with specified parameters.
     *
     * @param name bean's name.
     * @return bean object if its exist or else throw exception.
     */
    @Nullable
    @Override
    public final Object getBean(@Nonnull final String name) throws BeanNotFoundException {
        return Optional.ofNullable(singletonBeans.get(name))
                .orElseThrow(() -> new BeanNotFoundException(String.format("Bean: %s not found", name)));
    }

    /**
     * Request beans from the storage with specified parameters.
     *
     * @param name         bean name
     * @param requiredType required class type
     * @return bean object if its exist or else throw exception.
     */
    @Nullable
    @Override
    public final <T> T getBean(@Nonnull final String name,
                               @Nonnull final Class<T> requiredType) throws BeanNotFoundException {
        Object bean = singletonBeans.get(name);

        if (bean == null) {
            throw new BeanNotFoundException(String.format("Bean with a name %s not found", name));
        }

        if (!requiredType.isAssignableFrom(bean.getClass())) {
            throw new BeanNotFoundException(String.format("Bean with a name %s is not compatible with the type %s",
                    name, requiredType.getName()));
        }

        return requiredType.cast(bean);
    }

    /**
     * Request beans from the storage with specified parameters.
     *
     * @param requiredType required class type
     * @return bean object if its exist or else throw exception.
     */
    @Nullable
    @Override
    public final <T> T getBean(@Nonnull final Class<T> requiredType) throws BeanNotFoundException {
        return singletonBeans.values().stream()
                .filter(requiredType::isInstance)
                .findAny()
                .map(requiredType::cast)
                .orElseThrow(() -> new BeanNotFoundException(
                        String.format("Bean not found for type: %s", requiredType.getName())));
    }

    /**
     * Creating bean object with class type.
     *
     * @param beanClass specified bean class.
     * @return bean object if its not possible throw exception.
     */
    @Override
    public final <T> T createBean(@Nonnull final Class<T> beanClass)
            throws NotUniqueBeanDefinitionException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchMethodException {
        checkBeanClassUniqueness(beanClass);

        Object newBean = beanClass.getDeclaredConstructor().newInstance();
        singletonBeans.put(newBean.getClass().getName(), newBean);
        return beanClass.cast(newBean);
    }

    /**
     * Register bean in the bean's storage.
     *
     * @param name           bean's name.
     * @param beanDefinition bean's BeanDefinition.
     * @param beanInstance   bean's instance.
     */
    @Override
    public final void registerBean(@Nonnull final String name,
                                   @Nonnull final BeanDefinition beanDefinition,
                                   @Nonnull final Object beanInstance) {
        if (beanDefinition.isSingleton()) {
            singletonBeans.put(name, beanInstance);
        }
        beanDefinitions.put(name, beanDefinition);
    }

    /**
     * Adding BeanPostProcessor to the storage.
     *
     * @param postProcessor BeanPostProcessor.
     */
    @Override
    public final void addBeanPostProcessor(@Nonnull final BeanPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
    }

    private <T> void checkBeanClassUniqueness(@Nonnull final Class<T> beanClass) {
        if (singletonBeans.values().stream().anyMatch(beanClass::isInstance)) {
            throw new NotUniqueBeanDefinitionException(
                    String.format("Bean with type '%s' already exists", beanClass.getName()));
        }
    }

    /**
     * Initializes all beans defined in the bean definitions map.
     * <p>
     * This method attempts to initialize each bean, ensuring their dependencies are resolved.
     * If a bean cannot be initialized due to unresolved dependencies, it is added to a pending list.
     * The method iteratively resolves dependencies for pending beans until all beans are initialized
     * or a circular or unresolved dependency is detected, which results in an exception.
     * </p>
     * <p>
     * <b>Note:</b> All postProcessors should be added before calling this method
     * to ensure they are applied during the bean initialization process.
     * </p>
     *
     * @throws BeanFactoryException if some beans have unresolved dependencies after attempting to initialize them.
     */
    public void initializeBeans() {
        Map<String, Boolean> initializationStatuses = new HashMap<>();
        List<String> pendingBeans = new ArrayList<>();

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            boolean isInitialized = tryInitializeBean(beanName, beanDefinition, initializationStatuses);

            if (!isInitialized) {
                pendingBeans.add(beanName);
            }
        }

        boolean isAnyBeanResolved;
        while (!pendingBeans.isEmpty()) {
            isAnyBeanResolved = false;
            List<String> stillPendingBeans = new ArrayList<>();

            for (String beanName : pendingBeans) {
                BeanDefinition beanDefinition = beanDefinitions.get(beanName);
                boolean isInitialized = tryInitializeBean(beanName, beanDefinition, initializationStatuses);
                if (isInitialized) {
                    isAnyBeanResolved = true;
                } else {
                    stillPendingBeans.add(beanName);
                }
            }

            if (!isAnyBeanResolved) {
                throw new BeanFactoryException("Unresolved dependencies for beans: " + stillPendingBeans);
            }
            pendingBeans = stillPendingBeans;
        }
    }

    private boolean tryInitializeBean(
            String beanName,
            BeanDefinition beanDefinition,
            Map<String, Boolean> initializationStatuses) {

        if (Boolean.TRUE.equals(initializationStatuses.get(beanName))) {
            return false;
        }

        initializationStatuses.put(beanName, false);

        String[] dependsOn = beanDefinition.getDependsOn();
        if (dependsOn != null) {
            for (String dependency : dependsOn) {
                BeanDefinition dependencyBeanDefinition = Optional.ofNullable(beanDefinitions.get(dependency))
                        .orElseThrow(() -> new BeanFactoryException("Dependency not found for bean: " + dependency));

                if (!singletonBeans.containsKey(dependency)) {
                    return false;
                }
            }
        }

        Object beanInstance = createBeanInstance(beanName, beanDefinition);
        beanInstance = applyPostProcessorsBeforeInitialization(beanInstance, beanName);
        beanInstance = applyPostProcessorsAfterInitialization(beanInstance, beanName);
        singletonBeans.put(beanName, beanInstance);

        initializationStatuses.put(beanName, true);
        return true;
    }

    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition) {
        String className = Optional.ofNullable(beanDefinition.getBeanClassName())
                .orElseThrow(() -> new BeanFactoryException("Bean class name is not set for bean: " + beanName));

        try {
            Class<?> beanClass = Class.forName(className);

            return resolveConstructor(beanClass);
        } catch (ClassNotFoundException e) {
            throw new BeanFactoryException("Class with name not found: " + className, e);
        }
    }

    private Object resolveConstructor(Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Object[] resolvedDependencies = new Object[parameterTypes.length];
            boolean canResolve = true;

            for (int i = 0; i < parameterTypes.length; i++) {
                Type dependencyType = parameterTypes[i];
                Object dependency = getBeanDependency(dependencyType);
                if (dependency == null) {
                    canResolve = false;
                    break;
                }
                resolvedDependencies[i] = dependency;
            }

            if (canResolve) {
                try {
                    return constructor.newInstance(resolvedDependencies);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new BeanFactoryException("Unable to create bean instance due to: " + e.getMessage(), e);
                }
            }
        }
        throw new BeanFactoryException("Unable to resolve dependencies for class: " + beanClass.getName());
    }

    private Object getBeanDependency(Type dependencyType) {
        Object dependency;
        Type rawType = getRawType(dependencyType);
        if (rawType.equals(List.class)) {
            dependency = getCollectionDependency(dependencyType, 0).toList();
        } else if (rawType.equals(Set.class)) {
            dependency = getCollectionDependency(dependencyType, 0).collect(Collectors.toSet());
        } else if (rawType.equals(Map.class)) {
            dependency = getCollectionDependency(dependencyType, 1)
                .collect(Collectors.toMap(bean -> bean.getClass().getName(), bean -> bean));
        } else {
            dependency = singletonBeans.values().stream()
                .filter(bean -> bean.getClass().equals(rawType))
                .findFirst()
                .orElse(null);
        }
        return dependency;
    }

    private Type getRawType(Type dependencyType) {
        return dependencyType instanceof ParameterizedType
            ? ((ParameterizedType) dependencyType).getRawType()
            : dependencyType;
    }

    private Stream<Object> getCollectionDependency(Type parameterType, int valueTypeIndex) {
        Class<?> dependencyClass =
            (Class<?>) ((ParameterizedType) parameterType).getActualTypeArguments()[valueTypeIndex];
        return singletonBeans.values().stream()
            .filter(bean -> Arrays.stream(bean.getClass().getInterfaces())
                .anyMatch(interfaceClass -> interfaceClass.isAssignableFrom(dependencyClass)));
    }

    private Object applyPostProcessorsBeforeInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor postProcessor : postProcessors) {
            result = applyPostProcessor(postProcessor::postProcessBeforeInitialization, bean, beanName);
        }
        return result;
    }

    private Object applyPostProcessorsAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor postProcessor : postProcessors) {
            result = applyPostProcessor(postProcessor::postProcessAfterInitialization, bean, beanName);
        }
        return result;
    }

    private Object applyPostProcessor(BiFunction<Object, String, Object> postProcessorFunction,
                                      Object bean,
                                      String beanName) {
        return Optional.ofNullable(postProcessorFunction.apply(bean, beanName))
                .orElseThrow(() -> new BeanFactoryException(String.format(
                        "PostProcessor returned null for bean: %s during post processing", beanName)));
    }
}
