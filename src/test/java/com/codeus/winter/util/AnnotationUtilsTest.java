package com.codeus.winter.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationUtilsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestAnnotation {
        String value() default "default";
        int number() default 42;
    }

    @TestAnnotation(value = "testValue", number = 99)
    private static class AnnotatedClass {}

    @Test
    void testGetValue_DefaultValueAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("testValue", AnnotationUtils.getValue(annotation));
    }

    @Test
    void testGetValue_SpecificAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals(99, AnnotationUtils.getValue(annotation, "number"));
    }

    @Test
    void testGetValue_NonExistentAttribute() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertNull(AnnotationUtils.getValue(annotation, "nonExistent"));
    }

    @Test
    void testGetValue_NullAnnotation() {
        assertNull(AnnotationUtils.getValue(null));
        assertNull(AnnotationUtils.getValue(null, "value"));
    }

    @Test
    void testGetValue_NullOrEmptyAttributeName() {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertNull(AnnotationUtils.getValue(annotation, null));
        assertNull(AnnotationUtils.getValue(annotation, ""));
        assertNull(AnnotationUtils.getValue(annotation, "   "));
    }
}