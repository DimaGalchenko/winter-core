package com.codeus.winter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codeus.winter.config.impl.BeanDefinitionRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassPathBeanDefinitionScannerTest {
    private BeanDefinitionRegistry registry;
    private ClassPathBeanDefinitionScanner scanner;

    @BeforeEach
    void setUp() {
        registry = new BeanDefinitionRegistryImpl();
        scanner = new ClassPathBeanDefinitionScanner(registry);
    }

    @Test
    void testScanPackagesWhenValidBasePackagesShouldReturnCorrectCount() {
        // given
        String basePackage = "com.example.test";

        // when
        int beanCount = scanner.scanPackages(basePackage);

        // then
        assertEquals(0, beanCount);
    }

    @Test
    void testScanPackagesWhenNoBasePackagesShouldThrowException() {
        // when and then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanPackages();
        });

        assertEquals("At least one base package must be specified", thrown.getMessage());
    }
}
