package com.codeus.winter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Scope indicates the name of a scope to use for instances of the annotated type.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    /**
     * Specifies the name of the scope to use for the annotated component.
     * Defaults to an empty string ("") which implies SCOPE_SINGLETON.
     * @return scope name.
     */
    String value() default "";
}
