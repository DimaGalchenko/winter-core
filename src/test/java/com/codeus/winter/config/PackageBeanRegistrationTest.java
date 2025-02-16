package com.codeus.winter.config;

import com.codeus.winter.annotation.Component;
import com.codeus.winter.config.impl.BeanDefinitionImpl;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;

/**
 * Unit tests for {@link PackageBeanRegistration}.
 * These tests verify the behavior of the bean registration process, including
 * handling annotated classes, duplicate bean names, and empty package scenarios.
 */
class PackageBeanRegistrationTest {

    private PackageScanner packageScanner;
    private BeanDefinitionRegistry registry;
    private PackageBeanRegistration beanRegistration;

    /**
     * Sets up the test environment by mocking dependencies and initializing
     * the {@link PackageBeanRegistration} instance.
     */
    @BeforeEach
    void setUp() {
        packageScanner = mock(PackageScanner.class);
        registry = mock(BeanDefinitionRegistry.class);
        beanRegistration = new PackageBeanRegistration(packageScanner, registry);
    }

    /**
     * Tests that classes annotated with {@link Component} are successfully
     * registered as {@link BeanDefinition}s in the registry.
     */
    @Test
    void shouldRegisterBeansSuccessfully() {
        Class<?> testClass = WinterComponent.class;
        when(packageScanner.findClassesWithAnnotations(anyString(), anySet()))
                .thenReturn(Set.of(testClass));
        when(registry.containsBeanDefinition("winterComponent")).thenReturn(false);

        beanRegistration.registerBeans("com.framework");

        verify(registry).registerBeanDefinition(eq("winterComponent"), any(BeanDefinitionImpl.class));
    }

    /**
     * Tests that a {@link NotUniqueBeanDefinitionException} is thrown if two
     * classes with the same bean name are registered.
     */
    @Test
    void shouldThrowExceptionForDuplicateBeanName() {
        Class<?> testClass = WinterComponent.class;
        when(packageScanner.findClassesWithAnnotations(anyString(), anySet()))
                .thenReturn(Set.of(testClass));
        when(registry.containsBeanDefinition("winterComponent")).thenReturn(true);

        assertThrows(NotUniqueBeanDefinitionException.class, () ->
                beanRegistration.registerBeans("com.framework")
        );
    }

    /**
     * Tests that no bean definitions are registered if the scanned package
     * does not contain any annotated classes.
     */
    @Test
    void shouldHandleEmptyPackageGracefully() {
        when(packageScanner.findClassesWithAnnotations(anyString(), anySet()))
                .thenReturn(Set.of());

        beanRegistration.registerBeans("com.framework");

        verify(registry, never()).registerBeanDefinition(anyString(), any(BeanDefinitionImpl.class));
    }

    /**
     * Mock class annotated with {@link Component} to simulate a real component.
     */
    @Component
    static class WinterComponent {
    }
}
