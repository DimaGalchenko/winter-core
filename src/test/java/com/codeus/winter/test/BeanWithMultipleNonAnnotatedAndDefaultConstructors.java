package com.codeus.winter.test;

@SuppressWarnings("unused")
public class BeanWithMultipleNonAnnotatedAndDefaultConstructors {

    private final Common bean;

    public BeanWithMultipleNonAnnotatedAndDefaultConstructors(BeanA beanA) {
        this.bean = beanA;
    }

    public BeanWithMultipleNonAnnotatedAndDefaultConstructors(BeanE beanE) {
        this.bean = beanE;
    }

    public BeanWithMultipleNonAnnotatedAndDefaultConstructors() {
        this.bean = new DefaultBean();
    }

    public Common getBean() {
        return bean;
    }

    public static class DefaultBean implements Common {

    }
}
