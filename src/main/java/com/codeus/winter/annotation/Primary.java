package com.codeus.winter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a bean should be given preference when multiple candidates are qualified
 * to autowire a single-valued dependency. If exactly one 'primary' bean exists among
 * the candidates, it will be the autowired value.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
