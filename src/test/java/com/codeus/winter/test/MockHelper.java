package com.codeus.winter.test;

import com.codeus.winter.config.BeanDefinition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MockHelper {

    private MockHelper() {
    }

    public static BeanDefinition singletonBeanDefinitionMock(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(beanClass.getName());
        when(beanDefinition.isSingleton()).thenReturn(true);

        return beanDefinition;
    }

    @SuppressWarnings("SameParameterValue")
    public static BeanDefinition prototypeBeanDefinitionMock(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(beanClass.getName());
        when(beanDefinition.isPrototype()).thenReturn(true);

        return beanDefinition;
    }
}
