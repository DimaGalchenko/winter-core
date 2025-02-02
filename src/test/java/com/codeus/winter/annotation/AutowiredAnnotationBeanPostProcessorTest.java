package com.codeus.winter.annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeus.winter.config.BeanFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutowiredAnnotationBeanPostProcessorTest {

    private AutowiredAnnotationBeanPostProcessor postProcessor;

    @BeforeEach
    void setUpBeforeClass() {
        BeanFactory mockBeanFactory = mock(BeanFactory.class);
        when(mockBeanFactory.getBean(BeanComponent.class)).thenReturn(new BeanComponent());

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
