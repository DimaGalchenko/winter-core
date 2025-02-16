package com.codeus.winter.config;

import com.codeus.winter.test.BeanA;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class BeanDefinitionFactoryTest {

    @Test
    @DisplayName("Should create a simple bean definition with prototype scope for a given class")
    void testPrototypeBeanDefinition() {
        BeanDefinition beanDefinition = BeanDefinitionFactory.prototypeBeanDefinition(BeanA.class);

        assertNotNull(beanDefinition);
        assertEquals(BeanA.class.getName(), beanDefinition.getBeanClassName());
        assertEquals(BeanDefinition.SCOPE_PROTOTYPE, beanDefinition.getScope());

        assertNull(beanDefinition.getInitMethodName());
        assertNull(beanDefinition.getFactoryBeanName());
        assertNull(beanDefinition.getDestroyMethodName());

        assertNotNull(beanDefinition.getDependsOn());
        assertEquals(0, beanDefinition.getDependsOn().length);
    }
}
