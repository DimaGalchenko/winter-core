package com.codeus.winter.context;

import com.codeus.winter.config.BeanDefinition;
import com.codeus.winter.context.test.ComplexBean;
import com.codeus.winter.context.test.SimpleBean;
import com.codeus.winter.test.BeanA;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnotationApplicationContextTest {

    @Test
    @DisplayName("should initialize with beans from the given package")
    void shouldInitializeContextWithBeansFromGivenPackage() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        ComplexBean complexBean = context.getBean(ComplexBean.class);
        assertNotNull(complexBean);

        SimpleBean simpleBean = complexBean.getDependency();
        assertNotNull(simpleBean);
    }

    @Test
    @DisplayName("should delegate bean retrieval by name to the underlying BeanFactory")
    void shouldDelegateBeanRetrievalByNameToUnderlyingBeanFactory() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        assertNotNull(context.getBean("simpleBean"));
    }

    @Test
    @DisplayName("should delegate bean retrieval by name and class to the underlying BeanFactory")
    void shouldDelegateBeanRetrievalByNameAndClassToUnderlyingBeanFactory() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        assertNotNull(context.getBean("simpleBean", SimpleBean.class));
    }

    @Test
    @DisplayName("should delegate prototype bean creation to the underlying BeanFactory")
    void shouldDelegatePrototypeBeanCreationToUnderlyingBeanFactory() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        SimpleBean singleton = context.getBean(SimpleBean.class);
        SimpleBean prototype = context.createBean(SimpleBean.class);

        assertNotNull(singleton);
        assertNotNull(prototype);
        assertNotSame(singleton, prototype);
    }

    @Test
    @DisplayName("should delegate bean registration to the underlying BeanFactory")
    void shouldDelegateBeanRegistrationToUnderlyingBeanFactory() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(BeanA.class.getName());
        when(beanDefinition.isSingleton()).thenReturn(true);
        BeanA beanInstance = new BeanA();
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        context.registerBean("beanA", beanDefinition, beanInstance);
        BeanA beanFromContext = context.getBean(BeanA.class);

        assertNotNull(beanFromContext);
        assertSame(beanFromContext, beanInstance);
    }

    @Test
    @DisplayName("getId should return the default Application Context name")
    void getIdReturnsDefaultValue() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        String contextId = context.getId();

        assertNotNull(contextId);
        assertTrue(contextId.startsWith("com.codeus.winter.context.AnnotationApplicationContext"));
    }

    @Test
    @DisplayName("getDisplayName should return the default Application Context name")
    void getDisplayNameReturnsDefaultValue() {
        var context = new AnnotationApplicationContext("com.codeus.winter.context.test");

        String contextDisplayName = context.getDisplayName();

        assertNotNull(contextDisplayName);
        assertTrue(contextDisplayName.startsWith("com.codeus.winter.context.AnnotationApplicationContext"));
    }

}
