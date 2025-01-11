package com.codeus.winter.test;

public class BeanWithDependencyByInterface {

    private final BeanInterface bean;

    public BeanWithDependencyByInterface(BeanInterface bean) {
        this.bean = bean;
    }

    public BeanInterface getWrappeeBean() {
        return bean;
    }
}
