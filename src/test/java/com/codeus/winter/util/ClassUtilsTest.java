package com.codeus.winter.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilsTest {

    @Test
    @DisplayName("should resolve a Class for provided class name")
    void testResolveClassForExistingClassName() {
        Class<?> resolvedClass = ClassUtils.resolveClass("com.codeus.winter.util.ClassUtilsTest$TestClass");

        assertEquals(TestClass.class, resolvedClass);
    }

    @Test
    @DisplayName("should throw an exception if Class doesn't exist for provided name")
    void testFailResolveClassForNonExistingClassName() {
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.resolveClass("42.TestClass"));
    }

    static class TestClass {}
}