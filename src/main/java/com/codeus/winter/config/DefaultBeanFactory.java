package com.codeus.winter.config;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.exception.BeanCurrentlyInCreationException;
import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.exception.BeanNotFoundException;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the {@link BeanFactory} interface.
 */
public class DefaultBeanFactory extends AutowireCapableBeanFactory {

    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final Map<String, BeanDefinition> beanDefinitions;
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();
    private final Set<String> singletonsCurrentlyInCreation = new HashSet<>(16);

    private final ConstructorResolver constructorResolver;

    public DefaultBeanFactory() {
        this.beanDefinitions = new HashMap<>();
        this.constructorResolver = new ConstructorResolver(this);
    }

    public DefaultBeanFactory(Map<String, BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
        this.constructorResolver = new ConstructorResolver(this);
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
    //TODO 1
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
    //TODO 1
    public final <T> T getBean(@Nonnull final String name,
                               @Nonnull final Class<T> requiredType) throws BeanNotFoundException {
        Object bean = singletonBeans.get(name);

        if (bean == null) {
            throw new BeanNotFoundException(String.format("Bean with a name %s not found", name));
        }

        //TODO: after shelving, keep this check, because retrieved bean could have non-compatible class with `requiredType`
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
    //TODO 1: all getBean methods should call the new getBean method that would try to retrieve bean, if unsuccessful - create it
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
    //TODO 2 (other ticket scope): this should be adjusted to create beans with PROTOTYPE scope
    //TODO 3 : add the new class to beanDefinitions as well (at least for some time)
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
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            getOrCreateBean(beanName, beanDefinition);
        }
    }

    //TODO[MAJOR]: when retrieving Class<?> or Constructor<?> add logic to check if the given class is a proxy or not
    // this is not required for main logic but is required for tests (e.g. Bean Mocks)
    private Object getOrCreateBean(String beanName, BeanDefinition beanDefinition) {
        //TODO: add logic of initialization of `beanDefinition.getDependsOn()`

        if (beanDefinition.isSingleton()) {
            return getSingleton(beanName, beanDefinition);
        } else {
            // place for PROTOTYPE scope logic
            return null;
        }
    }

    private Object getSingleton(String beanName, BeanDefinition beanDefinition) {
        Object singleton = singletonBeans.get(beanName);

        if (singleton != null) return singleton;
        else {
            beforeSingletonCreation(beanName);
            Object beanInstance = createBean(beanName, beanDefinition);
            afterSingletonCreation(beanName);
            singletonBeans.put(beanName, beanInstance);

            return beanInstance;
        }
    }

