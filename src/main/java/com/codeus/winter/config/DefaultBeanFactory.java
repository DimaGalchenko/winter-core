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
        Constructor<?> autowiringConstructor = resolveAutowiringConstructor(beanName, beanDefinition);

        if (autowiringConstructor != null)
            beanInstance = constructorResolver.autowireConstructor(autowiringConstructor);
        else
            beanInstance = instantiateBean(beanName, beanDefinition);

        beanInstance = applyPostProcessorsBeforeInitialization(beanInstance, beanName);
        beanInstance = applyPostProcessorsAfterInitialization(beanInstance, beanName);

        return beanInstance;
    }

    @Override
    protected Object resolveDependency(String dependencyBeanName, Class<?> dependencyClass) {
        List<Map.Entry<String, BeanDefinition>> candidates = new ArrayList<>();
        //TODO: consider moving this `for-clause` into a separate method, or even somehow re-use `DefaultBeanFactory.getBean(java.lang.String, java.lang.Class<T>)`
        for (Map.Entry<String, BeanDefinition> definitionEntry : beanDefinitions.entrySet()) {
            BeanDefinition candidateDefinition = definitionEntry.getValue();

            try {
                Class<?> candidateClass = Class.forName(candidateDefinition.getBeanClassName());

                if (dependencyClass.isAssignableFrom(candidateClass)) {
                    candidates.add(definitionEntry);
                }
            } catch (ClassNotFoundException e) {
                throw new BeanFactoryException("Class with name not found: " + candidateDefinition.getBeanClassName(), e);
            }
        }

        if (candidates.isEmpty())
            throw new BeanFactoryException("Cannot find bean definition for class='%s'".formatted(dependencyClass.getName()));

        Map.Entry<String, BeanDefinition> targetCandidate;
        if (candidates.size() == 1) {
            targetCandidate = candidates.getFirst();
        } else {
            //TODO #35, #46: there are multiple candidates, add logic to choose one based on @Primary, @Qualifier or other util annotation.
            String candidateClasses = candidates.stream().map(candidate -> candidate.getValue().getBeanClassName()).collect(Collectors.joining(", "));
            throw new NotUniqueBeanDefinitionException("Cannot bean for class=%s, multiple beans are available for it: %s".formatted(dependencyClass, candidateClasses));
        }

        return getOrCreateBean(targetCandidate.getKey(), targetCandidate.getValue());
    }


    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanName, beanDefinition);

        try {
            Class<?> beanClass = Class.forName(className);
            return beanClass.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new BeanFactoryException("Class with name not found: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new BeanFactoryException("Class has no public default constructor: " + className, e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanFactoryException("Unable to create bean instance due to: " + e.getMessage(), e);
        }
    }

    /**
     * ...
     * Searches only public constructors for Autowiring.
     *
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Constructor<?> resolveAutowiringConstructor(String beanName,
                                                        BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanName, beanDefinition);
        Class<?> beanClass;
        try {
            beanClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeanFactoryException("Class with name not found: " + beanName, e);
        }

        Constructor<?>[] constructors = beanClass.getConstructors();
        List<Constructor<?>> candidates = new ArrayList<>();
        Constructor<?> explicitAutowiringConstructor = null;
        for (Constructor<?> constructor : constructors) {
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


    // UTILS //TODO: consider distributing to separate classes

    private boolean hasAutowiredAnnotation(Constructor<?> constructor) {
        return constructor.getAnnotation(Autowired.class) != null;
    }

    //Can be moved to BeanDefinition but requires interface/class adjustment
    private static String retrieveBeanClassName(String beanName, BeanDefinition beanDefinition) {
        return Optional.ofNullable(beanDefinition.getBeanClassName())
                .orElseThrow(() -> new BeanFactoryException("Bean class name is not set for bean: " + beanName));
    }
}
