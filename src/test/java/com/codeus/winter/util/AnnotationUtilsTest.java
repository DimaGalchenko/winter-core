package com.codeus.winter.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class AnnotationUtilsTest {
    private static final int TEST_ANNOTATION_VALUE = 99;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestAnnotation {
        String value() default "default";

        int number() default 42;
    }

    @TestAnnotation(value = "testValue", number = TEST_ANNOTATION_VALUE)
    private static final class AnnotatedClass {
    }

    @Test
    void shouldGetDefaultValueAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("testValue", AnnotationUtils.getValue(annotation));
    }

    @Test
    void shouldGetSpecificAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals(TEST_ANNOTATION_VALUE, AnnotationUtils.getValue(annotation, "number"));
    }

    @Test
    void shouldGetNonExistentAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertNull(AnnotationUtils.getValue(annotation, "nonExistent"));
    }

    @Test
    void shouldGetNullAnnotation() {
        assertNull(AnnotationUtils.getValue(null));
        assertNull(AnnotationUtils.getValue(null, "value"));
    }

    @Test
    void shouldGetNullOrEmptyAttributeName() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertNull(AnnotationUtils.getValue(annotation, null));
        assertNull(AnnotationUtils.getValue(annotation, ""));
        assertNull(AnnotationUtils.getValue(annotation, "   "));
    }
}
