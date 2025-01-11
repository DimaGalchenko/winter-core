package com.codeus.winter.test;

public class BeanWithCyclicDependencyA {

  private BeanA beanA;
  private BeanWithCyclicDependencyB beanWithCyclicDependencyB;

  public BeanWithCyclicDependencyA(BeanA beanA, BeanWithCyclicDependencyB beanWithCyclicDependencyB) {
    this.beanA = beanA;
    this.beanWithCyclicDependencyB = beanWithCyclicDependencyB;
  }

  public BeanA getBeanA() {
    return beanA;
  }

  public BeanWithCyclicDependencyB getBeanE() {
    return beanWithCyclicDependencyB;
  }
}
