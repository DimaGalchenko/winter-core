package com.codeus.winter.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeus.winter.config.AbstractAutowireCapableBeanFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutowiredAnnotationBeanPostProcessorTest {

    private AutowiredAnnotationBeanPostProcessor postProcessor;

    @BeforeEach
    void setUpBeforeClass() {
        AbstractAutowireCapableBeanFactory mockBeanFactory = mock(AbstractAutowireCapableBeanFactory.class);
        when(mockBeanFactory.resolveDependency(any())).thenReturn(new BeanComponent());

        postProcessor = new AutowiredAnnotationBeanPostProcessor();
        postProcessor.setBeanFactory(mockBeanFactory);
    }

    @Test
    void injectField() {
        BeanWithAutowiredField bean = new BeanWithAutowiredField();
        assertNull(bean.getDependency(), "BeanWithAutowiredField's `dependency` should be `null` after instantiation");

        Object postProcessedBean = postProcessor.postProcessBeforeInitialization(bean, "BeanWithAutowiredField");

        assertNotNull(postProcessedBean);
        assertEquals(BeanWithAutowiredField.class, postProcessedBean.getClass());
        assertNotNull(((BeanWithAutowiredField) postProcessedBean).getDependency());
    }

    @Test
    void injectMethod() {
        BeanWithAutowiredMethod bean = new BeanWithAutowiredMethod();
        assertNull(bean.getDependency(), "BeanWithAutowiredMethod's `dependency` should be `null` after instantiation");

        Object postProcessedBean = postProcessor.postProcessBeforeInitialization(bean, "BeanWithAutowiredMethod");

        assertNotNull(postProcessedBean);
        assertEquals(BeanWithAutowiredMethod.class, postProcessedBean.getClass());
        assertNotNull(((BeanWithAutowiredMethod) postProcessedBean).getDependency());
    }
}
