package com.codeus.winter.config;

import static com.codeus.winter.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codeus.winter.config.impl.BeanDefinitionRegistryImpl;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PackageBeanRegistration}. These tests verify the behavior of the bean
 * registration process, including handling annotated classes, duplicate bean names, and empty
 * package scenarios.
 */
class PackageBeanRegistrationTest {

    private BeanDefinitionRegistry registry;
    private PackageBeanRegistration beanRegistration;

    /**
     * Sets up the test environment by mocking dependencies and initializing the
     * {@link PackageBeanRegistration} instance.
     */
    @BeforeEach
    void setUp() {
        registry = new BeanDefinitionRegistryImpl();
        beanRegistration = new PackageBeanRegistration(registry);
    }

    @Test
    void shouldRegisterBeansSuccessfully() {
        beanRegistration.registerBeans("com.codeus.winter.config.test.inner");

        BeanDefinition definition = registry.getBeanDefinition("winterComponent");
        assertNotNull(definition);
        // bean name
        assertEquals("com.codeus.winter.config.test.inner.WinterComponent", definition.getBeanClassName());
        // scope
        assertEquals(SCOPE_PROTOTYPE, definition.getScope());
        assertFalse(definition.isSingleton());
        // depends on
        String[] dependencies = {"com.codeus.winter.config.test.inner.AutowiredComponent",
            "com.codeus.winter.config.test.inner.QualifierComponent"
        };
        assertArrayEquals(dependencies, definition.getDependsOn());
        // init method name
        assertEquals("init", definition.getInitMethodName());
        // destroy method name
        assertEquals("destroy", definition.getDestroyMethodName());
    }

    @Test
    void shouldThrowExceptionForDuplicateBeanNamePassedSamePackageTwoTimes() {
        beanRegistration.registerBeans("com.codeus.winter.config.test.inner");
        assertThrows(NotUniqueBeanDefinitionException.class, () ->
            beanRegistration.registerBeans("com.codeus.winter.config.test.inner")
        );
    }

    @Test
    void shouldThrowExceptionForDuplicateBeanNameFoundSameClassInDifferentPackages() {
        assertThrows(NotUniqueBeanDefinitionException.class, () ->
            beanRegistration.registerBeans("com.codeus.winter.config.test")
        );
    }

    @Test
    void shouldHandleEmptyPackageGracefully() {
        beanRegistration.registerBeans("test.com.codeus.winter.test");

        BeanDefinition winterComponent = registry.getBeanDefinition("winterComponent");
        assertNull(winterComponent);
    }
}
