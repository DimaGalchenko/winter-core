package com.codeus.winter.test;

public class BeanWithDependencyByInterface {

    private final Common commonBean;

    public BeanWithDependencyByInterface(Common commonBean) {
        this.commonBean = commonBean;
    }

    public Common getWrappeeBean() {
        return commonBean;
    }
}
