package com.codeus.winter.test;

import com.codeus.winter.annotation.Autowired;

@SuppressWarnings("unused")
public class BeanWithMultipleAutowiringConstructors {
    private final BeanA beanA;
    private final BeanE beanE;

    @Autowired
    public BeanWithMultipleAutowiringConstructors(BeanA beanA) {
        this.beanA = beanA;
        this.beanE = null;
    }

    @Autowired
    public BeanWithMultipleAutowiringConstructors(BeanE beanE) {
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
