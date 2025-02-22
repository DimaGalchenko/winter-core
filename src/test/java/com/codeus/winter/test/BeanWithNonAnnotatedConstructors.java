package com.codeus.winter.test;

@SuppressWarnings("unused")
public class BeanWithNonAnnotatedConstructors {

    private final BeanA beanA;
    private final BeanE beanE;

    public BeanWithNonAnnotatedConstructors(BeanA beanA) {
        this.beanA = beanA;
        this.beanE = null;
    }

    public BeanWithNonAnnotatedConstructors(BeanE beanE) {
        this.beanA = null;
        this.beanE = beanE;
    }

    public BeanA getBeanA() {
        return beanA;
    }

    public BeanE getBeanE() {
        return beanE;
    }
}
