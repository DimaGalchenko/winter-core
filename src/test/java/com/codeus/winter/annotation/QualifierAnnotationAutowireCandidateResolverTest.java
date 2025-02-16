package com.codeus.winter.annotation;


import com.codeus.winter.config.DependencyDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QualifierAnnotationAutowireCandidateResolverTest {

    private QualifierAnnotationAutowireCandidateResolver resolver;
    private DependencyDescriptor descriptor;

    @BeforeEach
    void setUp() {
        resolver = new QualifierAnnotationAutowireCandidateResolver();
        descriptor = mock(DependencyDescriptor.class);
    }

    @Test
    void testGetSuggestedNameWithQualifier() {
        Qualifier annotation = mock(Qualifier.class);
        when(annotation.value()).thenReturn("testName");
        when(annotation.annotationType()).thenAnswer(inv -> Qualifier.class);
        when(descriptor.getAnnotations()).thenReturn(new Annotation[]{annotation});

        String suggestedName = resolver.getSuggestedName(descriptor);
        assertEquals("testName", suggestedName);
    }

    @Test
    void testGetSuggestedNameWithoutQualifier() {
        when(descriptor.getAnnotations()).thenReturn(new Annotation[] {});

        String suggestedName = resolver.getSuggestedName(descriptor);
        assertNull(suggestedName);
    }

    @Test
    void testGetSuggestedNameNonStringValue() {
        Annotation annotation = mock(Annotation.class);
        when(annotation.annotationType()).thenAnswer(inv -> Qualifier.class);
        when(descriptor.getAnnotations()).thenReturn(new Annotation[]{annotation});

        String suggestedName = resolver.getSuggestedName(descriptor);
        assertNull(suggestedName);
    }
}