    private void beforeSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    private void afterSingletonCreation(String beanName) {
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Bean %s is not currently in creation.".formatted(beanName));
        }
    }

    /**
     * @param beanName
     * @param beanDefinition
     * @return an instance of a fully configured bean.
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object beanInstance;
        Constructor<?> autowiringConstructor = findAutowiringConstructor(beanName, beanDefinition);

        if (autowiringConstructor != null)
            beanInstance = constructorResolver.autowireConstructor(autowiringConstructor);
        else
            beanInstance = instantiateBean(beanName, beanDefinition);

        beanInstance = applyPostProcessorsBeforeInitialization(beanInstance, beanName);
        beanInstance = applyPostProcessorsAfterInitialization(beanInstance, beanName);

        return beanInstance;
    }

    /**
     * Searches for a public constructor for a given bean's name and definition that can be used for autowiring the bean.
     * The found constructor is never a default constructor or constructor without arguments.
     *
     * @param beanName a name of a bean to resolve autowiring constructor for.
     * @param beanDefinition a definition of a bean to resolve autowiring constructor for.
     * @return a public constructor ready for autowiring, null - otherwise.
     * @throws RuntimeException if Bean class has two or more constructors marked with the {@link Autowired) annotation.
     */
    private Constructor<?> findAutowiringConstructor(String beanName,
                                                     BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanName, beanDefinition);
        Class<?> beanClass = resolveClass(className, beanName);

        Constructor<?>[] constructors = beanClass.getConstructors();
        List<Constructor<?>> candidates = new ArrayList<>();
        Constructor<?> explicitAutowiringConstructor = null;
        for (Constructor<?> constructor : constructors) {
            //TODO: add logic to handle a class with two constructors and one of the is default (no arguments)
            //TODO: add logic to handle multiple constructors with @Autowired annotation
            if (hasAutowiredAnnotation(constructor)) {
                explicitAutowiringConstructor = constructor;
            }
            candidates.add(constructor);
        }

        if (candidates.size() == 1) return candidates.getFirst();
        else if (explicitAutowiringConstructor != null) return explicitAutowiringConstructor;
        else if (candidates.size() > 1) {
            //TODO: adjust error message, remove `autowire annotation`
            throw new BeanFactoryException("Cannot create bean for class %s, it has multiple constructors marked with autowire annotation".formatted(beanClass));
        } else {
            //TODO: print some message
            System.out.println("No autowiring constructor found");
            return null;
        }
    }

    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanName, beanDefinition);
        Class<?> beanClass = resolveClass(className, beanName);

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
     *
     * @param descriptor a dependency descriptor for resolving a bean.
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

    protected List<Object> resolveMultipleDependencies(DependencyDescriptor descriptor) {
        Class<?> dependencyClass = descriptor.getDependencyClass();
        List<Object> dependencies = new ArrayList<>();

        for (Map.Entry<String, BeanDefinition> candidate : findCandidates(dependencyClass)) {
            String beanName = candidate.getKey();
            BeanDefinition beanDefinition = candidate.getValue();

            dependencies.add(getOrCreateBean(beanName, beanDefinition));
        }

        return dependencies;
    }

    protected Object resolveSingleDependency(DependencyDescriptor descriptor) {
        Class<?> dependencyClass = descriptor.getDependencyClass();
        return resolveBean(dependencyClass);
    }

    /**
     * Find all available bean candidates' names and definitions in the bean definitions that conform given target class.
     *
     * @param targetClass a class to find bean candidates for.
     * @return a list of bean candidates' names and definitions that are assignable from the given target class.
     * @throws BeanFactoryException if a candidate class from bean definitions doesn't exist.
     */
    protected List<Map.Entry<String, BeanDefinition>> findCandidates(Class<?> targetClass) {
        List<Map.Entry<String, BeanDefinition>> candidates = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> definitionEntry : beanDefinitions.entrySet()) {
            String beanName = definitionEntry.getKey();
            BeanDefinition candidateDefinition = definitionEntry.getValue();
            Class<?> candidateClass = resolveClass(candidateDefinition.getBeanClassName(), beanName);

            if (targetClass.isAssignableFrom(candidateClass)) candidates.add(definitionEntry);
        }

        return candidates;
    }

    /**
     * Resolves bean instance for given class.
     *
     * @param beanClass a bean class to resolve for.
     * @return bean instance that is assignable from the given class.
     * @throws BeanFactoryException             if no BeanDefinition available for the given class.
     * @throws NotUniqueBeanDefinitionException if multiple candidates available for the given class,
     *                                          and it is not possible to determine the required one (missing qualifier metadata)
     */
    protected Object resolveBean(Class<?> beanClass) {
        List<Map.Entry<String, BeanDefinition>> candidates = findCandidates(beanClass);

        if (candidates.isEmpty())
            throw new BeanFactoryException("Cannot resolve bean for type='%s', no bean definitions available".formatted(beanClass.getName()));

        Map.Entry<String, BeanDefinition> targetCandidate;
        if (candidates.size() == 1) {
            targetCandidate = candidates.getFirst();
        } else {
            //TODO #35, #46: there are multiple candidates, add logic to choose one based on @Primary, @Qualifier or other util annotation.
            String candidateClasses = candidates.stream()
                    .map(candidate -> candidate.getValue().getBeanClassName())
                    .sorted(Comparator.nullsLast(Comparator.naturalOrder()))
                    .collect(Collectors.joining(", "));
            throw new NotUniqueBeanDefinitionException("Cannot resolve bean for type='%s', multiple beans are available: %s".formatted(beanClass.getName(), candidateClasses));
        }

        return getOrCreateBean(targetCandidate.getKey(), targetCandidate.getValue());
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


    // UTILS //TODO: consider distributing to separate classes

    private boolean hasAutowiredAnnotation(Constructor<?> constructor) {
        return constructor.getAnnotation(Autowired.class) != null;
    }

    //Can be moved to BeanDefinition but requires interface/class adjustment
    private static String retrieveBeanClassName(String beanName, BeanDefinition beanDefinition) {
        return Optional.ofNullable(beanDefinition.getBeanClassName())
                .orElseThrow(() -> new BeanFactoryException("Bean class name is not set for bean: " + beanName));
    }


    private static Class<?> resolveClass(String className, String beanName) {
        Class<?> beanClass;
        try {
            beanClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeanFactoryException("Class not found by name='%s' for bean='%s': ".formatted(className, beanName), e);
        }
        return beanClass;
    }
}
