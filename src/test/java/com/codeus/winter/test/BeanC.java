package com.codeus.winter.test;

@SuppressWarnings("ClassCanBeRecord")
public class BeanC {
    private final BeanA beanA;
    private final BeanB beanB;

    public BeanC(BeanA beanA, BeanB beanB) {
        this.beanA = beanA;
        this.beanB = beanB;
    }

    public BeanA getBeanA() {
        return beanA;
    }

    public BeanB getBeanB() {
        return beanB;
    }
}
