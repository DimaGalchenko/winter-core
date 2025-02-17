package com.codeus.winter.config;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.util.ClassUtils;
import jakarta.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An extension of {@link BeanFactory} that provides extra logic for bean resolving and autowiring.
 */
public abstract class AbstractAutowireCapableBeanFactory implements BeanFactory {
    private final ConstructorResolver constructorResolver;

    protected AbstractAutowireCapableBeanFactory() {
        this.constructorResolver = new ConstructorResolver(this);
    }

    /**
     * Resolves Bean instance using given {@link DependencyDescriptor}.
     * Can collect multiple candidates beans into a List, Set or Map.
     * Formed Map contains bean name as a key, bean instance as a value.
     *
     * @param descriptor a dependency descriptor to resolve a bean.
     * @return bean instance that conform the given {@link DependencyDescriptor}.
     */
    protected abstract Object resolveDependency(DependencyDescriptor descriptor);

    /**
     * Delegates constructor autowiring to the {@link ConstructorResolver}.
     *
     * @param constructor a constructor to autowire.
     * @return a bean instance with autowired arguments.
     */
    protected Object autowireConstructor(Constructor<?> constructor) {
        return constructorResolver.autowireConstructor(constructor);
    }

    /**
     * Searches for a public constructor for a given bean's name and definition that can be used for bean autowiring.
     * The found constructor is never a default constructor (constructor without arguments).
     *
     * @param beanName       a name of a bean to resolve autowiring constructor for.
     * @param beanDefinition a definition of a bean to resolve autowiring constructor for.
     * @return a public constructor ready for autowiring, null - otherwise.
     * @throws RuntimeException if Bean class has two or more constructors marked with the {@link Autowired} annotation.
     */
    @Nullable
    protected Constructor<?> findAutowiringConstructor(String beanName,
                                                       BeanDefinition beanDefinition) {
        String className = retrieveBeanClassName(beanDefinition, beanName);
        Class<?> beanClass = ClassUtils.resolveClass(className);
        Constructor<?>[] constructors = beanClass.getConstructors();

        List<Constructor<?>> candidates = new ArrayList<>();
        Constructor<?> explicitAutowiringConstructor = null;
        boolean hasExplicitDefaultConstructor = false;
        for (Constructor<?> constructor : constructors) {
            if (hasAutowiredAnnotation(constructor)) {
                if (explicitAutowiringConstructor != null) {
                    throw new BeanFactoryException(("Cannot create bean for class %s, " +
                            "multiple constructors are marked with autowire annotation").formatted(beanClass));
                }

                explicitAutowiringConstructor = constructor;
            }

            if (constructor.getParameterCount() == 0) {
                hasExplicitDefaultConstructor = true;
            }

            candidates.add(constructor);
        }

        if (explicitAutowiringConstructor != null) {
            return explicitAutowiringConstructor;
        } else if (candidates.size() == 1 && !hasExplicitDefaultConstructor) {
            return candidates.getFirst();
        } else if (candidates.size() == 2 && hasExplicitDefaultConstructor) {
            return candidates.stream().filter(constructor -> constructor.getParameterCount() > 0).findAny()
                    .orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Checks if given constructor is marked with the {@link Autowired} annotation.
     *
     * @param constructor a constructor to check.
     * @return {@code true} - if a constructor is marked with the {@link Autowired} annotation,
     * {@code false} - otherwise.
     */
    protected boolean hasAutowiredAnnotation(Constructor<?> constructor) {
        return constructor.getAnnotation(Autowired.class) != null;
    }


    /**
     * Safely retrieves bean class name from given bean definition.
     *
     * @param beanDefinition a definition from which to retrieve.
     * @param beanName       a name of a bean.
     * @return a bean class name.
     * @throws BeanFactoryException if bean class name is @{code null} in given bean definition.
     */
    protected static String retrieveBeanClassName(BeanDefinition beanDefinition, String beanName) {
        return Optional.ofNullable(beanDefinition.getBeanClassName())
                .orElseThrow(() -> new BeanFactoryException("Bean class name is not set for bean: " + beanName));
    }
}
