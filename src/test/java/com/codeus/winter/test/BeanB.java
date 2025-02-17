package com.codeus.winter.test;

@SuppressWarnings("ClassCanBeRecord")
public class BeanB {
    private final BeanA beanA;

    public BeanB(BeanA beanA) {
        this.beanA = beanA;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
