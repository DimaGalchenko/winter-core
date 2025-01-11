package com.codeus.winter.test;

@SuppressWarnings("unused")
public class BeanWithSelfInjection {

    private final BeanWithSelfInjection bean;

    public BeanWithSelfInjection(BeanWithSelfInjection bean) {
        this.bean = bean;
    }

    public BeanWithSelfInjection getBean() {
        return bean;
    }
}
