package com.codeus.winter.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A container that holds necessary data for resolving a dependency of a bean.
 */
public class DependencyDescriptor {

    private final Class<?> dependencyClass;
    private final Type dependencyType;
    private final String dependencyName;
    private final Annotation[] annotations;

    public DependencyDescriptor(
            String dependencyName,
            Type dependencyType,
            Class<?> dependencyClass,
            Annotation[] annotations) {
        this.dependencyName = dependencyName;
        this.dependencyClass = dependencyClass;
        this.dependencyType = dependencyType;
        this.annotations = annotations;
    }

    public DependencyDescriptor(String dependencyName, Type dependencyType) {
        this(dependencyName, dependencyType, getRawType(dependencyType), new Annotation[0]);
    }

    public DependencyDescriptor(Type dependencyType) {
        this(null, dependencyType);
    }

    protected static Class<?> getRawType(Type dependencyType) {
        Type rawType = dependencyType instanceof ParameterizedType
                ? ((ParameterizedType) dependencyType).getRawType()
                : dependencyType;

        return (Class<?>) rawType;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public boolean hasDependencyName() {
        return dependencyName != null && !dependencyName.isEmpty();
    }

    public Class<?> getDependencyClass() {
        return dependencyClass;
    }

    public Type getDependencyType() {
        return dependencyType;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyDescriptor that = (DependencyDescriptor) o;
        return Objects.equals(dependencyName, that.dependencyName) && Objects.equals(dependencyType, that.dependencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyName, dependencyType);
    }
}
