package com.codeus.winter.test;

@SuppressWarnings("unused")
public class BeanWithNonAnnotatedAndDefaultConstructors {

    private final BeanA beanA;

    public BeanWithNonAnnotatedAndDefaultConstructors(BeanA beanA) {
        this.beanA = beanA;
    }

    public BeanWithNonAnnotatedAndDefaultConstructors() {
        beanA = null;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
