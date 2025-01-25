package com.codeus.winter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyDescriptorTest {

    @Test
    @DisplayName("should return provided dependency name")
    void testGetDependencyName() {
        String name = "testName";
        DependencyDescriptor descriptor = new DependencyDescriptor(name, String.class);

        assertEquals(name, descriptor.getDependencyName());
    }

    @Test
    @DisplayName("should return null if dependency name is not provided")
    void testGetDependencyNameReturnsNullIfDependencyNameIsNotSet() {
        DependencyDescriptor descriptor = new DependencyDescriptor(String.class);

        assertNull(descriptor.getDependencyName());
    }

    @Test
    @DisplayName("should return true if dependency name is provided and it's not empty, false - otherwise")
    void testHasDependencyName() {
        DependencyDescriptor descriptorWithName = new DependencyDescriptor("testName", String.class);
        DependencyDescriptor descriptorWithoutName = new DependencyDescriptor(String.class);
        DependencyDescriptor descriptorWithEmptyName = new DependencyDescriptor("", String.class);

        assertTrue(descriptorWithName.hasDependencyName());
        assertFalse(descriptorWithoutName.hasDependencyName());
        assertFalse(descriptorWithEmptyName.hasDependencyName());
    }

    @Test
    @DisplayName("should return provided dependency class")
    void testGetDependencyClass() {
        DependencyDescriptor descriptor = new DependencyDescriptor(String.class);

        assertEquals(String.class, descriptor.getDependencyClass());
    }

    @Test
    @DisplayName("should return provided dependency type")
    void testGetDependencyType() {
        Type type = String.class;
        DependencyDescriptor descriptor = new DependencyDescriptor(type);

        assertEquals(type, descriptor.getDependencyType());
    }

    @Test
    @DisplayName("should return generic class as a dependency class if provided type is a generic type")
    void testGetDependencyClassForGenericType() {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        DependencyDescriptor descriptor = new DependencyDescriptor(type);

        assertEquals(List.class, descriptor.getDependencyClass());
    }

    @Test
    @DisplayName("should return generic type as a dependency type if provided type is a generic type")
    void testGetDependencyTypeForGenericType() {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        DependencyDescriptor descriptor = new DependencyDescriptor(type);

        assertEquals(type, descriptor.getDependencyType());
    }
}
