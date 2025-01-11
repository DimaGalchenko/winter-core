package com.codeus.winter.test;

public class BeanWithCyclicDependencyB {

  private final BeanWithCyclicDependencyA beanWithCyclicDependencyA;

  public BeanWithCyclicDependencyB(BeanWithCyclicDependencyA beanWithCyclicDependencyA) {
    this.beanWithCyclicDependencyA = beanWithCyclicDependencyA;
  }

  public BeanWithCyclicDependencyA getBeanD() {
    return beanWithCyclicDependencyA;
  }
}
