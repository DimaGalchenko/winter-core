package com.codeus.winter.config;

import static com.codeus.winter.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeus.winter.config.impl.BeanDefinitionRegistryImpl;
import com.codeus.winter.config.impl.PackageScannerImpl;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PackageBeanRegistration}. These tests verify the behavior of the bean
 * registration process, including handling annotated classes, duplicate bean names, and empty
 * package scenarios.
 */
class PackageBeanRegistrationTest {

    private PackageScanner packageScanner;
    private BeanDefinitionRegistry registry;
    private PackageBeanRegistration beanRegistration;

    /**
     * Sets up the test environment by mocking dependencies and initializing the
     * {@link PackageBeanRegistration} instance.
     */
    @BeforeEach
    void setUp() {
        packageScanner = new PackageScannerImpl();
        registry = new BeanDefinitionRegistryImpl();
        beanRegistration = new PackageBeanRegistration(packageScanner, registry);
    }

    @Test
    void shouldRegisterBeansSuccessfully() {
        beanRegistration.registerBeans("com.codeus.winter.config");

        BeanDefinition definition = registry.getBeanDefinition("winterComponent");
        assertNotNull(definition);
        // bean name
        assertEquals("com.codeus.winter.config.WinterComponent", definition.getBeanClassName());
        // scope
        assertEquals(SCOPE_PROTOTYPE, definition.getScope());
        assertFalse(definition.isSingleton());
        // depends on
        String[] dependencies = {"com.codeus.winter.config.QualifierComponent",
            "com.codeus.winter.config.AutowiredComponent"};
        assertTrue(Arrays.equals(dependencies, definition.getDependsOn()));
        // init method name
        assertEquals("init", definition.getInitMethodName());
        // destroy method name
        assertEquals("destroy", definition.getDestroyMethodName());
    }

    /**
     * Tests that a {@link NotUniqueBeanDefinitionException} is thrown if two classes with the same
     * bean name are registered.
     */
    @Test
    void shouldThrowExceptionForDuplicateBeanName() {
        beanRegistration.registerBeans("com.codeus.winter.config");

        assertThrows(NotUniqueBeanDefinitionException.class, () ->
            beanRegistration.registerBeans("com.codeus.winter.config")
        );
    }

    /**
     * Tests that no bean definitions are registered if the scanned package does not contain any
     * annotated classes.
     */
    @Test
    void shouldHandleEmptyPackageGracefully() {
        beanRegistration.registerBeans("test.com.codeus.winter.test");

        BeanDefinition winterComponent = registry.getBeanDefinition("winterComponent");
        assertNull(winterComponent);
    }
}
