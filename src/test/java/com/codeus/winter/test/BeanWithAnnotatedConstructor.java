package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;

public class BeanWithAnnotatedConstructor {

    private final BeanA beanA;

    @Autowired
    public BeanWithAnnotatedConstructor(BeanA beanA) {
        this.beanA = beanA;
    }

    public BeanWithAnnotatedConstructor() {
        this.beanA = null;
    }

    public BeanA getBeanA() {
        return beanA;
    }
}
