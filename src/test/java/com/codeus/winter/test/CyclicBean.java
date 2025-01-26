package com.codeus.winter.test;

public class CyclicBean {


    static class BeanOne {
        private BeanTwo beanTwo;

        BeanOne(BeanTwo beanTwo) {
            this.beanTwo = beanTwo;
        }
    }

    static class BeanTwo {
        private BeanOne beanOne;

        BeanTwo(BeanOne beanOne) {
            this.beanOne = beanOne;
        }
    }
}
