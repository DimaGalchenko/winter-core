package com.codeus.winter.exception;

/**
 * Exception that signal that requested bean is still in creation stage.
 * This typically happen when two beans have cyclic dependency.
 */
public class BeanCurrentlyInCreationException extends RuntimeException {

    public BeanCurrentlyInCreationException(String beanName) {
        super("Bean %s is currently in creation. Check possible cyclic dependencies".formatted(beanName));
    }

}
