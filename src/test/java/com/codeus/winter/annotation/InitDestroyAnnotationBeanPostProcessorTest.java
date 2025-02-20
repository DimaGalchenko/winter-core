package com.codeus.winter.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class InitDestroyAnnotationBeanPostProcessorTest {

    private static final String INIT_DESTROY_BEAN_NAME = "BeanWithInitAndDestroyMethods";
    private BeanWithInitAndDestroyMethods spyBean;
    private InitDestroyAnnotationBeanPostProcessor postProcessor;

    @BeforeEach
    void setUpBeforeClass() {
        postProcessor = new InitDestroyAnnotationBeanPostProcessor();
        spyBean = spy(new BeanWithInitAndDestroyMethods());
    }

    @Test
    void initPostConstructAnnotation() {
        Object postProcessedBean = postProcessor.postProcessBeforeInitialization(spyBean, INIT_DESTROY_BEAN_NAME);

        assertNotNull(postProcessedBean);
        verify(spyBean).init();
        verifyNoMoreInteractions(spyBean);
    }

    @Test
    void destroyPreDestroyAnnotation() {
        postProcessor.postProcessBeforeDestruction(spyBean, INIT_DESTROY_BEAN_NAME);

        verify(spyBean).destroy();
        verifyNoMoreInteractions(spyBean);
    }
}
