package com.codeus.winter.config;

import com.codeus.winter.config.impl.BeanDefinitionImpl;
import com.codeus.winter.exception.BeanCurrentlyInCreationException;
import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.exception.BeanNotFoundException;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import com.codeus.winter.util.ClassUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the {@link BeanFactory} interface.
 */
public class DefaultBeanFactory extends AbstractAutowireCapableBeanFactory {

    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions;
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();
    private final Set<String> singletonsCurrentlyInCreation = new HashSet<>(16);

    public DefaultBeanFactory() {
        this(new HashMap<>());
    }

    public DefaultBeanFactory(Map<String, BeanDefinition> beanDefinitions) {
        super();
        //TODO should we make a copy, so it won't be possible to modify beanDefinitions from outside?
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
     * Returns the singleton bean object of this application context for specified name.
     *
     * @param name bean's name.
     * @return the singleton bean object of this context.
     * @throws BeanNotFoundException if bean not found for specified name.
     */
    @Override
    public final Object getBean(@Nonnull final String name) throws BeanNotFoundException {
        BeanDefinition beanDefinition = getBeanDefinition(name);
        return getBean(name, beanDefinition);
    }

    /**
     * Returns the singleton bean object of this application context for specified name
     * and cast it to the specified class type.
     *
     * @param name         bean name
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the singleton bean object of this context.
     * @throws BeanNotFoundException if bean not found for specified name and type.
     */
    @Override
    public final <T> T getBean(@Nonnull final String name,
                               @Nonnull final Class<T> requiredType) throws BeanNotFoundException {
        BeanDefinition beanDefinition = getBeanDefinition(name);
        Object bean = getBean(name, beanDefinition);

        if (!requiredType.isAssignableFrom(bean.getClass())) {
            throw new BeanNotFoundException(String.format("Bean with a name %s is not compatible with the type %s",
                    name, requiredType.getName()));
        }

        return requiredType.cast(bean);
    }

    /**
     * Returns the singleton bean object of this application context for the specified class type.
     *
     * @param requiredType required class type
     * @param <T>          the type of the bean
     * @return the singleton bean object of this context.
     * @throws BeanNotFoundException if bean not found for specified type.
     */
    @Override
    public final <T> T getBean(@Nonnull final Class<T> requiredType) throws BeanNotFoundException {
        Object bean = resolveBean(requiredType);
        if (bean == null) {
            throw new BeanNotFoundException("Bean for type=%s not found".formatted(requiredType.getName()));
        }

        return requiredType.cast(bean);
    }

    /**
     * Creates bean object with class type.
     *
     * @param beanClass specified bean class.
     * @return bean object if its not possible throw exception.
     */
    @Override
    //TODO (other ticket scope): this should be adjusted to create beans with PROTOTYPE scope
    public final <T> T createBean(@Nonnull final Class<T> beanClass)
            throws NotUniqueBeanDefinitionException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchMethodException {
        checkBeanClassUniqueness(beanClass);

        //TODO handle direct BeanDefinitionImpl instantiation, add some factory method
        BeanDefinitionImpl beanDefinition = new BeanDefinitionImpl();
        beanDefinition.setBeanClassName(beanClass.getName());
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        Object newBean = beanClass.getDeclaredConstructor().newInstance();
        singletonBeans.put(newBean.getClass().getName(), newBean);
        beanDefinitions.put(newBean.getClass().getName(), beanDefinition);
        return beanClass.cast(newBean);
    }

    /**
     * Registers bean in the bean's storage.
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
     * Adds BeanPostProcessor to the storage.
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
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            getBean(beanName, beanDefinition);
        }
    }

    /**
     * Delegates bean retrieval further depending on the bean's scope.
     * Currently, only two scopes supported: singleton and prototype.
     *
     * @param beanName       a name of a bean to retrieve.
     * @param beanDefinition a definition of a bean to retrieve.
     * @return a bean instance.
     * @throws IllegalArgumentException if given beanDefinition has {@link Scope#PROTOTYPE} scope.
     */
    private Object getBean(String beanName, BeanDefinition beanDefinition) {
        if (beanDefinition.isSingleton()) {
            return getSingleton(beanName, beanDefinition);
        } else {
            // place for PROTOTYPE scope logic
            throw new IllegalArgumentException(
                    "DefaultBeanFactory cannot create bean (name='%s') with the PROTOTYPE scope.".formatted(beanName));
        }
    }

    /**
     * Retrieves an existing singleton bean by bean's name and definition.
     * Can create a singleton bean if it is not created yet.
     *
     * @param beanName       a name of a singleton bean to retrieve or create.
     * @param beanDefinition a definition of a singleton bean to retrieve or create.
     * @return a singleton bean.
     */
    private Object getSingleton(String beanName, BeanDefinition beanDefinition) {
        Object singleton = singletonBeans.get(beanName);

        if (singleton != null) {
            return singleton;
        } else {
            beforeSingletonCreation(beanName);
            Object beanInstance = createBean(beanName, beanDefinition);
            afterSingletonCreation(beanName);
            singletonBeans.put(beanName, beanInstance);

            return beanInstance;
        }
    }

    /**
     * Registers a bean as one that is currently in creation.
     * When bean is registered as "currently in creation", it cannot be injected into another bean.
     * Mainly used to detect cyclic-dependencies.
     *
     * @param beanName a name of a bean to register.
     * @throws BeanCurrentlyInCreationException if the same bean name passed twice
     *                                          without calling {@link DefaultBeanFactory#afterSingletonCreation}
     *                                          for it in between.
     */
    private void beforeSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    /**
     * Removes a bean from the "currently in creation" list.
     * Mainly used to detect cyclic-dependencies.
     *
     * @param beanName a name of a bean to deregister.
     * @throws IllegalStateException if the same bean name passed twice that means an incorrect usage of this method.
     */
    private void afterSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Bean %s is not currently in creation.".formatted(beanName));
        }
    }

    /**
     * Creates a bean and configures it using registered bean post processors.
     * The creation process involves checking if bean has autowiring constructor.
     * If not, it is instantiated by the default constructor.
     *
     * @param beanName       a name of a bean to create and customize.
     * @param beanDefinition a definition of a bean to create and customize.
     * @return an instance of a fully configured bean.
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object beanInstance;
        Constructor<?> autowiringConstructor = findAutowiringConstructor(beanName, beanDefinition);

        if (autowiringConstructor != null) {
            beanInstance = autowireConstructor(autowiringConstructor);
        } else {
            beanInstance = instantiateBean(beanName, beanDefinition);
        }

        beanInstance = applyPostProcessorsBeforeInitialization(beanInstance, beanName);
        beanInstance = applyPostProcessorsAfterInitialization(beanInstance, beanName);

        return beanInstance;
    }

    /**
     * Instantiates bean by the default constructor using given bean's name and definition.
     *
     * @param beanName       a name of a bean to instantiate.
     * @param beanDefinition a definition of a bean to instantiate.
     * @return a bean instance.
     * @throws BeanFactoryException if Bean class doesn't have a public default constructor, or it is not accessible.
     *                              May contain exceptions thrown by the constructor.
     */
    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanDefinition, beanName);
        Class<?> beanClass = ClassUtils.resolveClass(className);

        try {
            return beanClass.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new BeanFactoryException("Class has no public default constructor: " + className, e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanFactoryException("Unable to create bean instance due to: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves Bean instance using given {@link DependencyDescriptor}.
     * Can collect multiple candidates beans into a List, Set or Map.
     * Formed Map contains bean name as a key, bean instance as a value.
     *
     * @param descriptor a dependency descriptor to resolve a bean.
     * @return bean instance that conform the given {@link DependencyDescriptor}.
     */
    @Override
    protected Object resolveDependency(DependencyDescriptor descriptor) {
        Object dependency;
        Class<?> dependencyClass = descriptor.getDependencyClass();
        Type dependencyType = descriptor.getDependencyType();

        if (dependencyClass.equals(List.class)) {
            dependency = getCollectionDependency(dependencyType, 0).toList();
        } else if (dependencyClass.equals(Set.class)) {
            dependency = getCollectionDependency(dependencyType, 0).collect(Collectors.toSet());
        } else if (dependencyClass.equals(Map.class)) {
            dependency = getCollectionDependency(dependencyType, 1)
                    .collect(Collectors.toMap(bean -> bean.getClass().getName(), bean -> bean));
        } else {
            dependency = resolveSingleDependency(descriptor);
        }
        return dependency;
    }

    private Stream<Object> getCollectionDependency(Type parameterType, int valueTypeIndex) {
        Type dependencyType = ((ParameterizedType) parameterType).getActualTypeArguments()[valueTypeIndex];
        DependencyDescriptor descriptor = new DependencyDescriptor(dependencyType);
        return resolveMultipleDependencies(descriptor).stream();
    }

    /**
     * Resolves multiple candidate dependencies by given {@link DependencyDescriptor}.
     *
     * @param descriptor a dependency descriptor to resolve bean candidates for.
     * @return a list of bean candidate instances.
     */
    protected List<Object> resolveMultipleDependencies(DependencyDescriptor descriptor) {
        Class<?> dependencyClass = descriptor.getDependencyClass();
        List<Object> dependencies = new ArrayList<>();

        List<Map.Entry<String, BeanDefinition>> candidates = findCandidates(dependencyClass);
        if (candidates.isEmpty()) {
            throw new BeanNotFoundException(
                "Cannot resolve bean for type='%s', no bean definition available".formatted(dependencyClass.getName()));
        }

        for (Map.Entry<String, BeanDefinition> candidate : candidates) {
            String beanName = candidate.getKey();
            BeanDefinition beanDefinition = candidate.getValue();

            dependencies.add(getBean(beanName, beanDefinition));
        }

        return dependencies;
    }

    /**
     * Resolves single dependency by given {@link DependencyDescriptor}.
     *
     * @param descriptor a dependency descriptor to resolve a bean for.
     * @return a bean instance.
     */
    protected Object resolveSingleDependency(DependencyDescriptor descriptor) {
        Class<?> dependencyClass = descriptor.getDependencyClass();
        Object resolvedBean = resolveBean(dependencyClass);
        if (resolvedBean == null) {
            throw new BeanNotFoundException(
                "Cannot resolve bean for type='%s', no bean definition available".formatted(dependencyClass.getName()));
        }

        return resolvedBean;
    }

    /**
     * Finds all available bean candidates' names and definitions in the bean definitions that conform
     * given target class.
     *
     * @param targetClass a class to find bean candidates for.
     * @return a list of bean candidates' names and definitions that are assignable from the given target class.
     * @throws BeanFactoryException if a candidate class from bean definitions doesn't exist.
     */
    protected List<Map.Entry<String, BeanDefinition>> findCandidates(Class<?> targetClass) {
        List<Map.Entry<String, BeanDefinition>> candidates = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> definitionEntry : beanDefinitions.entrySet()) {
            BeanDefinition candidateDefinition = definitionEntry.getValue();
            Class<?> candidateClass = ClassUtils.resolveClass(candidateDefinition.getBeanClassName());

            if (targetClass.isAssignableFrom(candidateClass)) {
                candidates.add(definitionEntry);
            }
        }

        return candidates;
    }

    /**
     * Resolves bean instance for given class. Handles multiple candidates using qualifier annotations.
     *
     * @param beanClass a bean class to resolve for.
     * @return bean instance that is assignable from the given class,
     * {@code null} - if no candidates found for given class.
     * @throws NotUniqueBeanDefinitionException if multiple candidates available for the given class,
     *                                          and it is not possible to determine the required one
     *                                          (missing qualifier metadata)
     */
    @Nullable
    protected Object resolveBean(Class<?> beanClass) {
        List<Map.Entry<String, BeanDefinition>> candidates = findCandidates(beanClass);

        if (candidates.isEmpty()) {
            return null;
        }

        Map.Entry<String, BeanDefinition> targetCandidate;
        if (candidates.size() == 1) {
            targetCandidate = candidates.getFirst();
        } else {
            //TODO #35, #46: there are multiple candidates,
            // add logic to choose one based on @Primary, @Qualifier or other util annotation.
            String candidateClasses = candidates.stream()
                    .map(candidate -> candidate.getValue().getBeanClassName())
                    .sorted(Comparator.nullsLast(Comparator.naturalOrder()))
                    .collect(Collectors.joining(", "));
            throw new NotUniqueBeanDefinitionException(
                    "Cannot resolve bean for type='%s', multiple beans are available: %s"
                            .formatted(beanClass.getName(), candidateClasses));
        }

        return getBean(targetCandidate.getKey(), targetCandidate.getValue());
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


    /**
     * Convenient method to retrieve {@link BeanDefinition} by name and throw exception if missing.
     *
     * @param name a name of a bean which definition to retrieve.
     * @return bean definition associated with given name.
     * @throws BeanNotFoundException if no bean definition registered for given name.
     */
    private BeanDefinition getBeanDefinition(String name) {
        BeanDefinition beanDefinition = beanDefinitions.get(name);
        if (beanDefinition == null) {
            throw new BeanNotFoundException("Bean for name='%s' not found".formatted(name));
        }

        return beanDefinition;
    }
}
