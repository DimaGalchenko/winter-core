package com.codeus.winter.test;

public class BeansWithCyclicDependency {


    public static class BeanOne {
        private final BeanTwo beanTwo;

        public BeanOne(BeanTwo beanTwo) {
            this.beanTwo = beanTwo;
        }

        public BeanTwo getBeanTwo() {
            return beanTwo;
        }
    }

    public static class BeanTwo {
        private final BeanOne beanOne;

        public BeanTwo(BeanOne beanOne) {
            this.beanOne = beanOne;
        }

        public BeanOne getBeanOne() {
            return beanOne;
        }
    }
}
